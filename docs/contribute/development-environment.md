---
title: Saros Development Environment
---

# {{ page.title }}
{:.no_toc}

{% include toc.html %}

The following page describes how a development environment can be set up in order to develop all Saros products (Eclipse-Plugin, IntelliJ-Plugin and Server).
If you want to execute the STF tests it is recommended to use eclipse. Otherwise it is also possible to develop with IntelliJ IDEA and execute the stf tests in a docker container. 

## Common
* You have to [clone](https://help.github.com/articles/cloning-a-repository/) the Saros repository with [git](https://git-scm.com/book/en/v2/Getting-Started-Installing-Git).
* You need a **Java 8 JDK**.
* You need an **Eclipse 4.6** installation which is used for dependency resolution. You can either install a [minimal Eclipse](http://www.eclipse.org/downloads/packages/release/neon/3/eclipse-ide-java-developers) or [Eclipse for Eclipse Committers](http://www.eclipse.org/downloads/packages/release/neon/3/eclipse-ide-eclipse-committers) if you also want to develop with eclipse.
* Install [GEF Legacy](https://projects.eclipse.org/projects/tools.gef/downloads) into Eclipse
* You need an **Intellij IDEA** installation which is used for dependency resolution. Install a current version of [**IntelliJ IDEA**](https://www.jetbrains.com/idea/download/#section=linux) (we have only tested Saros/I for IntelliJ releases 2017.X and later)

Set the **system-wide environment variable `ECLIPSE_HOME`** to the eclipse installation dir that contains the directory `plugins`.
Set the **system-wide environment variable `INTELLIJ_HOME`** to the intellij installation dir that contains the directory `lib`.

If the `ECLIPSE_HOME` variable is not set or not global the correponding eclipse specific dependencies cannot be found during build.
If the `INTELLIJ_HOME` variable is not set or not global the intellij-gradle plugin will download a version of IntelliJ which is
defined in the gradle build description.

## Develop with Eclipse
If you develop on Eclipse you should have already installed the Eclipse version for "Eclipse Committers".
### Import the Saros Project
* Open a bash terminal, navigate to the repository directory and execute `./gradlew prepareEclipse` (use `./gradlew.bat` for windows)
* Import the project as Git project
* If you add dependencies you have to execute the `prepareEclipse` task again in order to regenerate the dependency information for eclipse.

## Develop with Intellij
It is necessary to import the saros project and change the project setting so that all build/test/debug actions are processed
by Gradle.

### Open Project
**Don't import the project.** Otherwise all existing IntelliJ configurations (e.g. formatting rules) will be removed.

* Click on `Open`
* Select the repository root as project root and click `OK`
* Open the gradle task view and execute the task `prepareIntellij`


### Delegate IDE Action
This is necessary in order to allow IntelliJ to execute all build and test action via Gradle.
* Navigate to `Settings > Build, Exection, Deployment > Build Tools > Gradle > Runner`
* Check the box `Delegate IDE build/run actions to gradle`
* Select `Gradle Test Runner` in the dropdown box with the caption `Run tests using`

## Develop without an IDE
If you prefer to develop with a text editor (like Vim or Emacs) you can build and test
the code via gradle. In order to call a task you have to execute `./gradlew <task>...` in
your local Saros repository root directory.

The following tasks are used to build and test the different Saros components:
* `cleanAll` - Removes all build and test artifacts
* `sarosEclipse` - Triggers the build and test of the Saros Eclipse Plugin
* `sarosIntellij` - Triggers the build and test of the Saros IntelliJ Plugin
* `sarosServer` - Triggers the build and test of the Saros Server
* `prepareEclipse` - Executes all tasks which are required before developing in Eclipse
* `prepareIntellij` - Executes all tasks which are required before developing in IntelliJ
* `runIde` - Starts a IntelliJ IDE containing the Saros Plugin. The IDE version depends on the value of `INTELLIJ_HOME`.

In order to build the whole project without using existing build artifacts simply call `./gradlew cleanAll sarosEclipse sarosIntellij sarosServer`.

Gradle checks whether the component specific sources are changed. Therefore a task become a NOP if nothing changed and the build results still exist.
If you want to force gradle to reexecute the tasks you have to call `./gradlew --rerun-tasks <task>...` or call the `cleanAll` task before other tasks.
The final build results are copied into the directory `<repository root>/build/distribute/(eclipse|intellij)`.
