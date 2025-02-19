package com.wangjiegulu.sifcache.basic.response

import org.springframework.http.ResponseEntity


//val responseJsonMapper = JsonMapper.builder()
//    .also {
//        val module = JavaTimeModule()
//        module.addSerializer()
//        it.addModule(module)
//    }
//    .build()

fun ResponseEntity.BodyBuilder.successJsonBody(body: String): ResponseEntity<String> =
    header("Content-Type", "application/json;charset=UTF-8")
        .body(body)