plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.coroutines)
    implementation(libs.logback.classic)

    implementation(projects.common)

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}