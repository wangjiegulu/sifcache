package com.wangjiegulu.sifcache.sif_default.keys_cx

import com.wangjiegulu.sifcache.app.sif.QUALIFIER_SIF_INSTANCE_DEFAULT
import com.wangjiegulu.sifcache_lib.SifKeysLoader
import com.wangjiegulu.sifcache_lib.SifStringKey
import com.wangjiegulu.sifcache_lib.redis.DefaultRedisDeleteAssociateHandler
import com.wangjiegulu.sifcache_lib.keyparts.SifValueTypePartString
import com.wangjiegulu.sifcache_lib.makeHandler
import com.wangjiegulu.sifcache.model.BioBlock
import com.wangjiegulu.sifcache.sif_default.WhereByUserId
import com.wangjiegulu.sifcache.sif_default.keys_meta.SIF_VALUE_TYPE_MT__BLOCK
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

/////// DATA_TYPE 类型 2： 复杂类型  ///////
// 如：BO、List<X> /Set<X> / Map<X, Y> 等等
val SIF_VALUE_TYPE__BLOCKS = SifValueTypePartString<List<BioBlock>>("BKS")

@Component
@Qualifier(QUALIFIER_SIF_INSTANCE_DEFAULT)
object SifKeysCX : SifKeysLoader {

    // space detail blocks 缓存 //
    val KEY_SPACE_DETAIL_BLOCKS = SifStringKey<List<BioBlock>, WhereByUserId>(
        "SPD_BKS_A",
        SIF_VALUE_TYPE__BLOCKS,
        hashMapOf(
            makeHandler(SIF_VALUE_TYPE_MT__BLOCK, DefaultRedisDeleteAssociateHandler(false) {
                arrayOf(
                    WhereByUserId(it.ownerUserId)
                )
            })
        )
    )

    // space detail blocks 缓存(测试) //
    val KEY_SPACE_DETAIL_BLOCKS_B = SifStringKey<List<BioBlock>, WhereByUserId>(
        "SPD_BKS_B",
        SIF_VALUE_TYPE__BLOCKS,
        hashMapOf(
            makeHandler(SIF_VALUE_TYPE_MT__BLOCK, DefaultRedisDeleteAssociateHandler { arrayOf(WhereByUserId(it.ownerUserId)) })
        )
    )

}