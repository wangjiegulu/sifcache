package com.wangjiegulu.sifcache.sif_default.keys_meta

import com.wangjiegulu.sifcache.app.sif.QUALIFIER_SIF_INSTANCE_DEFAULT
import com.wangjiegulu.sifcache_lib.SifKeysLoader
import com.wangjiegulu.sifcache_lib.SifStringKey
import com.wangjiegulu.sifcache_lib.keyparts.SifValueTypePartString
import com.wangjiegulu.sifcache.model.BioBlock
import com.wangjiegulu.sifcache.model.LoginStatus
import com.wangjiegulu.sifcache.model.User
import com.wangjiegulu.sifcache.sif_default.WhereByBlockId
import com.wangjiegulu.sifcache.sif_default.WhereByToken
import com.wangjiegulu.sifcache.sif_default.WhereByUserId
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

/////// DATA_TYPE 类型 1： META 数据类型(model) ///////
val SIF_VALUE_TYPE_MT__LOGIN_STATUS = SifValueTypePartString<LoginStatus>("LG_ST")
val SIF_VALUE_TYPE_MT__USER = SifValueTypePartString<User>("UR")
val SIF_VALUE_TYPE_MT__BLOCK = SifValueTypePartString<BioBlock>("BK")

@Component
@Qualifier(com.wangjiegulu.sifcache.app.sif.QUALIFIER_SIF_INSTANCE_DEFAULT)
object SifKeysMeta : SifKeysLoader {
    // Block 缓存 //
    val KEY_MT_BLOCK = SifStringKey<BioBlock, WhereByBlockId>(
        "MT_BK",
        SIF_VALUE_TYPE_MT__BLOCK,
    )

    // 登录态缓存 //
    val KEY_MT_LOGIN_STATUS_BY_TOKEN = SifStringKey<LoginStatus, WhereByToken>(
        "MT_LSBT",
        SIF_VALUE_TYPE_MT__LOGIN_STATUS
    )

    // 用户信息缓存 //
    val KEY_MT_USER = SifStringKey<User, WhereByUserId>(
        "MT_UR",
        SIF_VALUE_TYPE_MT__USER
    )
}