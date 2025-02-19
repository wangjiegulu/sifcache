package com.wangjiegulu.sifcache.app.sif

import com.wangjiegulu.sifcache_lib.SifInstance
import com.wangjiegulu.sifcache_lib.SifKeysLoader
import com.wangjiegulu.sifcache_lib.impl.redis.distributed.SifDistributedRedisInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.core.RedisTemplate

/**
 * 默认实例的 Qualifier
 */
const val QUALIFIER_SIF_INSTANCE_DEFAULT = "SIF_INSTANCE_DEFAULT"

@Configuration
class SifConfig {

    /**
     * 默认 SifInstance
     */
    @Primary
    @Bean
    fun defaultSifInstance(
        @Autowired cacheImpl: RedisTemplate<String, Any>,
        @Autowired @Qualifier(QUALIFIER_SIF_INSTANCE_DEFAULT) keysLoaders: List<SifKeysLoader>
    ): SifInstance {
        return SifDistributedRedisInstance(cacheImpl, keysLoaders)
//        return SifSingleRedisInstance(cacheImpl, keysLoaders)
    }
}