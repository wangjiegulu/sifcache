package com.wangjiegulu.sifcache_lib

import com.wangjiegulu.sifcache_lib.keyparts.SifWherePart
import java.time.Duration
import java.time.LocalDateTime

/**
 * Sif 接口
 */
interface SifInstance {
    /**
     * 注册 Key
     */
    fun register(sifKey: SifKey<*, SifWherePart>)

    /**
     * 根据 Key 获取缓存，如果没有，则调用 calc 方法生成并进行缓存
     */
    fun <ValueType /* 缓存中返回的数据类型 */, WherePart /*条件类型*/ : SifWherePart> getOrCalc(
        key: SifKey<ValueType, WherePart>,
        dataWherePart: WherePart,

        // 以下两个时间决定缓存有效时间长度（以短的为准）
        timeout: Duration = key.defaultTimeout, // 过期时间，默认为 Key 中配置的默认 timeout
        lessThanDateTimeFunc: ((ValueType) -> LocalDateTime?)? = null, // 不超过这个时间点

        lockWhenCalc: Boolean = true, // 调用 calc 时是否需要加锁

        // 缓存不存在时的回调
        calc: (() -> ValueType?)? = null
    ): ValueType?

    /**
     * 更新缓存，并触发一次级联处理
     */
    fun <ValueType, WherePart : SifWherePart> set(
        key: SifKey<ValueType, WherePart>,
        dataWherePart: WherePart,
        value: ValueType,

        // 以下两个时间决定缓存有效时间长度（以短的为准）
        timeout: Duration = key.defaultTimeout, // 过期时间，默认为 Key 中配置的默认 timeout
        lessThanDateTimeFunc: ((ValueType) -> LocalDateTime?)? = null, // 不超过这个时间点
        setCondition: SetCondition = SetCondition.FORCE // 设置的条件，默认为强制设置（不存 key 是否存在）
    )

    /**
     * 是否存在该 key
     */
    fun <ValueType, WherePart : SifWherePart> hasKey(
        key: SifKey<ValueType, WherePart>,
        dataWherePart: WherePart,
    ): Boolean

    /**
     * 删除某个 key(不推荐使用 delete，推荐使用 set 替换 key 或者等过期)
     * 原因：当要删除的缓存数据是 BLANK 时，无法做到级联处理；
     */
//    @Deprecated("废弃，当要删除的数据是 BLANK 时，无法做到级联处理")
    fun <ValueType, WherePart : SifWherePart> delete(
        key: SifKey<ValueType, WherePart>,
        dataWherePart: WherePart,
        associateHandle: Boolean = true // 删除之后是否级联处理
    )

    /**
     * 触发一次级联处理
     */
    fun <ValueType> triggerAssociateHandle(
        sifEvent: SifEvent<ValueType>,
        value: ValueType,
        triggerReason: TriggerReason,
        exceptSifKey: SifKey<*, *>? = null // 触发的时候排除掉指定的 key
    )

}