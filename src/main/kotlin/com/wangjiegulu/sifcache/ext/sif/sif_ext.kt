package com.wangjiegulu.sifcache.ext.sif

import com.wangjiegulu.sifcache.ext.sif.keyparts.SifWherePart

///**
// * 把 SifKey 注册到 SifInstance 中
// */
//fun <ValueType, WherePart : SifWherePart> SifKey<ValueType, WherePart>.register(sifInstance: SifInstance): SifKey<ValueType, WherePart> {
//    sifInstance.register(this as SifKey<ValueType, SifWherePart>)
//    return this
//}

fun <ValueType, WherePart : SifWherePart> makeHandler(
    sifEvent: SifEvent<ValueType>,
    sifAssociateHandler: SifAssociateHandler<*, WherePart, ValueType>
): Pair<SifEvent<*>, SifAssociateHandler<*, *, *>> = Pair(sifEvent, sifAssociateHandler)

enum class SetCondition {
    FORCE, // 强制，不论当前是否存在都进行设置
    IF_ABSENT, // 如果不存在，则设置
    IF_PRESENT // 如果存在，则设置
    ;

    fun isIfAbsent() = IF_ABSENT == this

    fun isIfPresent() = IF_PRESENT == this
}

/**
 * Value 是否为空（防止缓存穿透）
 */
fun isSifValueBlank(value: Any?): Boolean {
    if(null == value){
        return false
    }
    return value is String && (SIF_VALUE_BLANK == value || "null" == value.lowercase())
}

/**
 * value 为 null 时，redis 中缓存的数据为空字符串
 * 防止缓存穿透，如果不存在则保存为空字符串
 */
const val SIF_VALUE_BLANK = ""