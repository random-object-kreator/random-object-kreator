import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.10"
    maven
}

group = "memoizr"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("dom4j:dom4j:1.6.1")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.4.10")
    implementation("org.apache.commons:commons-lang3:3.5")
    implementation("org.javassist:javassist:3.27.0-GA")
    implementation("io.github.classgraph:classgraph:4.8.95")

    testImplementation("com.github.memoizr:assertk-core:-SNAPSHOT")
    testImplementation(kotlin("test-junit"))
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

