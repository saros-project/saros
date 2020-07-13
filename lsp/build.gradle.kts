plugins {
    id("com.github.sherter.google-java-format") version "0.8"
}

val versionQualifier = ext.get("versionQualifier")

configurations {
    val testConfig by getting {}
    val testCompile by getting {
        extendsFrom(testConfig)
    }
}

dependencies {
    compile(project(":saros.core"))
    compile(project(":saros.server"))
    compile("org.apache.commons:commons-collections4:4.2")
    compile("org.eclipse.lsp4j:org.eclipse.lsp4j:0.8.1")
    compile("org.eclipse.lsp4j:org.eclipse.lsp4j.jsonrpc:0.8.1")
}

sourceSets {
    main {
        java.srcDirs("src")
    }
    test {
        java.srcDirs("test/junit")
    }
}

tasks.jar {
    val jarVersion = "0.1.0$versionQualifier"
    manifest {
        attributes(mutableMapOf(
                "Main-Class" to "saros.lsp.SarosLauncher",
                "Implementation-Version" to jarVersion
        ))
    }
    from(
            configurations.compile.get().map { if (it.isDirectory) it else zipTree(it) }
    )

    from("src/log4j.properties")
    exclude("**/*.jar")

    // Exclude files that prevent the jar from starting
    exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
}