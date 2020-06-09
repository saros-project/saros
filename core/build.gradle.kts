val versionQualifier = ext.get("versionQualifier") as String

configurations {
    // Defined in root build.gradle
    val testConfig by getting {}
    val releaseDep by getting {}

    // Default configuration
    val compile by getting {
        extendsFrom(releaseDep)
    }
    val testCompile by getting {
        extendsFrom(testConfig)
    }
    val plain by creating {
        extendsFrom(compile)
    }
}

sarosEclipse {
    manifest = file("META-INF/MANIFEST.MF")
    isCreateBundleJar = true
    isAddPdeNature = true
    pluginVersionQualifier = versionQualifier
}

dependencies {
    releaseDep("commons-codec:commons-codec:1.3")
    releaseDep("commons-io:commons-io:2.0.1")
    releaseDep("org.apache.commons:commons-lang3:3.8.1")

    releaseDep("javax.jmdns:jmdns:3.4.1")
    releaseDep(project(path = ":saros.picocontainer", configuration = "shadow"))
    releaseDep("xpp3:xpp3:1.1.4c")
    releaseDep("com.thoughtworks.xstream:xstream:1.4.10")
    releaseDep("org.gnu.inet:libidn:1.15")

    releaseDep("log4j:log4j:1.2.15") {
        exclude(group = "com.sun.jmx", module = "jmxri")
        exclude(group = "com.sun.jdmk", module = "jmxtools")
        exclude(group = "javax.jms", module = "jms")
    }

    // TODO: use real release. This version is a customized SNAPSHOT
    releaseDep(files("libs/weupnp.jar"))
    // Workaround until we updated to a newer smack version
    releaseDep(files("libs/smack-3.4.1.jar"))
    releaseDep(files("libs/smackx-3.4.1.jar"))
}

sourceSets {
    main {
        java.srcDirs("src", "patches")
        resources.srcDirs("resources")
    }
    test {
        java.srcDirs("test/junit")
    }
}

tasks {

    val testJar by registering(Jar::class) {
        classifier = "tests"
        from(sourceSets["test"].output)
    }

    // Jar containing only the core code (the default jar is an osgi bundle
    // containing a lib dir with all dependency jars)
    val plainJar by registering(Jar::class) {
        manifest {
            from("META-INF/MANIFEST.MF")
        }
        from(sourceSets["main"].output)
        classifier = "plain"
    }

    artifacts {
        add("testing", testJar)
        add("plain", plainJar)
    }
}