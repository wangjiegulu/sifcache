package com.wangjiegulu.sifcache.controller

import com.wangjiegulu.sifcache.basic.response.successJsonBody
import com.wangjiegulu.sifcache.controller.aop.ControllerRedis
import com.wangjiegulu.sifcache_lib.*
import com.wangjiegulu.sifcache.model.BioBlock
import com.wangjiegulu.sifcache.model.User
import com.wangjiegulu.sifcache.sif_default.WhereByBlockId
import com.wangjiegulu.sifcache.sif_default.WhereByUserId
import com.wangjiegulu.sifcache.sif_default.WhereByUsername
import com.wangjiegulu.sifcache.sif_default.cx.SifKeysCX
import com.wangjiegulu.sifcache.sif_default.event.SIF_EVENT__DELETE_USER
import com.wangjiegulu.sifcache.sif_default.meta.SifKeysMeta
import com.wangjiegulu.sifcache_lib.ext.SifPair
import jakarta.annotation.Resource
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID

@RestController
@RequestMapping("/redis")
class RedisTestController {

    companion object{
        // 测试 uid
        const val TEST_UID: Long = 1
        const val TEST_USERNAME: String = "zhangsan"
    }

    private val logger: Logger = LoggerFactory.getLogger(RedisTestController::class.java)

    @Resource
    lateinit var redisTemplate: RedisTemplate<String, Any>

    @Autowired
    lateinit var sifInstance: SifInstance

    @GetMapping("/get1")
    fun get1(): ResponseEntity<String> {
        var prevValue = redisTemplate.opsForValue().get("get1_key") as String?
        if(null == prevValue){
            Thread.sleep(150)
            prevValue = UUID.randomUUID().toString()
            // redisTemplate.keys -> 使用 scan
//            redisTemplate.delete(redisTemplate.keys(".*:block:.*"))
            redisTemplate.opsForValue().set("get1_key", prevValue, Duration.ofMinutes(1))
        }

        return ResponseEntity.ok()
            .successJsonBody(prevValue)
    }

    @ControllerRedis("get2_key")
    @GetMapping("/get2")
    fun get2(): ResponseEntity<String> {
        redisTemplate.opsForHash<String, Array<String>>()
        return ResponseEntity.ok()
            .successJsonBody(UUID.randomUUID().toString())
    }

    @GetMapping("/blocks_n")
    fun getBlocks(): ResponseEntity<String> {
        Thread.sleep(200)
        val get3Value = arrayListOf(
            BioBlock(123, UUID.randomUUID().toString(), TEST_UID),
            BioBlock(124, UUID.randomUUID().toString(), TEST_UID)
        )
        return ResponseEntity.ok()
            .successJsonBody(get3Value.toString())
    }

    @GetMapping("/blocks_a")
    fun getA(): ResponseEntity<String> {
        val get3Value = sifInstance.getOrCalc(
            SifKeysCX.KEY_SPACE_DETAIL_BLOCKS,
            WhereByUserId(TEST_UID),
            Duration.ofMinutes(1)
        ){
            Thread.sleep(200)
            arrayListOf(
                BioBlock(123, UUID.randomUUID().toString(), TEST_UID),
                BioBlock(124, UUID.randomUUID().toString(), TEST_UID)
            )
        }
        return ResponseEntity.ok()
            .successJsonBody(get3Value.toString())
    }

    @GetMapping("/blocks_a2")
    fun getA2(): ResponseEntity<String> {
        val get3Value = sifInstance.getOrCalc(
            SifKeysCX.KEY_SPACE_DETAIL_BLOCKS,
            WhereByUserId(TEST_UID),
            Duration.ofMinutes(1)
        ){
            Thread.sleep(200)
            arrayListOf(
                BioBlock(123, UUID.randomUUID().toString(), TEST_UID),
                BioBlock(124, UUID.randomUUID().toString(), TEST_UID)
            )
        }
        return ResponseEntity.ok()
            .successJsonBody(get3Value.toString())
    }

