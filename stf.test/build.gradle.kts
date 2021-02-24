plugins {
    id("org.gradle.test-retry") version "1.1.5"
    id("saros.gradle.eclipse.plugin")
}

val eclipseVersionNr = ext.get("eclipseVersion") as String

val commonsLang = ext.get("commons-lang3") as String

sarosEclipse {
    manifest = file("META-INF/MANIFEST.MF")
    excludeManifestDependencies = listOf("org.junit", "saros.eclipse", "saros.core")
    isAddDependencies = true
    eclipseVersion = eclipseVersionNr
}

configurations {
    val implementation by getting {}
    val testConfig by getting {}
    // TODO: The custom saros eclipse plugin adds all dependencies to implementation
    val stfTestCompile by creating {
        extendsFrom(implementation, testConfig)
    }
    val testCompile by getting {
        extendsFrom(stfTestCompile)
    }
}

dependencies {
    implementation(commonsLang)
    val stfTestCompile by configurations
    stfTestCompile(project(":saros.stf"))
    stfTestCompile(project(path = ":saros.stf", configuration = "testing"))
}

sourceSets {
    create("stfTest") {
        java.srcDirs("test")
    }
}

open class StfTest : Test() {
    init {
        group = "Verification"
        description = ("Runs the stf tests. Requires a corresponding test environment")
        systemProperty("saros.stf.client.configuration.files", System.getProperty("stf.client.configuration.files", ""))

        testLogging(org.gradle.api.Action<org.gradle.api.tasks.testing.logging.TestLoggingContainer> {
            showStandardStreams = true
            setEvents(listOf("started", "passed", "skipped", "failed", "standardOut", "standardError"))
        })


        val failedTests = mutableListOf<String>()

        // Workaround: https://github.com/gradle/gradle/issues/5431
        afterTest(KotlinClosure2<TestDescriptor, TestResult, Void>({ descriptor, result ->
            if (result.resultType == TestResult.ResultType.FAILURE) {
                failedTests.add("${descriptor.className}.${descriptor.name}")
            }
            null
        }))

        this.afterSuite(KotlinClosure2<TestDescriptor, TestResult, Void>({ suite, result ->
            if (suite.parent == null && failedTests.isNotEmpty()) {
                logger.lifecycle("Failed tests:")
                failedTests.forEach { logger.lifecycle(it) }
            }
            null
        }))

        include("**/stf/test/**/*Test.*")
        exclude("**/stf/test/stf/**")
    }
}
tasks {
    register("stfTest", StfTest::class) {
        dependsOn(":saros.eclipse:build")

        description = "Executes the STF tests"

        testClassesDirs = sourceSets["stfTest"].output.classesDirs
        classpath = sourceSets["stfTest"].runtimeClasspath

        useJUnit {
            excludeCategories = mutableSetOf("saros.stf.test.categories.FlakyTests")
        }

        retry {
            failOnPassedAfterRetry.set(false)
            maxFailures.set(10)
            maxRetries.set(3)
        }
    }

    register("stfFlakyTest", StfTest::class) {
        dependsOn(":saros.eclipse:build")

        description = "Executes the STF tests marked as Flaky"

        testClassesDirs = sourceSets["stfTest"].output.classesDirs
        classpath = sourceSets["stfTest"].runtimeClasspath
        ignoreFailures = true

        useJUnit {
            includeCategories = mutableSetOf("saros.stf.test.categories.FlakyTests")
        }
    }
}
