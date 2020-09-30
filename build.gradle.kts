import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.10"
    application
    id("org.openjfx.javafxplugin") version "0.0.8"
}
group = "me.cisimon7"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://dl.bintray.com/kyonifer/maven")
    jcenter()
}

javafx {
    version = "11.0.2"
    modules("javafx.controls", "javafx.graphics")
}

dependencies {
    implementation("no.tornado:tornadofx:1.7.20")
    implementation("io.data2viz:d2v-data2viz-jfx:0.7.2-RC1")
    implementation("org.nield:kotlin-statistics:1.2.1")

    implementation("com.kyonifer:koma-core-ejml:0.12")

    testImplementation(kotlin("test-junit5"))
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}
application {
    mainClassName = "MainKt"
}