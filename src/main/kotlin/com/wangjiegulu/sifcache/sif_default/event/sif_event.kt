package com.wangjiegulu.sifcache.sif_default.event

import com.wangjiegulu.sifcache_lib.SifEventString
import com.wangjiegulu.sifcache_lib.ext.SifPair

/**
 * 删除用户的事件类型
 */
val SIF_EVENT__DELETE_USER = SifEventString<SifPair<Long/* userId*/, String/* username */>>("SIF_EVENT__DELETE_USER")