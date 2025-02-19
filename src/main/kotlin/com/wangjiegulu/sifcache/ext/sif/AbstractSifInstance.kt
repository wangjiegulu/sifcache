package com.wangjiegulu.sifcache.ext.sif

import com.wangjiegulu.sifcache.ext.sif.keyparts.SifWherePart
import jakarta.annotation.PostConstruct
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Sif 对象抽象实现
 */
abstract class AbstractSifInstance<CacheImpl>(
    private val cacheImpl: CacheImpl,
    private val sifKeysLoaders: List<SifKeysLoader>,
    val sifInstanceId: String
) : SifInstance {
    private val logger: Logger = LoggerFactory.getLogger(AbstractSifInstance::class.java)

    /**
     * 保存所有 sif key
     */
    private val sifKeys = hashMapOf<String/* SifBizPart */, SifKey<*, SifWherePart>>()

    @PostConstruct
    fun init() {
        sifKeysLoaders.forEach { loader ->
            val loadKeys = loader.loadKeys()
            if (null == loadKeys) { // 如果返回 null，则会通过反射来加载所有 loader 中的 SifKeys 变量
                injectAllKeyFields(loader)
            } else { // 优先使用 loadKeys 显式加载的 Keys
                loadKeys.forEach { key ->
                    register(key as SifKey<*, SifWherePart>) // 注册 key
                }
            }
        }
    }

    /**
     * 通过反射来加载所有 loader 中的 SifKeys 变量
     */
    private fun injectAllKeyFields(loader: SifKeysLoader) {
        logger.debug("injectAllKeyFields, loader: {}", loader)
        loader.javaClass.declaredFields.forEach { field ->
            field.isAccessible = true
            val fieldObj = field.get(loader)
            logger.debug("injectAllKeyFields, fieldObj: {}, name: {}", fieldObj, field.name)
            if(SifKey::class.java.isAssignableFrom(fieldObj.javaClass)){
                register(fieldObj as SifKey<*, SifWherePart>)
            }
        }
    }

    override fun register(sifKey: SifKey<*, SifWherePart>) {
        if (sifKeys.containsKey(sifKey.bizPart.getKeyPart())) {
            // TODO: FEATURE wangjie `throw exception` @ 2025-01-22 16:02:06
            logger.error("Register failure, ${sifKey.bizPart.getKeyPart()} is already registered")
            return
        }
        sifKeys[sifKey.bizPart.getKeyPart()] = sifKey
    }

    /**
     * 根据 bizPart 查询对应的 SifKey
     */
    fun getSifKey(sifBizPart: String): SifKey<*, SifWherePart>? {
        return sifKeys[sifBizPart]
    }

    fun getSifKeys(sifBizParts: List<String>): List<SifKey<*, SifWherePart>> {
        return sifBizParts.mapNotNull { sifKeys[it] }
    }

    /**
     * 触发一次级联处理
     */
    override fun <ValueType> triggerAssociateHandle(
        sifEvent: SifEvent<ValueType>,
        value: ValueType,
        triggerReason: TriggerReason,
        exceptSifKey: SifKey<*, *>? // 触发的时候排除掉指定的 key
    ) {
        try {
            // TODO: FEATURE wangjie `空值的时候如何级联处理？` @ 2025-01-21 17:37:19
            if (isSifValueBlank(value)) {
                logger.warn("triggerAssociateHandle, the value is blank, can not be handle associate.")
                return
            }

            // 级联处理依赖当前数据类型的缓存
            var associateKeys = searchAssociateSifKeys(sifEvent)

            // 排除掉当前的 key
            if(null != exceptSifKey){
                associateKeys = associateKeys.filter { exceptSifKey != it }
            }

            if (associateKeys.isEmpty()) {
                return
            }

            // 调用级联处理
            onDoAssociateHandle(
                sifEvent,
                value,
                triggerReason,
                associateKeys
            )
        } catch (e: Throwable) {
            logger.error("_triggerValue", e)
        }
    }

    /**
     * 实现级联处理逻辑
     */
    protected abstract fun <ValueType> onDoAssociateHandle(
        sifEvent: SifEvent<ValueType>,
        value: ValueType,
        triggerReason: TriggerReason,
        associateKeys: List<SifKey<*, SifWherePart>>
    )

    /**
     * 查询依赖当前数据类型的所有 key
     */
    private fun <ValueType> searchAssociateSifKeys(sifEvent: SifEvent<ValueType>): List<SifKey<*, SifWherePart>> {
        return sifKeys.values.filter {
            it.associateHandlers?.contains(sifEvent) ?: false
        }
    }
}