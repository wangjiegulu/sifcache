package com.wangjiegulu.sifcache.app.redis
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer


@Configuration
class RedisConfig {
    /**
     * Spring Boot（Kotlin）使用Redis的踩坑记录: https://blog.sukiu.top/Projects/Problems/Redis-Usage-With-Kotlin/
     */
    @Primary
    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory?): RedisTemplate<String, Any> {
        val redisTemplate = RedisTemplate<String, Any>()
        redisTemplate.connectionFactory = connectionFactory
        redisTemplate.setEnableTransactionSupport(true)

        // 设置键key的序列化方式
        val stringRedisSerializer = StringRedisSerializer()
        redisTemplate.keySerializer = stringRedisSerializer
        redisTemplate.hashKeySerializer = stringRedisSerializer

        // 设置值value的序列化方式
        val genericJackson2JsonRedisSerializer = getJsonRedisSerializer()
        redisTemplate.valueSerializer = genericJackson2JsonRedisSerializer
        redisTemplate.hashValueSerializer = genericJackson2JsonRedisSerializer
        redisTemplate.afterPropertiesSet()
        return redisTemplate
    }

    /**
     * 设置jackson的序列化方式
     */
    private fun getJsonRedisSerializer(): GenericJackson2JsonRedisSerializer {
        val objectMapper = ObjectMapper()
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        // fix: 查询缓存转换异常
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        val javaTimeModule = JavaTimeModule()
        objectMapper.registerModule(javaTimeModule)

        // 忽略所有序列化的 null 属性
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)

        // fix: class java.util.LinkedHashMap cannot be cast to class
        objectMapper.activateDefaultTyping(objectMapper.polymorphicTypeValidator, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        return GenericJackson2JsonRedisSerializer(objectMapper)
    }
}