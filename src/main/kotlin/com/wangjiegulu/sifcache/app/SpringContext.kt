package com.wangjiegulu.sifcache.app

import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

@Component
@org.springframework.context.annotation.Lazy(false)
class SpringContext : ApplicationContextAware {

    override fun setApplicationContext(ac: ApplicationContext) {
        com.wangjiegulu.sifcache.app.SpringContext.Companion.applicationContext = ac
    }

    companion object {
        private var applicationContext: ApplicationContext? = null
            @Throws(BeansException::class)
            set(applicationContext) {
                field = applicationContext
            }

        fun getBean(name: String): Any {
            return com.wangjiegulu.sifcache.app.SpringContext.Companion.applicationContext!!.getBean(name)
        }

        fun <T> getBean(clazz: Class<T>): T {
            return com.wangjiegulu.sifcache.app.SpringContext.Companion.applicationContext!!.getBean(clazz)
        }

        fun <T> getBean(name: String, clazz: Class<T>): T {
            return com.wangjiegulu.sifcache.app.SpringContext.Companion.applicationContext!!.getBean(name, clazz)
        }
    }


}