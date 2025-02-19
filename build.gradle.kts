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
	annotation("com.wangjiegulu.sifcache.ext.AllOpen")
}

noArg {
	annotation("com.wangjiegulu.sifcache.ext.NoArg")
}

repositories {
	mavenCentral()
}

dependencies {
	// spring boot
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-validation")

	implementation(project(":sifcache_lib"))

	// redis
	implementation("org.springframework.boot:spring-boot-starter-data-redis") {
		exclude("io.lettuce", "lettuce-core")
	}
	implementation("redis.clients:jedis")

	implementation("org.springframework.boot:spring-boot-starter-aop")

	// json
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
	implementation("org.jetbrains.kotlin:kotlin-reflect")

	// 数据库
//	implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.3")
//	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
//	implementation("org.flywaydb:flyway-core")
//	implementation("org.flywaydb:flyway-mysql")
//	runtimeOnly("com.mysql:mysql-connector-j")

	implementation("org.apache.commons:commons-pool2") // need by redis

	// util
	runtimeOnly("org.aspectj:aspectjweaver:1.9.22.1")
	implementation("commons-codec:commons-codec:1.17.1")


	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
