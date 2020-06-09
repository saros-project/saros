plugins {
    id("com.gradle.enterprise") version "3.3.3"
}

/*
 * TODO: Remove superfluous prefix. The previous setup required the prefix
 *       because the osgi bundle names had to match the project names.
 */
val prefix = "saros."
listOf("core", "eclipse", "intellij", "server", "lsp", "stf", "stf.test").forEach { dir ->
    val projectName = prefix + dir
    include(projectName)
    project(":$projectName").projectDir = file(dir)
}

val projectName = prefix + "picocontainer"
include(projectName)
project(":$projectName").projectDir = file("core/picocontainer")
