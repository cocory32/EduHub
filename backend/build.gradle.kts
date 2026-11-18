plugins {
    java
    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.diffplug.spotless") version "7.0.4"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    // NFR-04: 코드에서 OpenAPI 스펙을 생성한다 (/v3/api-docs, /swagger-ui.html)
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9")
    runtimeOnly("org.postgresql:postgresql")

    // 테스트도 실제 PostgreSQL(로컬 eduhub_test)에 접속한다. 접속 정보는 .env의 TEST_DB_* 값
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// NFR-05: ./gradlew spotlessCheck 로 검사, ./gradlew spotlessApply 로 자동 정리
spotless {
    java {
        target("src/**/*.java")
        googleJavaFormat()
        removeUnusedImports()
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktlint()
    }
}
