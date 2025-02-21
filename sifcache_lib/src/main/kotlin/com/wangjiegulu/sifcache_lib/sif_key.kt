package com.wangjiegulu.sifcache_lib

import com.wangjiegulu.sifcache_lib.keyparts.SifBizPart
import com.wangjiegulu.sifcache_lib.keyparts.SifBizPartString
import com.wangjiegulu.sifcache_lib.keyparts.SifValueTypePart
import com.wangjiegulu.sifcache_lib.keyparts.SifWherePart
import java.time.Duration

// Key 缓存的默认 timeout
private val DEFAULT_KEY_TIMEOUT = Duration.ofMinutes(60)

/**
 * SIF Key 枚举
 */
open class SifKey<ValueType, WherePart : SifWherePart>(
    // 第一部分：业务（要唯一）
    val bizPart: SifBizPart,

    // 第二部分：数据类型
    val valueTypePart: SifValueTypePart<ValueType>,

    // 当前 key 默认的过期时间
    val defaultTimeout: Duration = DEFAULT_KEY_TIMEOUT,

    // 依赖的相关数据类型及对应的处理器（根据该列表来进行级联处理）
//    val associateHandlers: Map<SifEvent<*>, SifAssociateHandler<*, *, *>>? = null,
    createAssociateHandlers: (() -> Map<SifEvent<*>, SifAssociateHandler<*, *, *>>?)? = null
){
    var associateHandlers: Map<SifEvent<*>, SifAssociateHandler<*, *, *>>? = null
    init {
        createAssociateHandlers?.let {
            associateHandlers = it()
        }
    }
    /**
     * 计算 key
     * 该 Key 在 redis 等中表示唯一的 Key
     */
    fun calcKey(sifDataWherePart: WherePart) = "${bizPart.getKeyPart()}:${valueTypePart.getKeyPart()}:${sifDataWherePart.getKeyPart()}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SifKey<*, *>

        return bizPart == other.bizPart
    }

    override fun hashCode(): Int {
        return bizPart.hashCode()
    }


}

/**
 * String 类型 event
 */
class SifStringKey<ValueType, WherePart : SifWherePart>(
    bizPartName: String,
    valueTypePart: SifValueTypePart<ValueType>,
    defaultTimeout: Duration = DEFAULT_KEY_TIMEOUT,
    createAssociateHandlers: (() -> Map<SifEvent<*>, SifAssociateHandler<*, *, *>>?)? = null
) : SifKey<ValueType, WherePart>(SifBizPartString(bizPartName), valueTypePart, defaultTimeout, createAssociateHandlers)

/**
 * SifKey 加载器
 */
interface SifKeysLoader {
    /**
     * 显式加载 Keys
     * 如果返回 null，则会通过反射来加载所有 loader 中的 SifKeys 变量
     */
    fun loadKeys(): List<SifKey<*, *>>? = null
}