package com.wangjiegulu.sifcache.controller.aop

import com.wangjiegulu.sifcache.basic.response.successJsonBody
import jakarta.annotation.Resource
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import java.time.Duration

annotation class ControllerRedis(val value: String)

@Deprecated("")
@Component
@Aspect
class ControllerRedisAspect {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @Resource
    lateinit var redisTemplate: RedisTemplate<String, Any>

    @Around("@annotation(com.wangjiegulu.sifcache.controller.aop.ControllerRedis)")
    fun onControllerRedisAdvice(joinPoint: ProceedingJoinPoint): Any? {
        logger.debug("onControllerRedisAdvice before")

        if(joinPoint !is MethodInvocationProceedingJoinPoint){
            logger.warn("joinPoint !is MethodInvocationProceedingJoinPoint")
            return joinPoint.proceed()
        }

        val signature = joinPoint.signature
        if(signature !is MethodSignature){
            logger.warn("signature !is MethodSignature")
            return joinPoint.proceed()
        }

        val controllerRedis: ControllerRedis = signature.method.getAnnotation(ControllerRedis::class.java) ?: return joinPoint.proceed()

        if(controllerRedis.value.isBlank()){
            return joinPoint.proceed()
        }

        var prevResponseBody = redisTemplate.opsForValue().get(controllerRedis.value) as String?

        val logTag = "${joinPoint.target}-${signature.method.name}"

        if(null != prevResponseBody){
            logger.debug("onControllerRedisAdvice[${logTag}] has cache")
            return ResponseEntity.ok()
                .successJsonBody(prevResponseBody)
        }

        logger.debug("onControllerRedisAdvice[${logTag}] not cache")

        val responseEntity = joinPoint.proceed()

        if(responseEntity is ResponseEntity<*> && responseEntity.body is String){
            prevResponseBody = responseEntity.body as String
            redisTemplate.opsForValue().set(controllerRedis.value, prevResponseBody, Duration.ofMinutes(1))
        }

        logger.debug("onControllerRedisAdvice after")
        return responseEntity
    }

}