import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.0.21"
    java
}

group = "top.e404"
version = "1.1.3"

repositories {
    mavenLocal()
    // paper
    maven("https://repo.papermc.io/repository/maven-public/")
    // spigot (fallback)
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/groups/public/")
    mavenCentral()
}

dependencies {
    // paper api - 更新到最新可用版本
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
    // kotlin standard library - 需要显式包含以便打包
    implementation(kotlin("stdlib"))
    // bstats - 使用不需要重定位的版本
    implementation("org.bstats:bstats-base:3.1.0")
    implementation("org.bstats:bstats-bukkit:3.1.0")
}

tasks.withType<KotlinCompile> {
    compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.jar {
    archiveFileName.set("${project.name}-${project.version}.jar")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    
    // 包含所有依赖
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    
    // 排除不必要的META-INF文件
    exclude("META-INF/*.SF")
    exclude("META-INF/*.DSA")
    exclude("META-INF/*.RSA")
    exclude("META-INF/versions/**")
    exclude("META-INF/maven/**")
    
    doFirst {
        for (file in File("jar").listFiles() ?: arrayOf()) {
            println("正在删除`${file.name}`")
            file.delete()
        }
    }

    doLast {
        File("jar").mkdirs()
        for (file in File("build/libs").listFiles() ?: arrayOf()) {
            println("正在复制`${file.name}`")
            file.copyTo(File("jar/${file.name}"), true)
        }
    }
}

tasks {
    processResources {
        filesMatching("paper-plugin.yml") {
            expand(project.properties)
        }
    }
}