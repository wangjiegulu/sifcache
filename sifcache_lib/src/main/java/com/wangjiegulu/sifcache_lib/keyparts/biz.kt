package com.wangjiegulu.sifcache_lib.keyparts

/**
 * Key 第一部分：业务域
 */
interface SifBizPart : SifKeyPart

/**
 * 字符串类型
 */
class SifBizPartString(private val keyPart: String) : SifBizPart {
    override fun getKeyPart() = keyPart
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SifBizPartString

        return keyPart == other.keyPart
    }

    override fun hashCode(): Int {
        return keyPart.hashCode()
    }


}