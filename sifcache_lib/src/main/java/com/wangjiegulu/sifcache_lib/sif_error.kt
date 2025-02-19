package com.wangjiegulu.sifcache_lib

/**
 * 获取锁失败的异常，防止缓存击穿
 */
class SifCalcLockFailureException(message: String) : Exception(message)