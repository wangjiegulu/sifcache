package com.wangjiegulu.sifcache_lib.redis

import com.wangjiegulu.sifcache_lib.AbstractSifAssociateHandler
import com.wangjiegulu.sifcache_lib.SifKey
import com.wangjiegulu.sifcache_lib.TriggerReason
import com.wangjiegulu.sifcache_lib.keyparts.SifWherePart
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisOperations

/**
 * 该 AssociateHandler 级联处理数据方式
 * - 直接删除缓存
 */
class DefaultRedisDeleteAssociateHandler<WherePart : SifWherePart, T>(
    isPreferHandleInTransaction: Boolean = true,
    val getWherePartsToDeleteFunc: (T) -> Array<WherePart>
) : AbstractSifAssociateHandler<RedisOperations<String, Any>, WherePart, T>(isPreferHandleInTransaction) {

    private val logger: Logger = LoggerFactory.getLogger(DefaultRedisDeleteAssociateHandler::class.java)

    override fun handleAssociate(
        cacheImpl: RedisOperations<String, Any>,
        associateSifKey: SifKey<*, WherePart>,
        obj: T,
        triggerReason: TriggerReason
    ) {
        val wherePartsToDelete = getWherePartsToDeleteFunc(obj)
        wherePartsToDelete.forEach {
            val stringKey = associateSifKey.calcKey(it)

            logger.debug(
                "DefaultRedisDeleteAssociateHandler, handleAssociate in, stringKey: {}, triggerReason: {}",
                stringKey,
                triggerReason
            )

            // 主动删除计算的 key
//        if(cacheImpl.hasKey(stringKey) == true){ // 使用 hasKey 来判断是有问题的，因为 hasKey 的结果在事务提交之后才会返回
            cacheImpl.delete(stringKey)
//        }
        }
    }
}

/**
 * 该 AssociateHandler 级联处理数据方式
 * - 如果 TriggerReason 是删除，则直接删除缓存
 * - 如果 TriggerReason 是新增或者变更，则更新缓存（如果缓存中存在）
 */
class DefaultAssociateHandler<WherePart : SifWherePart, T>(
    isPreferHandleInTransaction: Boolean = true,
    val getWherePartsToDeleteFunc: (T) -> Array<WherePart>
) : AbstractSifAssociateHandler<RedisOperations<String, Any>, WherePart, T>(isPreferHandleInTransaction) {

    private val logger: Logger = LoggerFactory.getLogger(DefaultRedisDeleteAssociateHandler::class.java)

    override fun handleAssociate(
        cacheImpl: RedisOperations<String, Any>,
        associateSifKey: SifKey<*, WherePart>,
        obj: T,
        triggerReason: TriggerReason
    ) {
        val wherePartsToDelete = getWherePartsToDeleteFunc(obj)
        wherePartsToDelete.forEach {
            val stringKey = associateSifKey.calcKey(it)

            logger.debug(
                "DefaultRedisReplaceIfPresentAssociateHandler, handleAssociate in, stringKey: {}, triggerReason: {}",
                stringKey,
                triggerReason
            )

            // 主动删除计算的 key
            if(triggerReason.isDelete()){
                cacheImpl.delete(stringKey)
            } else if(triggerReason.isCreateOrUpdate()){
                // TODO: FEATURE wangjie `缓存更新时间，默认是 -1（永不过期）` @ 2025-02-03 12:36:35
                cacheImpl.opsForValue().setIfPresent(stringKey, obj as Any)
            }
        }
    }
}

///**
// * 也可以针对不同的 SifKey 自定义 AssociateHandler
// */
//class SpaceDetailBlocksAssociateHandler :
//    AbstractSifAssociateHandler<RedisOperations<String, Any>, WhereByUserId, BioBlock>(true) {
//    private val logger: Logger = LoggerFactory.getLogger(SpaceDetailBlocksAssociateHandler::class.java)
//    override fun handleAssociate(
//        cacheImpl: RedisOperations<String, Any>,
//        associateSifKey: SifKey<*, WhereByUserId>,
//        obj: BioBlock,
//        triggerReason: TriggerReason
//    ) {
//        logger.debug("SpaceDetailBlocksAssociateHandler, handleAssociate, obj: {}", obj)
//
//        if(triggerReason.isCreateOrUpdate()) {
//            // ...
//        } else if(triggerReason.isDelete()) {
//            // ...
//        }
//
//        val stringKey = associateSifKey.calcKey(WhereByUserId(obj.ownerUserId))
////        if(cacheImpl.hasKey(stringKey)){
//            cacheImpl.delete(stringKey)  // 使用 hasKey 来判断是有问题的，因为 hasKey 的结果在事务提交之后才会返回
////        }
//    }
//}