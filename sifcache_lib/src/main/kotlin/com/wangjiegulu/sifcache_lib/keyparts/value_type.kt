package com.wangjiegulu.sifcache_lib.keyparts

import com.wangjiegulu.sifcache_lib.SifEvent

/**
 * Key 第二部分：数据类型
 *
 * SifValueTypePart 实现了 SifEvent 接口，表示 SifValueTypePart 本身就属于 Event，所以根据 SifValueTypePart 获取到通知并进行级联处理
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
