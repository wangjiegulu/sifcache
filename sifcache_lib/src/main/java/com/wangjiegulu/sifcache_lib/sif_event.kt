package com.wangjiegulu.sifcache_lib

/**
 * Author: wangjie
 * Email: tiantian.china.2@gmail.com
 * Date: 2025/1/26.
 */

/**
 * 级联处理的 event
 */
interface SifEvent<ValueType/* 事件的数据类型 */>{
    fun getEventId(): String
}

class SifEventString<ValueType/* 数据类型 */>(
    private val uniqueId: String
) : SifEvent<ValueType> {

    /**
     * Event unique id
     */
    override fun getEventId() = uniqueId
}