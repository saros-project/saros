plugins {
    `java-gradle-plugin`
}

repositories {
    maven {
        url = uri("https://plugins.gradle.org/m2/")
    }
}

dependencies {
    compile("com.diffplug.gradle:goomph:3.24.0")
    compile("gradle.plugin.org.jetbrains.intellij.plugins:gradle-intellij-plugin:0.4.21")
}

gradlePlugin {
    plugins {
        create("eclipsePlugin") {
            id = "saros.gradle.eclipse.plugin"
            implementationClass = "saros.gradle.eclipse.SarosEclipsePlugin"
        }
        create("intellijPlugin") {
            id = "saros.gradle.intellij.plugin"
            implementationClass = "saros.gradle.intellij.SarosIntellijPlugin"
        }
    }
}
