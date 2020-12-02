// Imports for node support
import com.moowork.gradle.node.npm.NpmInstallTask
import com.moowork.gradle.node.npm.NpmTask
import com.moowork.gradle.node.npm.NpxTask
import com.moowork.gradle.node.task.NodeTask
import com.moowork.gradle.node.yarn.YarnInstallTask
import com.moowork.gradle.node.yarn.YarnTask

// Imports for os detection
import org.apache.tools.ant.taskdefs.condition.Os

plugins {
  id("com.github.node-gradle.node") version "2.2.4" apply true
}

// Extract extension data
var packageJsonContent = File("${project.projectDir}/package.json").readText()

var versionRegex = "\"version\": \"(.*?)\"".toRegex()
var versionMatch = versionRegex.find(packageJsonContent)
var version = versionMatch!!.groups.get(1)!!.value

var publisherRegex = "\"publisher\": \"(.*?)\"".toRegex()
var publisherMatch = publisherRegex.find(packageJsonContent)
var publisher = publisherMatch!!.groups.get(1)!!.value

var nameRegex = "\"name\": \"(.*?)\"".toRegex()
var nameMatch = nameRegex.find(packageJsonContent)
var name = nameMatch!!.groups.get(1)!!.value

// Path to vsce cli
var vscePath = "./node_modules/vsce/out/vsce"

node {
  version = "10.14.1"
  npmVersion = "6.4.1"
  download = true
}

tasks.register<Copy>("copyLsp") {
  from("${rootDir.absolutePath}/build/distribution/lsp")
  into("dist")
}

tasks.register("buildExtension") {
  dependsOn("copyLsp",
  "npmInstall",
  "npm_run_webpack")
  group = "VS Code"
  description = "Builds the extension"
}

tasks.register<Exec>("runExtension") {  
  dependsOn("buildExtension")
  group = "VS Code"
  description = "Builds and runs the extension"

  var cwd = System.getProperty("cwd", "") // gradle argument is -Pcwd=
  var execArgs = "--extensionDevelopmentPath=${projectDir.absolutePath} ${cwd}".trim()
  if (Os.isFamily(Os.FAMILY_WINDOWS)) {
    executable = "cmd"
    setArgs(listOf("/c code ${execArgs}"))
  } else {
    executable = "code"
    setArgs(listOf(execArgs))
  }

  workingDir = File("./dist")
}

tasks.register<NodeTask>("packageExtension") {
  dependsOn("copyLsp", "npmInstall")
  group = "VS Code"
  description = "Packages the extension"
  
  var outDir = "${project.projectDir}/vsix"
  doFirst {
    delete("$outDir/*")
    File("$outDir").mkdirs()
  }
  
  script = file(vscePath)
  setArgs(listOf("package" , "--out", "$outDir/${project.name}-$version.vsix" ))
}

tasks.register<NodeTask>("publishExtension") {
  dependsOn("copyLsp", "npmInstall")
  group = "VS Code"
  description = "Publishes the extension"
  
  script = file(vscePath)
  setArgs(listOf("publish", "patch"))
  
  setExecOverrides(closureOf<ExecSpec>({
    workingDir = file("./")
  }))
}

tasks.register<NodeTask>("unpublishExtension") {
  group = "VS Code"
  description = "Unpublishes the extension"
  
  script = file(vscePath)
  setArgs(listOf("unpublish", "${publisher}.${name}"))
  
  setExecOverrides(closureOf<ExecSpec>({
    workingDir = file("./")
  }))
}