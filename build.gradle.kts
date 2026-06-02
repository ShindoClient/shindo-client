plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.forge.gradle)
    alias(libs.plugins.mixin.gradle)
}

base {
    archivesName = "ShindoClient"
    version = "5111"
    group = "me.miki"
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

java {
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8

    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

kotlin {
    compilerOptions {
        jvmToolchain(8)
        freeCompilerArgs.add("-Xallow-no-source-files")
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

minecraft {
    version = "1.8.9"
    runDir = "run"
    mappings = "stable_22"
    makeObfSourceJar = false
    setTweakClass("me.miki.shindo.injection.mixin.ShindoTweaker")
    setMainClass("net.minecraft.launchwrapper.Launch")
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.spongepowered.org/maven/")
    maven("https://maven.cleanroommc.com")
}

val shade: Configuration by configurations.creating
configurations {
    val compile by creating {
        extendsFrom(shade)
    }
    getByName("implementation").extendsFrom(compile)
}

dependencies {
    "annotationProcessor"(libs.mixin)
    shade(libs.mixin) {
        exclude(module = "launchwrapper")
        exclude(module = "guava")
        exclude(module = "gson")
        exclude(module = "commons-io")
    }

    shade(libs.romaji)
    shade(libs.spotify) {
        exclude(group = "com.google.code.gson", module = "gson")
    }

    shade(libs.i18n)
    shade(libs.gson)
    shade(libs.yaml)
    shade(libs.mc.auth)
    shade(libs.bundles.okhttp)

    shade(files("libs/lwjgl-shindo.jar"))
    shade(files("libs/lwjgl-shindo-natives.jar"))

    shade(
        fileTree("libs/viashindo") {
            include(
                "ViaVersion*.jar",
                "ViaBackwards*.jar",
                "ViaRewind*.jar",
            )
        },
    )
}

mixin {
    defaultObfuscationEnv = "notch"
    add("main", "mixins.shindo.refmap.json")
}

tasks.processResources {
    inputs.files("src/main/resources")
    outputs.dir("build/classes/main")
    copy {
        from("src/main/resources")
        into("build/classes/main")
    }
}

tasks.jar {
    dependsOn(shade)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes(
            "MixinConfigs" to "mixins.shindo.json",
            "TweakClass" to "me.miki.shindo.injection.mixin.ShindoTweaker",
            "TweakOrder" to 0,
            "Manifest-Version" to "1.0",
            "FMLAT" to "shindo_at.cfg",
        )
    }
    from(shade.map { if (it.isDirectory()) it else zipTree(it) })
    exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
    exclude("META-INF/**")
    exclude("META-INF/versions/**")
    exclude("module-info.class")

    archiveBaseName.set("ShindoClient")
}
