plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"

    id("org.jetbrains.kotlin.plugin.noarg") version "1.9.25"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.9.25"

    id("org.springframework.boot") version "3.4.1"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.wangjiegulu"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

allOpen {
    annotation("com.wangjiegulu.sifcache_lib.ext.AllOpen")
}

noArg {
    annotation("com.wangjiegulu.sifcache_lib.ext.NoArg")
}

repositories {
    mavenCentral()
}

dependencies {
    // redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis") {
        exclude("io.lettuce", "lettuce-core")
    }
    implementation("redis.clients:jedis")

    // json
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("org.jetbrains.kotlin:kotlin-reflect")


    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.test {
    useJUnitPlatform()
}