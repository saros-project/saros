plugins {
    id("saros.gradle.eclipse.plugin")
    id("com.diffplug.p2.asmaven") // Provides the class FeaturesAndBundlesPublisher
}

import com.diffplug.gradle.p2.FeaturesAndBundlesPublisher
import com.diffplug.gradle.p2.CategoryPublisher
import com.diffplug.gradle.pde.EclipseRelease

val versionQualifier = ext.get("versionQualifier") as String
val eclipseVersionNr = ext.get("eclipseVersion") as String

configurations {
    val testConfig by getting {}
    getByName("testImplementation") {
        extendsFrom(testConfig)
    }
}

sarosEclipse {
    manifest = file("META-INF/MANIFEST.MF")
    excludeManifestDependencies = listOf("saros.core", "org.junit", "org.eclipse.gef")
    isAddPdeNature = true
    isCreateBundleJar = true
    isAddDependencies = true
    pluginVersionQualifier = versionQualifier
    eclipseVersion = eclipseVersionNr
}

sourceSets {
    main {
        java.srcDirs("src", "ext-src")
        resources.srcDirs("src")
        resources.exclude("**/*.java")
    }
    test {
        java.srcDirs("test/junit")
    }
}

dependencies {
    implementation(project(":saros.core"))
    // This is a workaround for https://github.com/saros-project/saros/issues/1086
    implementation("org.eclipse.platform:org.eclipse.urischeme:1.1.0")
    // This is a workaround for https://github.com/saros-project/saros/issues/1114
    implementation("org.eclipse.platform:org.eclipse.ui.ide:3.17.200")
    implementation("org.eclipse.platform:org.eclipse.ui.workbench:3.120.0")
	// This is a workaround for an Issues, same as https://github.com/saros-project/saros/issues/1114
	implementation("org.eclipse.platform:org.eclipse.e4.ui.services:1.3.700")
	implementation("javax.inject:javax.inject:1")
    testImplementation(project(path = ":saros.core", configuration = "testing"))
}

tasks {

    jar {
        into("assets") {
            from("assets")
        }
        into("icons") {
            from("icons")
        }
        from(".") {
            include("*.properties")
            include("readme.html")
            include("plugin.xml")
            include("LICENSE")
            include("CHANGELOG")
        }
	    from(rootProject.file("saros_log4j2.xml"))
	    from(rootProject.file("log4j2.xml"))
    }

    val testJar by registering(Jar::class) {
        classifier = "tests"
        from(sourceSets["test"].output)
    }

    artifacts {
        add("testing", testJar)
    }

    /*
     * Copy the log4j2 files into the eclipse project dir
     * to make them available for PDE.
     */
    register("copyLogFiles", Copy::class) {
      into("${project.projectDir}/")
      from(rootProject.file("log4j2.xml"))
      from(rootProject.file("saros_log4j2.xml"))
    }

    /* Eclipse release tasks
     *
     * The following tasks provide the functionality of creating
     * an eclipse update-site (via "updateSite") or dropin (via "dropin").
     * The creation of the update-site uses as recommended the eclipse's
     * P2Directory. You can find a how-to here:
     * http://maksim.sorokin.dk/it/2010/11/26/creating-a-p2-repository-from-features-and-plugins/
     *
     * Instead of calling the P2Director via CLI we use the goomph
     * plugin's classes that provide an abstraction layer of the P2Director.
     */

    val updateSiteDirPath = "build/update-site"

    /* Step 0 of update-site creation
     *
     * Creates the structure:
     * update-site/
     *  |- features/
     *    |- feature.xml
     *  |- plugins/
     *    |- saros.core.jar
     *    |- saros.eclipse.jar
     *  |- site.xml
     */
    val updateSitePreparation by registering(Copy::class) {
        dependsOn(":saros.core:jar", ":saros.eclipse:jar")

        into(updateSiteDirPath)
        into("features") {
            from("feature/feature.xml")
        }
        into("plugins") {
            from(project.tasks.findByName("jar"))
            from(project(":saros.core").tasks.findByName("jar"))
        }
        from("update/site.xml")
    }

    /* Step 1 of update-site creation
     *
     * Creates the basic p2-Repository, but it is
     * not usable as update-site, because the plugins
     * are not visible to users.
     *
     * equivalent to P2Director call with:
     * <code>
     * org.eclipse.equinox.p2.publisher.FeaturesAndBundlesPublisher \
     *   -source <project dir>/build/update-site \
     *   -compress \
     *   -inplace \
     *   -append \
     *   -publishArtifacts
     * </code>
     */
    val updateSiteFeaturesAndBundlesPublishing by registering {
        dependsOn(updateSitePreparation)
        doLast {
            with(FeaturesAndBundlesPublisher()) {
                source(project.file(updateSiteDirPath))
                compress()
                inplace()
                append()
                publishArtifacts()
                runUsingBootstrapper()
            }
        }
    }

    /* Step 2 of update-site creation
     *
     * Adds the meta-data to the repository to make
     * the plugin accessible for users.
     *
     * equivalent to P2Director call with:
     * <code>
     * org.eclipse.equinox.p2.publisher.CategoryPublisher \
     *   -metadataRepository file:<project dir>/build/update-site \
     *   -categoryDefinition file:<project dir>/build/update-site/site.xml
     * </code>
     */
    val updateSiteCategoryPublishing by registering {
        dependsOn(updateSiteFeaturesAndBundlesPublishing)
        doLast {
            with(CategoryPublisher(EclipseRelease.official(eclipseVersionNr))) {
                metadataRepository(project.file(updateSiteDirPath))
                categoryDefinition(project.file("$updateSiteDirPath/site.xml"))
                runUsingPdeInstallation()
            }
        }
    }

    /* Step 3 of update-site creation
     *
     * The creation-process is already completed after
     * step 2, but this task removes the meta-data which
     * were necessary for update-site creation but are
     * not part of the update-site.
     */
    val updateSite by registering(Delete::class) {
        dependsOn(updateSiteCategoryPublishing)

        delete("$updateSiteDirPath/features/feature.xml")
        delete(fileTree("$updateSiteDirPath/plugins") {
            // Remove all bundles without version (which is appended during update-site build)
            exclude("*_*.jar")
        })
        delete("$updateSiteDirPath/site.xml")
    }

    /* Creates a dropin at build/dropin
     *
     * Creates an update-site, removes the superfluous meta-data
     * and creates a zip.
     */
    register("dropin", Zip::class) {
        dependsOn(updateSite)

        archiveFileName.set("saros-eclipse-dropin.zip")
        destinationDirectory.set(project.file("build/dropin"))

        from(updateSiteDirPath) {
            exclude("*.jar")
        }
    }
}
