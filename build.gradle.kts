plugins {
    idea
    java
    id("com.github.johnrengelman.shadow") version "5.2.0" apply false
}

/*
 * Set <code>./gradlew -PuseBuildScan=true</code> to activate the build scan plugin
 * and service in order to visualize build/test failures.
 * The corresponding plugin is applied in the <code>settings.gradle.kts</code> file.
 */
val useBuildScan: String? by project
if (useBuildScan != null && useBuildScan.equals("true", true)) {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
        publishOnFailure()
    }
}

// Adjust Intellij module configuration for main project
idea {
    module {
        excludeDirs.addAll(mutableListOf(file("docs/.jekyll-cache"), file("docs/_site"), file("docs/node_modules")))
    }
}

/*
 * Apply default plugins and IntelliJ module configuration
 * for all sub-projects
 */
configure(subprojects) {
    apply(plugin = "idea")
    apply(plugin = "java")
    apply(plugin = "pmd")

    idea {
        module {
            excludeDirs.addAll(mutableListOf(file("bin"), file("lib"), file("libs")))
        }
    }
}

/*
 * Workaround: Applying the shadow plugin in picocontainer
 * leads to an error that says that "Project.afterEvaluate" cannot
 * be called in this context.
 */
configure(mutableListOf(project(":saros.picocontainer"))) {
    apply(plugin = "com.github.johnrengelman.shadow")
    apply(plugin = "eclipse")
}

val projectsToConfigure = subprojects - project(":saros.picocontainer")

configure(projectsToConfigure) {
    val projectToConf = this

    /*
     * Applied on all sub-projects, because the plugin adds the
     * dependency to the picocontainer project to the eclipse config.
     * If the picocontainer project is separated the gradle "eclipse"
     * plugin can be applied (and our plugin only for osgi sub-projects)
     */
    apply(plugin = "saros.gradle.eclipse.plugin")

    repositories {
        mavenCentral()
    }

    configurations {
        val testing by creating // used to reference the testJar
        val testConfig by creating // contains test dependencies that are used by all java subprojects
        val releaseDep by creating // contains all dependencies which has to be included into the release jar/zip
        releaseDep.isTransitive = true // avoid that the whole dependency tree is released
    }

    configure<PmdExtension> {
        toolVersion = "6.22.0"
        isConsoleOutput = true
        ruleSetFiles = files(rootProject.file("ruleset.xml"))
        ruleSets = emptyList<String>()
    }

    tasks {

        withType<Test> {

            // Otherwise our custom tags (as "@JTourBus") let the javadoc generation fail
            javadoc {
                isFailOnError = false
            }
            /* Exclude test suites if property is set. Otherwise tests are executed multiple times
             * in the ci server (via test class and suite).
             * see gradle.properties for default values
             */
            val skipTestSuites: String? by project
            if (skipTestSuites != null && skipTestSuites.equals("true", true)) {
                exclude("**/*TestSuite*")
            }

            /*
             * Exclude STF tests if property is set. Otherwise the STF self-tests (which are reliant on test
             * workers to function properly) are also run when executing the 'test' task to run all Saros
             * test.
             */
            val skipSTFTests: String? by project
            if (skipSTFTests != null && skipSTFTests.equals("true", true)) {
                exclude("saros/stf/test/stf/*")
            }

            // Don't execute abstract test classes
            exclude("**/Abstract*")

            testLogging {
                showStandardStreams = true
                events("passed", "skipped", "failed", "standardOut", "standardError")
            }
        }

        /* generate lib directory that contains all release dependencies
         * This is necessary to enable eclipse to run the stf tests, because
         * eclipse uses the path of the MANIFEST.MF and is not compatible with
         * gradle dependency resolution
         */
        register("generateLib", Copy::class) {
            into("${project.projectDir}/lib")
            from(projectToConf.configurations.getByName("releaseDep"))
        }

        val aggregateTestResults: String? by project
        if (aggregateTestResults != null && aggregateTestResults.equals("true", true)) {
            val test by getting {}
            val copyReport = register("copyReport", Copy::class) {
                from("build/reports/tests/test")
                into("$rootDir/build/reports/allTests/${projectToConf.name}")
            }
            test.finalizedBy(copyReport)
        }
    }

    /*
     * Make common dependency definitions accessible by all sub-projects
     */
    val junitVersion = "junit:junit:4.12"
    val log4JVersion = "log4j:log4j:1.2.15"
    projectToConf.extra["junitVersion"] = junitVersion
    projectToConf.extra["log4jVersion"] = log4JVersion

    dependencies {
        val testConfig by configurations
        testConfig(log4JVersion) {
            exclude(group = "com.sun.jmx", module = "jmxri")
            exclude(group = "com.sun.jdmk", module = "jmxtools")
            exclude(group = "javax.jms", module = "jms")
        }
        testConfig(junitVersion)
        testConfig("org.easymock:easymock:4.0.1")
        testConfig("org.powermock:powermock-core:2.0.5")
        testConfig("org.powermock:powermock-module-junit4:2.0.5")
        testConfig("org.powermock:powermock-api-easymock:2.0.5")
    }

    /*
     * Properties:
     * Set <code>./gradlew -PintellijHome=<path to IntelliJ></code> to use
     * a local installation for build and test.
     *
     * Set <code>./gradlew -PversionQualifier=<qualifier></code> to define
     * a version qualifier for CI build results.
     */
    val intellijHome: String? by project
    val versionQualifier: String? by project

    projectToConf.extra["intellijHome"] = intellijHome ?: System.getenv("INTELLIJ_HOME")
    projectToConf.extra["intellijSandboxDir"] = System.getenv("SAROS_INTELLIJ_SANDBOX") ?: ""
    projectToConf.extra["versionQualifier"] = if (versionQualifier.isNullOrBlank()) "" else ".$versionQualifier"
}

