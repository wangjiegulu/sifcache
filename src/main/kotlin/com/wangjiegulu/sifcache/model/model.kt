package com.wangjiegulu.sifcache.model

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.wangjiegulu.sifcache.ext.kt.NoArg


@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@NoArg
class BioBlock(
    val id: Long,
    val uuid: String,
    val ownerUserId: Long
){
    override fun toString(): String {
        return """
            {
                "id": ${id},
                "uuid": $uuid,
                "uid": $ownerUserId
            }
        """.trimIndent()
    }
}

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@NoArg
class LoginStatus(val token: String)

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@NoArg
class UserPremium

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@NoArg
class User(val userId: Long)