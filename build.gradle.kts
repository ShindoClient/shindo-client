import org.jetbrains.kotlin.gradle.dsl.JvmTarget

buildscript {
    repositories {
        mavenCentral()

        maven {
            name = "forge"
            url = uri("https://maven.minecraftforge.net")
            // url = uri("http://files.minecraftforge.net/maven")
        }

        maven {
            name = "sponge"
            url = uri("https://repo.spongepowered.org/maven")
        }

        maven {
            url = uri("https://jitpack.io")
        }
    }

    dependencies {
        classpath("com.github.MikiDevAHM:ForgeGradle:50e3574")
        classpath("com.github.thefightagainstmalware:MixinGradle:92e66fe")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.0")
    }
}

var kotlin_version = "2.2.0"

apply(plugin = "net.minecraftforge.gradle.tweaker-client")
apply(plugin = "org.spongepowered.mixin")
apply(plugin = "kotlin")
apply(plugin = "java")
apply(plugin = "idea")

version = "5111"
group = "me.miki"

configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}


// Kotlin target
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
        freeCompilerArgs.set(listOf("-Xallow-no-source-files"))
    }
}

configure<net.minecraftforge.gradle.user.tweakers.TweakerExtension>  {
    version = "1.8.9"
    runDir = "run"

    setTweakClass("me.miki.shindo.injection.mixin.ShindoTweaker")
    setMainClass("net.minecraft.launchwrapper.Launch")

    mappings = "stable_22"
    makeObfSourceJar = false
}

repositories {
    mavenCentral()

    maven {
        name = "Jitpack"
        url = uri("https://jitpack.io")
    }

    maven {
        name = "SpongePowered"
        url = uri("https://repo.spongepowered.org/maven/")
    }

    maven {
        url = uri("https://maven.cleanroommc.com")
    }
}

val shade: Configuration by configurations.creating
configurations {
    val compile by creating {
        extendsFrom(shade)
    }
    getByName("implementation").extendsFrom(compile)
}

configurations.all {
    resolutionStrategy {
        force("org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version")
    }
}

dependencies {
    shade("org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version")

    "annotationProcessor"("org.spongepowered:mixin:0.7.11-SNAPSHOT")
    shade("org.spongepowered:mixin:0.7.11-SNAPSHOT") {
        exclude(module = "launchwrapper")
        exclude(module = "guava")
        exclude(module = "gson")
        exclude(module = "commons-io")
    }

    shade("com.github.jikyo:romaji:0.0.4")
    shade("se.michaelthelin.spotify:spotify-web-api-java:6.5.4") {
        exclude(group = "com.google.code.gson", module = "gson")
    }

    shade("com.neovisionaries:nv-i18n:1.29")
    shade("com.squareup.okhttp3:okhttp:3.14.9")
    shade("com.google.code.gson:gson:2.13.1")

    shade(files("libs/lwjgl-shindo.jar"))
    shade(files("libs/lwjgl-shindo-natives.jar"))

    shade(
        fileTree("libs/viashindo") {
            include(
                "ViaVersion*.jar",
                "ViaBackwards*.jar",
                "ViaRewind*.jar",
                "snakeyaml*.jar"
            )
        }
    )
}
dependencyLocking {
    lockAllConfigurations()
}

configure<org.spongepowered.asm.gradle.plugins.MixinExtension> {
    defaultObfuscationEnv = "notch"
    add("main", "mixins.shindo.refmap.json")
}


tasks.withType<ProcessResources> {
    inputs.files("src/main/resources")
    outputs.dir("build/classes/main")
    copy {
        from("src/main/resources")
        into("build/classes/main")
    }
}

tasks.withType(Jar::class) {

    dependsOn(shade)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes(
            "MixinConfigs" to "mixins.shindo.json",
            "TweakClass" to "me.miki.shindo.injection.mixin.ShindoTweaker",
            "TweakOrder" to 0,
            "Manifest-Version" to "1.0",
            "FMLAT" to "shindo_at.cfg"
        )
    }
    from(shade.map { if(it.isDirectory()) it else zipTree(it)})
    exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
    exclude("META-INF/**")
    exclude("META-INF/versions/**")
    exclude("module-info.class")

    archiveBaseName.set("ShindoClient")
}