tasks {

    /* Internal Tasks (not intended to be called by users) */

    // generate all lib dirs in order to run stf tests
    register("generateLibAll") {
        dependsOn(
                "cleanGenerateLibAll",
                ":saros.core:generateLib",
                ":saros.eclipse:generateLib",
                ":saros.stf:generateLib",
                ":saros.stf.test:generateLib")
    }

    register("cleanGenerateLibAll") {
        doLast {
            project(":saros.eclipse").file("lib").deleteRecursively()
            project(":saros.core").file("lib").deleteRecursively()
            project(":saros.stf").file("lib").deleteRecursively()
            project(":saros.stf.test").file("lib").deleteRecursively()
        }
    }

    /* External Tasks */

    // TODO: Verify whether this task is used
    register("aggregatedTestReport", TestReport::class) {
        destinationDir = file("$buildDir/reports/allTests")
        val testProjects = (projectsToConfigure - project(":saros.stf") - project(":saros.stf.test"))
        reportOn(testProjects.map { it.tasks.getByName("test") })

        group = "Report"
        description = "Triggers the test task on all sub-projects and aggregates the report in a single report"
    }

    // remove all build dirs. The frontend package has no build directory
    val projectsToPrepare = projectsToConfigure + project(":saros.picocontainer")
    register("prepareEclipse") {
        dependsOn(
                projectsToPrepare.map { listOf(":${it.name}:cleanEclipseProject", ":${it.name}:cleanEclipseClasspath") }.flatten() +
                projectsToPrepare.map { listOf(":${it.name}:eclipseProject", ":${it.name}:eclipseClasspath") }.flatten() +
                listOf("generateLibAll")
        )

        group = "IDE"
        description = "Generates the 'libs' directories containing the " +
                "dependencies and generates the eclipse configurations for all projects"
    }

    register("cleanAll") {
        dependsOn(projectsToConfigure.map { ":${it.name}:clean" })

        group = "Build"
        description = "Utility task that calls 'clean' of all sub-projects"
    }

    register("sarosEclipse", Copy::class) {
        dependsOn(
                ":saros.picocontainer:test",
                ":saros.core:test",
                ":saros.eclipse:test",
                ":saros.eclipse:jar")

        group = "Build"
        description = "Builds and tests all modules required by Saros for Eclipse"

        from(project(":saros.core").tasks.findByName("jar"))
        from(project(":saros.eclipse").tasks.findByName("jar"))
        into("build/distribution/eclipse")
    }

    register("sarosStf", Copy::class) {
        dependsOn(
                "sarosEclipse",
                "saros.stf:jar"
        )

        from(project(":saros.stf").tasks.findByName("jar"))
        into("build/distribution/eclipse")
    }

    register("sarosServer", Copy::class) {
        dependsOn(
                ":saros.core:test",
                ":saros.server:test",
                ":saros.server:jar")

        group = "Build"
        description = "Builds and tests all modules required by the Saros Server"

        from(project(":saros.server").tasks.findByName("jar"))
        into("build/distribution/server")
    }
    register("sarosLsp", Copy::class) {
        dependsOn(
                ":saros.core:test",
                ":saros.lsp:test",
                ":saros.lsp:jar"
        )
        group = "Build"
        description = "Builds and tests all modules required by the Saros Language Server"

        from(project(":saros.lsp").tasks.findByName("jar"))
        into("build/distribution/lsp")
    }

    register("sarosIntellij", Copy::class) {
        dependsOn(
                ":saros.picocontainer:test",
                ":saros.core:test",
                ":saros.intellij:test",
                ":saros.intellij:buildPlugin"
        )
        group = "Build"
        description = "Builds and tests all modules required by Saros for Intellij"

        from(project(":saros.intellij").configurations.archives.get().artifacts.files)
        include("*.zip")
        into("build/distribution/intellij")
    }
}
