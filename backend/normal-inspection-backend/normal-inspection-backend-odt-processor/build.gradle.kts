plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.woodstox)

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}