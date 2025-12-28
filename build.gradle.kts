plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.2"
}

group = "com.plucky"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    // ASM for class file parsing
    implementation("org.ow2.asm:asm:9.6")
    implementation("org.ow2.asm:asm-util:9.6")
    implementation("org.ow2.asm:asm-tree:9.6")

    // PlantUML for diagram generation (离线可用)
    // PlantUML包含所有必要的依赖，可以完全离线运行
    implementation("net.sourceforge.plantuml:plantuml:1.2024.0")

    // 注意：PlantUML默认使用Graphviz进行布局，但也支持纯Java的Smetana布局引擎
    // Smetana是PlantUML内置的，不需要额外依赖，完全离线可用

    testImplementation("junit:junit:4.13.2")
}

// Configure Gradle IntelliJ Plugin
intellij {
    version.set("2023.1")
    type.set("IC") // IntelliJ IDEA Community Edition
    plugins.set(listOf("java"))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
        options.encoding = "UTF-8"
    }

    patchPluginXml {
        sinceBuild.set("231")
        untilBuild.set("241.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
