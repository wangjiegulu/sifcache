package com.wangjiegulu.sifcache.ext.sif

import com.wangjiegulu.sifcache.ext.sif.keyparts.SifWherePart

/**
 * 级联处理：依赖的数据变更后触发的处理器
 */
interface SifAssociateHandler<CacheImpl, WherePart : SifWherePart, T> {
    /**
     * 级联处理回调，当关心的数据类型变更或删除时会回调这个方法进行处理
     */
    fun handleAssociate(
        cacheImpl: CacheImpl,
        associateSifKey: SifKey<*, WherePart>,
        obj: T,
        triggerReason: TriggerReason
    )

    /**
     * 是否优先在统一的事务中执行
     */
    fun isPreferHandleInTransaction(): Boolean
}

abstract class AbstractSifAssociateHandler<CacheImpl, WherePart : SifWherePart, T>(
    private val isPreferHandleInTransaction: Boolean
) : SifAssociateHandler<CacheImpl, WherePart, T> {

    /**
     * 是否优先在统一的事务中执行
     * 默认为 true
     */
    override fun isPreferHandleInTransaction(): Boolean = isPreferHandleInTransaction
}

/**
 * 触发的原因
 */
enum class TriggerReason {
    CREATE_OR_UPDATE,
    DELETE;

    fun isCreateOrUpdate() = CREATE_OR_UPDATE == this

    fun isDelete() = DELETE == this
}