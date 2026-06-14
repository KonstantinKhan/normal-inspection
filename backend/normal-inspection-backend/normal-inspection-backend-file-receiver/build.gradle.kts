plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.coroutines)

    implementation(projects.normalInspectionBackendCommon)
    implementation(projects.normalInspectionBackendFileStorage)

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}