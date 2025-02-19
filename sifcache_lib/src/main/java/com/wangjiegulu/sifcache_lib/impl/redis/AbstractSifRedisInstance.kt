package com.wangjiegulu.sifcache_lib.impl.redis

import com.wangjiegulu.sifcache_lib.*
import com.wangjiegulu.sifcache_lib.keyparts.SifWherePart
import com.wangjiegulu.sifcache_lib.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import kotlin.random.Random


/**
 * Redis 实现的 Sif
 */
abstract class AbstractSifRedisInstance(
    protected val cacheImpl: RedisTemplate<String, Any>,
    sifKeysLoaders: List<SifKeysLoader>,
    sifInstanceId: String
) :
    AbstractSifInstance<RedisTemplate<String, Any>>(cacheImpl, sifKeysLoaders, sifInstanceId) {

    private val logger: Logger = LoggerFactory.getLogger(AbstractSifRedisInstance::class.java)

    /**
     * 获取数据
     * - 如果命中缓存，则直接返回
     * - 如果没有命中缓存，则调用 calc 进行计算，并缓存计算的结果，最后返回结果
     */
    override fun <ValueType, WherePart : SifWherePart> getOrCalc(
        key: SifKey<ValueType, WherePart>,
        dataWherePart: WherePart,
        timeout: Duration,
        lessThanDateTimeFunc: ((ValueType) -> LocalDateTime?)?,
        lockWhenCalc: Boolean,
        calc: (() -> ValueType?)?
    ): ValueType? {
        var count = 0
        while (count < 3) {
            try {
                return getOrCalcInternal(key, dataWherePart, timeout, lessThanDateTimeFunc, lockWhenCalc, calc)
            } catch (e: SifCalcLockFailureException) {
                logger.warn("getOrCalc tryLock failure, count: ${count}, message: ${e.message}")
                count++
                Thread.sleep(100)
            }
        }
        return null
    }

    @Throws(SifCalcLockFailureException::class)
    private fun <ValueType, WherePart : SifWherePart> getOrCalcInternal(
        key: SifKey<ValueType, WherePart>,
        dataWherePart: WherePart,
        timeout: Duration,
        lessThanDateTimeFunc: ((ValueType) -> LocalDateTime?)?,

        lockWhenCalc: Boolean,
        calc: (() -> ValueType?)?
    ): ValueType? {
        val stringKey = key.calcKey(dataWherePart)
        try {
            val value = cacheImpl.opsForValue().get(stringKey)
            if (null != value) {
                logger.debug("getOrCalcInternal KEY HIT!\n┏━ SIFInstance: ${this.javaClass.simpleName}\n┣━ key        : ${stringKey}\n┗━ value      : $value\n\n")

                // 解决缓存穿透，如果是空字符串，则直接返回 null
                if (isSifValueBlank(value)) {
                    return null
                }
                return value as ValueType
            }
        } catch (e: Throwable) {
            logger.error("getOrCalcInternal::get", e)
            // 降级调用 calc
        }

        // 没有设置 calc，则直接返回 null
        if (null == calc) {
            return null
        }

        // 调用 calc 时是否需要加锁
        val newValue = if (lockWhenCalc) { // 防止缓存击穿
            val lockKey = "SLK.${stringKey}"
            // 获取锁失败，抛异常给上层（是否需要重试由上层决定）
            if (!tryLock(lockKey)) {
                throw SifCalcLockFailureException("Sif calc lock FAILURE!")
            }
            // 获取锁成功
            try {
                calc() ?: SIF_VALUE_BLANK // 防止缓存穿透，如果不存在则保存为空字符串("")
            } finally {
                unlock(lockKey)
            }
        } else {
            calc() ?: SIF_VALUE_BLANK // 防止缓存穿透，如果不存在则保存为空字符串("")
        }

        // 保存新值
        trySetInternal(stringKey, newValue, timeout, lessThanDateTimeFunc)

        newValue as ValueType

        // 级联处理依赖当前 type 的缓存
        triggerAssociateHandle(key.valueTypePart, newValue, TriggerReason.CREATE_OR_UPDATE, key)

        return if (newValue is String) null else newValue
    }

    // 获取锁
    private fun tryLock(lockKey: String): Boolean {
        //自定义互斥锁  将申请锁的结果返回
        try {
            val flag = cacheImpl.opsForValue().setIfAbsent(lockKey, "1", 10L, TimeUnit.SECONDS)
            logger.debug("tryLock, key: $lockKey, flag: $flag")
            return flag ?: false
        } catch (e: Throwable) {
            logger.error("tryLock failure, ignored", e)
            return true
        }
    }

    // 释放锁
    private fun unlock(lockKey: String) {
        logger.debug("unlock, key: $lockKey")
        try {
            cacheImpl.delete(lockKey)
        } catch (e: Throwable) {
            logger.error("unlock failure, ignored", e)
        }
    }

    /**
     * 缓存数据
     *
     * - 缓存之后，并通过 AssociateHandler 进行通知进行级联处理
     */
    override fun <ValueType, WherePart : SifWherePart> set(
        key: SifKey<ValueType, WherePart>,
        dataWherePart: WherePart,
        value: ValueType,
        timeout: Duration,
        lessThanDateTimeFunc: ((ValueType) -> LocalDateTime?)?,
        setCondition: SetCondition
    ) {
        val stringKey = key.calcKey(dataWherePart)

        // 如果只不存在才更新, 存在则跳过
        if (setCondition.isIfAbsent() && hasKey(key, dataWherePart)) {
            logger.debug("SifKey[{}] is exist, set skipped", stringKey)
            return
        }

        // 如果只存在才更新，不存在则跳过
        if (setCondition.isIfPresent() && !hasKey(key, dataWherePart)) {
            logger.debug("SifKey[{}] is not exist, set skipped", stringKey)
            return
        }

        // 执行更新
        trySetInternal(stringKey, value as Any, timeout, lessThanDateTimeFunc)

        // 级联处理依赖当前 type 的缓存
        triggerAssociateHandle(key.valueTypePart, value, TriggerReason.CREATE_OR_UPDATE, key)
    }

    /**
     * 是否存在该 key
     */
    override fun <ValueType, WherePart : SifWherePart> hasKey(
        key: SifKey<ValueType, WherePart>,
        dataWherePart: WherePart
    ): Boolean = cacheImpl.hasKey(key.calcKey(dataWherePart))

    /**
     * 当要删除的数据是 BLANK 时，无法做到级联处理
     */
    override fun <ValueType, WherePart : SifWherePart> delete(
        key: SifKey<ValueType, WherePart>,
        dataWherePart: WherePart,
        associateHandle: Boolean
    ) {
        val stringKey = key.calcKey(dataWherePart)
        val value = cacheImpl.opsForValue().get(stringKey) ?: return // value 是空表示没有缓存该数据，则直接返回

        // 执行删除
        val deleted = cacheImpl.delete(stringKey)
        if (!deleted) {
            return
        }

        // 不需要级联处理，则返回
        if (!associateHandle) {
            return
        }

        // 级联处理
        value as ValueType
        triggerAssociateHandle(key.valueTypePart, value, TriggerReason.DELETE)
    }

    /**
     * 缓存数据
     *
     * 以下两个条件取最小值：
     *
     * - timeout：超时时间
     * - lessThanDateTimeFunc：小于某个具体的时间点（比如会员过期时间则该值可以设置为过期时间）
     */
    private fun <ValueType> trySetInternal(
        stringKey: String,
        value: Any,
        timeout: Duration,
        lessThanDateTimeFunc: ((ValueType) -> LocalDateTime?)?
    ) {
        try {
            val lessThanDateTime =
                if (isSifValueBlank(value)) null else lessThanDateTimeFunc?.invoke(value as ValueType)

            // less than 为空，则直接保存
            if (null == lessThanDateTime) {
                setFinally(stringKey, value, timeout)
                return
            }

            // 如果当前时间 大于 less than，则不保存
            val now = LocalDateTime.now()
            if (now >= lessThanDateTime) {
                return
            }

            // 差值
            val deltaDuration = Duration.between(now, lessThanDateTime)
            if (deltaDuration.isNegative) { // 超时时间点小于当前时间点，则不缓存
                return
            }

            // 差值与 timeout 取小值
            setFinally(stringKey, value, if (deltaDuration < timeout) deltaDuration else timeout)
        } catch (e: Throwable) {
            logger.error("setInternal::set", e)
        }
    }

    /**
     * 真正的 set 操作（set 前会对设置的超时时间进行一定范围内的随机，防止缓存雪崩）
     */
    private fun setFinally(stringKey: String, value: Any, timeout: Duration) {
        val actualTimeout =
            if (timeout > Duration.ofMinutes(2)) {
                timeout.minusMillis(Random.nextLong(10_000)) // 防止缓存雪崩，随机时间
            } else {
                timeout
            }
        cacheImpl.opsForValue().set(stringKey, value, actualTimeout)
    }

}