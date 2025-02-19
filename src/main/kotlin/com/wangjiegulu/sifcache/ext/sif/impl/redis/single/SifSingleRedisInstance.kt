package com.wangjiegulu.sifcache.ext.sif.impl.redis.single

import com.wangjiegulu.sifcache.ext.sif.*
import com.wangjiegulu.sifcache.ext.sif.impl.redis.AbstractSifRedisInstance
import com.wangjiegulu.sifcache.ext.sif.keyparts.SifWherePart
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisOperations
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.SessionCallback
import java.util.*


/**
 * Redis 实现的 Sif
 * 直接调用通知
 */
open class SifSingleRedisInstance(
    cacheImpl: RedisTemplate<String, Any>,
    sifKeysLoaders: List<SifKeysLoader>,
    sifInstanceId: String = UUID.randomUUID().toString()
) : AbstractSifRedisInstance(cacheImpl, sifKeysLoaders, sifInstanceId) {
    private val logger: Logger = LoggerFactory.getLogger(SifSingleRedisInstance::class.java)

    /**
     * 级联处理实现
     */
    override fun <ValueType> onDoAssociateHandle(
        sifEvent: SifEvent<ValueType>,
        value: ValueType,
        triggerReason: TriggerReason,
        associateKeys: List<SifKey<*, SifWherePart>>
    ) {
        if (associateKeys.isEmpty()) {
            return
        }

        // 直接调用处理的列表
        val handleDirectlyList =
            arrayListOf<Pair<SifKey<*, SifWherePart>, SifAssociateHandler<RedisOperations<String, Any>, SifWherePart, ValueType>>>()
        // 在事务中调用处理的列表
        val handleInTransactionList =
            arrayListOf<Pair<SifKey<*, SifWherePart>, SifAssociateHandler<RedisOperations<String, Any>, SifWherePart, ValueType>>>()

        // 两种方式调用统计
        associateKeys.forEach { associateKey ->
            associateKey.associateHandlers?.get(sifEvent)?.let { associateHandler ->
                try {
                    associateHandler as SifAssociateHandler<RedisOperations<String, Any>, SifWherePart, ValueType>
                    if (associateHandler.isPreferHandleInTransaction()) { // 要在事务中处理
                        handleInTransactionList.add(Pair(associateKey, associateHandler))
                    } else { // 要直接调用处理
                        handleDirectlyList.add(Pair(associateKey, associateHandler))
                    }
                } catch (e: Throwable) {
                    logger.error("onDoAssociateHandle cast failure", e)
                }
            }
        }

        // 处理事务进行变更
        if (handleInTransactionList.isNotEmpty()) {
            logger.debug("onDoAssociateHandle handleInTransaction size: {}", handleInTransactionList.size)
            val executeResult = cacheImpl.execute(object : SessionCallback<List<Any>> {
                override fun <K : Any?, V : Any?> execute(operations: RedisOperations<K, V>): List<Any>? {
                    operations.multi()
                    handleInTransactionList.forEach {
                        try {
                            it.second.handleAssociate(
                                operations as RedisOperations<String, Any>,
                                it.first,
                                value,
                                triggerReason
                            )
                        } catch (e: Throwable) {
                            logger.error("onDoAssociateHandle handleInTransaction failure", e)
                        }
                    }
                    return operations.exec()
                }

            })

            logger.debug("onDoAssociateHandle handleInTransaction result: {}", executeResult)
        }

        // 直接调用进行变更
        if (handleDirectlyList.isNotEmpty()) {
            logger.debug("onDoAssociateHandle handleDirectly size: {}", handleDirectlyList.size)
            try{
                handleDirectlyList.forEach {
                    it.second.handleAssociate(
                        cacheImpl,
                        it.first,
                        value,
                        triggerReason
                    )
                }
            } catch (e: Throwable) {
                logger.error("onDoAssociateHandle handleDirectly failure", e)
            }
        }


    }

}