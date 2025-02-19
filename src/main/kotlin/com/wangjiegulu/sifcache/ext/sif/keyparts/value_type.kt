package com.wangjiegulu.sifcache.ext.sif.keyparts

import com.wangjiegulu.sifcache.ext.sif.SifEvent

/**
 * Key 第二部分：数据类型
 */
interface SifValueTypePart<ValueType> : SifKeyPart, SifEvent<ValueType>

class SifValueTypePartString<ValueType/* 数据类型 */>(
    private val keyPart: String
) : SifValueTypePart<ValueType> {
    override fun getKeyPart() = keyPart

    override fun getEventId() = keyPart

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SifValueTypePartString<*>

        return keyPart == other.keyPart
    }

    override fun hashCode(): Int {
        return keyPart.hashCode()
    }

}
