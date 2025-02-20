package com.wangjiegulu.sifcache_lib

/**
 * Author: wangjie
 * Email: tiantian.china.2@gmail.com
 * Date: 2025/1/26.
 */

/**
 * 级联处理的 event，即 事件
 *
 * 1. SifValueTypePart 实现了 SifEvent 接口，表示 SifValueTypePart 本身就属于 Event，所以根据 SifValueTypePart 获取到通知并进行级联处理
 * 2. Event 也可以由用户自定义，实现缓存相关的事件通知
 */
interface SifEvent<ValueType/* 事件的数据类型 */>{
    fun getEventId(): String
}

/**
 * 默认字符串表示的事件
 */
class SifEventString<ValueType/* 数据类型 */>(
    // 事件唯一 ID
    private val uniqueId: String
) : SifEvent<ValueType> {

    /**
     * Event unique id
     */
    override fun getEventId() = uniqueId
}