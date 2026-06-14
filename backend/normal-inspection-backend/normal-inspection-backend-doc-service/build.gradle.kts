plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

application {
    mainClass.set("com.example.doc.service.MainKt")
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(libs.odfdom.java)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}