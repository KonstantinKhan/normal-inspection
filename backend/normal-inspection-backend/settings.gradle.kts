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
include("normal-inspection-backend-ktor-app")
include("normal-inspection-backend-doc-service")
include("normal-inspection-backend-file-receiver")
include("normal-inspection-backend-common")
include("normal-inspection-backend-file-storage")