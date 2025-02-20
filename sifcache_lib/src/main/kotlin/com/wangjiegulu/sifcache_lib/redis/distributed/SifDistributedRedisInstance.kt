package com.wangjiegulu.sifcache_lib.redis.distributed

import com.wangjiegulu.sifcache_lib.*
import com.wangjiegulu.sifcache_lib.redis.AbstractSifRedisInstance
import com.wangjiegulu.sifcache_lib.keyparts.SifWherePart
import com.wangjiegulu.sifcache_lib.SifAssociateHandler
import com.wangjiegulu.sifcache_lib.SifEvent
import com.wangjiegulu.sifcache_lib.SifKey
import com.wangjiegulu.sifcache_lib.TriggerReason
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.core.RedisOperations
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.SessionCallback
import java.util.UUID
import java.util.concurrent.Executors

/**
 * Redis 实现的 Sif
 * 使用订阅进行通知
 */
open class SifDistributedRedisInstance(
    cacheImpl: RedisTemplate<String, Any>,
    sifKeysLoaders: List<SifKeysLoader>,
    private val associateHandleChannel: String = "SIF_ASSOCIATE_HANDLE_CHANNEL_DEFAULT",
    sifInstanceId: String = UUID.randomUUID().toString()
) : AbstractSifRedisInstance(cacheImpl, sifKeysLoaders, sifInstanceId) {
    private val logger: Logger = LoggerFactory.getLogger(SifDistributedRedisInstance::class.java)

    /**
     * 订阅的线程池，目前 级联处理 是在同一个 channel一个 所以是单线程
     */
    private val subscriberThreadPool = Executors.newSingleThreadExecutor()

    init {
        start()
    }

    private fun start() {
        val connectionFactory = cacheImpl.connectionFactory ?: return
        subscriberThreadPool.execute {
            // 订阅之后当前线程会阻塞
            connectionFactory.connection.subscribe(
                { message, pattern ->
                    logger.debug("subscribe, onMessage()")
                    onSubscribedMessage(message, pattern)
                },
                // TODO: OPTIMIZE wangjie `需要分 Channel ?` @ 2025-01-23 17:16:59
                associateHandleChannel.toByteArray()
            )
        }
    }

    /**
     * 处理同步的数据变更消息
     */
    private fun onSubscribedMessage(message: Message, pattern: ByteArray?){
        val sifMessage: SifMessage
        try {
            sifMessage = cacheImpl.valueSerializer.deserialize(message.body) as SifMessage
        } catch (e: Exception) {
            logger.error("subscribe, onMessage() deserialize error", e)
            return
        }

        logger.debug("subscribe, onMessage(), \ngroupId: {}\nsender sifInstance: {}\nreceiver sifInstance: {}",
            sifMessage.groupId, sifMessage.senderSifInstanceId, sifInstanceId)

        val sifAssociateHandleMessage = sifMessage.sifAssociateHandleMessage

        val sifKeys = getSifKeys(sifAssociateHandleMessage.sifKeyBizParts)
        if(sifKeys.isEmpty()){
            return
        }

        // 直接调用处理的列表
        val handleDirectlyList =
            arrayListOf<Pair<SifKey<*, SifWherePart>, SifAssociateHandler<RedisOperations<String, Any>, SifWherePart, Any>>>()
        // 在事务中调用处理的列表
        val handleInTransactionList =
            arrayListOf<Pair<SifKey<*, SifWherePart>, SifAssociateHandler<RedisOperations<String, Any>, SifWherePart, Any>>>()

        // 两种方式调用统计
        sifKeys.forEach { associateKey ->
            val associateHandler = associateKey.associateHandlers?.firstNotNullOf {
                if (it.key.getEventId() == sifAssociateHandleMessage.eventKey) it else null
            }?.value ?: return

            try {
                associateHandler as SifAssociateHandler<RedisOperations<String, Any>, SifWherePart, Any>
                if (associateHandler.isPreferHandleInTransaction()) { // 要在事务中处理
                    handleInTransactionList.add(Pair(associateKey, associateHandler))
                } else { // 要直接调用处理
                    handleDirectlyList.add(Pair(associateKey, associateHandler))
                }
            } catch (e: Exception) {
                logger.error("subscribe, onMessage() cast failure", e)
            }
        }

        // 处理事务进行变更
        if (handleInTransactionList.isNotEmpty()) {
            logger.debug("subscribe, onMessage() handleInTransaction size: {}", handleInTransactionList.size)
            val executeResult = cacheImpl.execute(object : SessionCallback<List<Any>> {
                override fun <K : Any?, V : Any?> execute(operations: RedisOperations<K, V>): List<Any>? {
                    operations.multi()
                    handleInTransactionList.forEach {
                        try {
                            it.second.handleAssociate(
                                operations as RedisOperations<String, Any>,
                                it.first,
                                sifAssociateHandleMessage.value,
                                sifAssociateHandleMessage.triggerReason
                            )
                        } catch (e: Throwable) {
                            logger.error("subscribe, onMessage() handleInTransaction failure", e)
                        }
                    }
                    return operations.exec()
                }

            })

            logger.debug("subscribe, onMessage() handleInTransaction result: {}", executeResult)
        }

        // 直接调用进行变更
        if (handleDirectlyList.isNotEmpty()) {
            logger.debug("subscribe, onMessage() handleDirectly size: {}", handleDirectlyList.size)
            try{
                handleDirectlyList.forEach {
                    it.second.handleAssociate(
                        cacheImpl,
                        it.first,
                        sifAssociateHandleMessage.value,
                        sifAssociateHandleMessage.triggerReason
                    )
                }
            } catch (e: Throwable) {
                logger.error("subscribe, onMessage() handleDirectly failure", e)
            }
        }
    }

    /**
     * 级联处理实现
     */
    override fun <ValueType> onDoAssociateHandle(
        sifEvent: SifEvent<ValueType>,
        value: ValueType,
        triggerReason: TriggerReason,
        associateKeys: List<SifKey<*, SifWherePart>>
    ) {
        // 一次时间触发的级联处理都是相同的 groupId
        val groupId = UUID.randomUUID().toString()

        // 发送消息
        cacheImpl.convertAndSend(
            // TODO: OPTIMIZE wangjie `需要分 Channel ?` @ 2025-01-23 17:16:34
            associateHandleChannel,
            SifMessage(
                groupId,
                sifInstanceId,
                SifAssociateHandleMessage(
                    value as Any,
                    sifEvent.getEventId(),
                    // TODO: OPTIMIZE wangjie `分页批量更新，防止量过大` @ 2025-01-23 18:15:47
                    associateKeys.map { it.bizPart.getKeyPart() },
                    triggerReason
                )
            )
        )

    }

}