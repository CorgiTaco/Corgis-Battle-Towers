plugins {
    id("com.github.johnrengelman.shadow")
}

architectury {
    platformSetupLoomIde()
    fabric()
}

val minecraftVersion = project.properties["minecraft_version"] as String

configurations {
    create("common")
    "common" {
        isCanBeResolved = true
        isCanBeConsumed = false
    }
    create("shadowBundle")
    compileClasspath.get().extendsFrom(configurations["common"])
    runtimeClasspath.get().extendsFrom(configurations["common"])
    getByName("developmentFabric").extendsFrom(configurations["common"])
    "shadowBundle" {
        isCanBeResolved = true
        isCanBeConsumed = false
    }
}

loom.accessWidenerPath.set(project(":common").loom.accessWidenerPath)


// Fabric Datagen Gradle config.  Remove if not using Fabric datagen
fabricApi.configureDataGeneration()

dependencies {
    modImplementation("net.fabricmc:fabric-loader:${project.properties["fabric_loader_version"]}")
    testImplementation("net.fabricmc:fabric-loader-junit:${project.properties["fabric_loader_version"]}")
    testImplementation(platform("org.junit:junit-bom:5.11.0"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.junit.platform:junit-platform-launcher")
    testImplementation("org.assertj:assertj-core:3.19.0")
    testImplementation("com.google.guava:guava-testlib:21.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.3.1")

    modApi("net.fabricmc.fabric-api:fabric-api:${project.properties["fabric_api_version"]}+$minecraftVersion")

    "common"(project(":common", "namedElements")) { isTransitive = false }
    "shadowBundle"(project(":common", "transformProductionFabric"))
    modApi("software.bernie.geckolib:geckolib-fabric-$minecraftVersion:${project.properties["geckolib_version"]}")
}


tasks.test {
    useJUnitPlatform()
    jvmArgs = listOf("-Xmx4096m")
}

sourceSets {
    test {
        compileClasspath += sourceSets.named("main").get().compileClasspath
        runtimeClasspath += sourceSets.named("main").get().runtimeClasspath
    }
}

tasks {
    processResources {
        inputs.property("version", project.version)

        filesMatching("fabric.mod.json") {
            expand(mapOf("version" to project.version))
        }
    }

    shadowJar {
        exclude("architectury.common.json")
        configurations = listOf(project.configurations.getByName("shadowBundle"))
        archiveClassifier.set("dev-shadow")
    }

    remapJar {
        injectAccessWidener.set(true)
        inputFile.set(shadowJar.get().archiveFile)
        dependsOn(shadowJar)
    }
}
