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
                "Main-Class" to "saros.server.SarosServer",
                "Implementation-Version" to jarVersion
        ))
    }
    from(
            configurations.compile.get().map { if (it.isDirectory) it else zipTree(it) }
    )
    from("src/log4j.properties")

    /**
     * Temporary workaround for Log4J
     * upstream issue: https://issues.apache.org/jira/browse/LOG4J2-673
     */
    exclude("**/Log4j2Plugins.dat")

    exclude("**/*.jar")
}
