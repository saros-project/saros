plugins {
  id("saros.gradle.eclipse.plugin")
}

val versionQualifier = (ext.get("versionQualifier") ?: "") as String
val eclipseVersionNr = ext.get("eclipseVersion") as String
val junitVersion = ext.get("junitVersion")

sarosEclipse {
    manifest = file("META-INF/MANIFEST.MF")
    excludeManifestDependencies = listOf("saros.core", "saros.eclipse", "org.junit", "org.eclipse.gef")
    isCreateBundleJar = true
    isAddDependencies = true
    pluginVersionQualifier = versionQualifier
    eclipseVersion = eclipseVersionNr
}

configurations {
    val releaseDep by getting {}
    val compile by getting {
        extendsFrom(releaseDep)
    }
}

dependencies {
    if (junitVersion != null) {
        compile(junitVersion)
    }
    compile(project(":saros.core"))
    compile(project(":saros.eclipse"))
    // This is a workaround for https://github.com/saros-project/saros/issues/1086
    implementation("org.eclipse.platform:org.eclipse.urischeme:1.1.0")
    // This is a workaround for https://github.com/saros-project/saros/issues/1138
    implementation("org.eclipse.platform:org.eclipse.e4.core.di:1.7.600")
    // This is a workaround for https://github.com/saros-project/saros/issues/1114
    implementation("org.eclipse.platform:org.eclipse.ui.ide:3.17.200")
    implementation("org.eclipse.platform:org.eclipse.ui.workbench:3.120.0")
	// This is a workaround for an Issues, same as https://github.com/saros-project/saros/issues/1114
	implementation("org.eclipse.platform:org.eclipse.e4.ui.services:1.3.700")
	implementation("javax.inject:javax.inject:1")
    compile(project(path = ":saros.eclipse", configuration = "testing"))

    releaseDep(fileTree("libs"))
}

sourceSets {
    main {
        java.srcDirs("src")
        resources.srcDirs("src")
        resources.exclude("**/*.java")
    }
    test {
        java.srcDirs("test")
    }
}

tasks {

    test {
        systemProperty("saros.stf.client.configuration.files", System.getProperty("stf.client.configuration.files", ""))
    }

    jar {
        from("plugin.xml")
        into("test/resources") {
            from("test/resources")
        }
    }

    val testJar by registering(Jar::class) {
        classifier = "tests"
        from(sourceSets["test"].output)
    }

    artifacts {
        add("testing", testJar)
    }
}
