package com.wangjiegulu.sifcache.ext.sif.impl.redis.distributed

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.wangjiegulu.sifcache.ext.kt.NoArg
import com.wangjiegulu.sifcache.ext.sif.TriggerReason

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@NoArg
class SifMessage(
    val groupId: String,
    val senderSifInstanceId: String,
    val sifAssociateHandleMessage: SifAssociateHandleMessage
)

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@NoArg
class SifAssociateHandleMessage(
    val value: Any,
    val eventKey: String,
    val sifKeyBizParts: List<String>, // 需要级联处理的 SifKey 列表
    val triggerReason: TriggerReason
)