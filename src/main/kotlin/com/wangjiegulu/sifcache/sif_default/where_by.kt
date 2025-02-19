package com.wangjiegulu.sifcache.sif_default

import com.wangjiegulu.sifcache.ext.sif.keyparts.SifWhereEqFragment
import com.wangjiegulu.sifcache.ext.sif.keyparts.SifWherePart

//////////// Where 条件类 ////////////
class WhereByUserId(
    userId: Long
): SifWherePart(arrayListOf(
    SifWhereEqFragment("uid", "$userId")
))
class WhereByBlockId(
    blockId: Long
): SifWherePart(arrayListOf(
    SifWhereEqFragment("bid", "$blockId")
))

class WhereByToken(
    token: String
): SifWherePart(arrayListOf(
    SifWhereEqFragment("token", token)
))