    @GetMapping("/blocks_b")
    fun getB(): ResponseEntity<String> {
        val get3Value = sifInstance.getOrCalc(
            SifKeysCX.KEY_SPACE_DETAIL_BLOCKS_B,
            WhereByUserId(TEST_UID),
            Duration.ofMinutes(1)
        ){
            Thread.sleep(200)
            arrayListOf(
                BioBlock(123, UUID.randomUUID().toString(), TEST_UID),
                BioBlock(125, UUID.randomUUID().toString(), TEST_UID)
            )
        }
        return ResponseEntity.ok()
            .successJsonBody(get3Value.toString())
    }

    @GetMapping("/setblock")
    fun set3(): ResponseEntity<String> {
        logger.debug("Block exist: {}", sifInstance.hasKey(SifKeysMeta.KEY_MT_BLOCK, WhereByBlockId(123)))

        val newBlock = BioBlock(123, UUID.randomUUID().toString(), TEST_UID)
        sifInstance.set(SifKeysMeta.KEY_MT_BLOCK, WhereByBlockId(123),
            newBlock,
            Duration.ofMinutes(1),
            { LocalDateTime.now().plusSeconds(40) }
        )
        return ResponseEntity.ok()
            .successJsonBody(newBlock.toString())
    }

    @GetMapping("/getblockornew")
    fun set4(): ResponseEntity<String> {
        val newBlock = sifInstance.getOrCalc(SifKeysMeta.KEY_MT_BLOCK, WhereByBlockId(123),
            Duration.ofMinutes(1)
        ){
//            Thread.sleep(5000)
            BioBlock(123, UUID.randomUUID().toString(), TEST_UID)
        }
        return ResponseEntity.ok()
            .successJsonBody(newBlock.toString())
    }

    @GetMapping("/getblockornull")
    fun set5(): ResponseEntity<String> {
        val block = sifInstance.getOrCalc(
            SifKeysMeta.KEY_MT_BLOCK,
            WhereByBlockId(123),
            Duration.ofMinutes(1)
        ){
//            Thread.sleep(5000)
            null
        }
        return ResponseEntity.ok()
            .successJsonBody(block?.toString() ?: "null")
    }

    @GetMapping("/deleteblock")
    fun deleteBlock(): ResponseEntity<String> {
        // 根据 key + 条件删除
        sifInstance.delete(SifKeysMeta.KEY_MT_BLOCK, WhereByBlockId(123))

        return ResponseEntity.ok()
            .successJsonBody("{}")
    }

    @GetMapping("/getOrCalcUserByUserId")
    fun getOrCalcUserByUserId(): ResponseEntity<String> {
        val user = sifInstance.getOrCalc(SifKeysMeta.KEY_MT_USER_BY_UID, WhereByUserId(TEST_UID)){
            User(TEST_UID, TEST_USERNAME, UUID.randomUUID().toString())
        }

        return ResponseEntity.ok()
            .successJsonBody(user.toString())
    }

    @GetMapping("/getOrCalcUserByUsername")
    fun getOrCalcUserByUsername(): ResponseEntity<String> {
        val user = sifInstance.getOrCalc(SifKeysMeta.KEY_MT_USER_BY_USERNAME, WhereByUsername(TEST_USERNAME)){
            User(TEST_UID, TEST_USERNAME, UUID.randomUUID().toString())
        }
        return ResponseEntity.ok()
            .successJsonBody(user.toString())
    }

    @GetMapping("/calcUserByUserId")
    fun calcUserByUserId(): ResponseEntity<String> {
        val user = User(TEST_UID, TEST_USERNAME, UUID.randomUUID().toString())
        sifInstance.set(SifKeysMeta.KEY_MT_USER_BY_UID, WhereByUserId(TEST_UID), user)
        return ResponseEntity.ok()
            .successJsonBody(user.toString())
    }

    // 不调用 delete 删除，而是通过通过 triggerAssociateHandle，使用 event 通知，让对应的 SifKey 来进行删除
    @GetMapping("/triggerDeleteUserByUserId")
    fun triggerDeleteUserByUserId(): ResponseEntity<String> {
        sifInstance.triggerAssociateHandle(SIF_EVENT__DELETE_USER, SifPair(TEST_UID, TEST_USERNAME), TriggerReason.DELETE)
        return ResponseEntity.ok()
            .successJsonBody("User $TEST_UID DELETED!")
    }
}