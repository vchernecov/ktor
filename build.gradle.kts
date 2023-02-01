val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val flywayDbVersion: String = "7.5.1"
val hikariVersion: String = "3.4.5"
val jooqVersion: String = "3.17.1"
val postgresDriverVersion: String = "42.3.8"

plugins {
    kotlin("jvm") version "1.8.0"
    id("io.ktor.plugin") version "2.2.2"
    id("nu.studer.jooq") version "5.2.1"
    id("org.flywaydb.flyway") version "7.5.1"
}

group = "com.example"
version = "0.0.1"
application {
    mainClass.set("com.example.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-tomcat-jvm:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")

    api("org.jooq:jooq:$jooqVersion")
    api("com.zaxxer:HikariCP:$hikariVersion")
    api("org.flywaydb:flyway-core:$flywayDbVersion")
    api("org.postgresql:postgresql:$postgresDriverVersion")

    jooqGenerator("org.postgresql:postgresql:$postgresDriverVersion")

    testImplementation("io.ktor:ktor-server-tests-jvm:$kotlin_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}

flyway {
    url = "jdbc:postgresql://localhost:5432/entity"
    user = "postgres-ktor"
    password = "postgres-ktor"
}

jooq {
    version.set(jooqVersion)

    configurations {
        create("main") {
            generateSchemaSourceOnCompilation.set(false)
            jooqConfiguration.apply {
                jdbc.apply {
                    driver = "org.postgresql.Driver"
                    url = "jdbc:postgresql://localhost:5432/entity"
                    user = "postgres-ktor"
                    password = "postgres-ktor"
                }
                generator.apply {
                    name = "org.jooq.codegen.KotlinGenerator"
                    strategy.apply {
                        name = "org.jooq.codegen.DefaultGeneratorStrategy"
                    }
                    database.apply {
                        name = "org.jooq.meta.postgres.PostgresDatabase"
                        excludes = "flyway_schema_history"
                        inputSchema = "public"
                    }
                    generate.apply {
                        isRelations = true
                        isDeprecated = false
                        isRecords = true
                        isPojos = true
                        isPojosEqualsAndHashCode = true
                        isDaos = true
                    }
                    target.apply {
                        packageName = "com.database.generated"
                        directory = "src/generated/kotlin"
                    }
                }
            }
        }
    }
}

tasks.named<nu.studer.gradle.jooq.JooqGenerate>("generateJooq") {
    inputs.dir("${projectDir}/src/main/resources/db/migration")
    outputs.cacheIf { true }
    dependsOn("flywayMigrate")
}

