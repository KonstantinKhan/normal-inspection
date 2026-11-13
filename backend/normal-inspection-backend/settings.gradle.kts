rootProject.name = "normal-inspection-backend"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
    versionCatalogs {
        create("ktorLibs").from("io.ktor:ktor-version-catalog:3.5.0")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
include(":transport")
include(":common")
include(":ktor-app")
include(":file-receiver")
include("normal-inspection-backend-doc-service")
include("normal-inspection-backend-file-storage")
include("normal-inspection-backend-odt-processor")
