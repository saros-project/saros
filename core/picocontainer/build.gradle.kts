
/*
   This build applies our picocontainer patches and creates a new picocontainer jar with a different package naming
   instead of the package structure org.picocontainer the package name saros.repackaged.picocontainer is used.


   This is required, because intellij uses another picocontainer version and this results in conflicts during build.
   The previous solution was to exclude the picocontainer jar from the intellij installation (with a hidden feature
   of the gradle/intellij plugin) this feature will be removed.
*/

val picoVersion = "2.11.2"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.picocontainer:picocontainer:$picoVersion")
    testImplementation("junit:junit:4.12")
}

tasks {
    shadowJar {
        relocate("org.picocontainer", "saros.repackaged.picocontainer")
        baseName = "picocontainer"
        classifier = "patched_relocated"
        version = picoVersion
    }
}
