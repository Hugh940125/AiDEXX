plugins {
    `java-gradle-plugin`
    `maven-publish`
    kotlin("jvm")
}

group = "com.microtech.plugins"
version = "1.0.0"

gradlePlugin {
    plugins {
        create("resourcesId") {
            id = "com.microtech.plugins.resourcesId"
            implementationClass = "com.microtech.plugins.ResourcesIdPlugin"
            description = "固定资源id"
        }
    }
}

publishing {
    repositories {
        maven {
            url = uri("maven-repo")
        }
    }
}



dependencies {
    implementation(gradleApi())
    testImplementation("junit:junit:4.13.2")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.8.0")
    compileOnly("com.android.tools.build:gradle:7.3.0")
}
