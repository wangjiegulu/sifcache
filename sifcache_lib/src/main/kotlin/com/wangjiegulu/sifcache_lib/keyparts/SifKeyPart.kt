package com.wangjiegulu.sifcache_lib.keyparts

/**
 * Sif Key 部分
 */
interface SifKeyPart {
    /**
     * 获取当前部分的 value，String 类型，用于拼接到 key 上
     */
    fun getKeyPart(): String
}