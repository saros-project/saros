plugins {
    id("saros.gradle.intellij.plugin")
}

val versionQualifier: String? = ext.get("versionQualifier") as String?
val intellijHome: String? = ext.get("intellijHome") as String?
val intellijSandboxDir: String? = ext.get("intellijSandboxDir") as String?

configurations {
    val testConfig by getting {}
    val testCompile by getting {
        extendsFrom(testConfig)
    }
}

dependencies {
    implementation(project(":saros.libratory", configuration = "plain"))
    compile(project(path = ":saros.core", configuration = "plain"))

    compile("org.easytesting:fest-assert:1.2")
    compile("org.easytesting:fest-reflect:1.2")
    compile("org.easytesting:fest-swing:1.2.1")
    compile("org.easytesting:fest-util:1.1.3")

    testImplementation(project(path = ":saros.libratory", configuration = "testing"))
    testCompile(project(path = ":saros.core", configuration = "testing"))
}

sourceSets {
    main {
        java.srcDirs("src")
        resources.srcDirs("resources", "src")
        resources.exclude("**/*.java")
    }

    test {
        java.srcDirs("test/junit")
    }
}

sarosIntellij {
    sandboxBaseDir = if (intellijSandboxDir.isNullOrBlank()) null else file(intellijSandboxDir!!)
    localIntellijHome = if (intellijHome.isNullOrBlank()) null else file(intellijHome!!)
    intellijVersion = "IC-2020.1.3"
}

tasks {
    jar {
        manifest {
            attributes(mutableMapOf(
                    "Created-By" to "IntelliJ IDEA",
                    "Manifest-Version" to "1.0"
            ))
        }
        version = ""

        from(sourceSets["main"].output)

        from(rootProject.file("saros_log4j2.xml"))
        from(rootProject.file("log4j2.xml"))
    }

    intellij {
        pluginName = "saros-intellij"
    }

    runIde {
        // set heap size for the test JVM(s)
        minHeapSize = "128m"
        maxHeapSize = "2048m"
        jvmArgs("-Dsaros.debug=true")
    }

    intellij {
        patchPluginXml {
            setVersion("0.3.0$versionQualifier")
        }
    }
}
