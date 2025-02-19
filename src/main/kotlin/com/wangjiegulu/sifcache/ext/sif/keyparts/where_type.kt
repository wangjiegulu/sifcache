package com.wangjiegulu.sifcache.ext.sif.keyparts

/**
 * SipKey 的条件部分（第三方部分）的每个条件对象，目前只支持相等 eq
 */
class SifWhereEqFragment(val key: String, val value: String)

/**
 * Key 第三部分：条件
 */
open class SifWherePart(private val whereFragments: ArrayList<SifWhereEqFragment> = ArrayList()) : SifKeyPart {
    override fun getKeyPart(): String {
        if (whereFragments.isEmpty()) {
            return ""
        }
        // 排序，保证相同条件生成的 key 是一样的
        whereFragments.sortBy { it.key }
        return whereFragments.joinToString("|") { "${it.key}|${it.value}" }
    }

    fun eq(key: String, value: String): SifWherePart {
        whereFragments.add(SifWhereEqFragment(key, value))
        return this
    }

}