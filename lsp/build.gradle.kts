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
    compile("org.apache.commons:commons-collections4:4.2")
    compile("org.eclipse.lsp4j:org.eclipse.lsp4j:0.9.0")
    compile("org.eclipse.lsp4j:org.eclipse.lsp4j.jsonrpc:0.9.0")
    compile("info.picocli:picocli:4.2.0")
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
            configurations.compile.get().map {if (it.isDirectory) it else zipTree(it) }
    )

    from(rootProject.file("saros_log4j2.xml"))
    from(rootProject.file("log4j2.xml"))
    exclude("**/*.jar")
    
    // Exclude files that prevent the jar from starting
    exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")

    // Exclude ambigious Log4j2Plugins.dat resulting from bug LOG4J2-954
    // see https://issues.apache.org/jira/browse/LOG4J2-954
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}