---
title: Saros Development Environment
---

The following page describes how a development environment can be set up in order to develop all Saros products (Eclipse-Plugin, IntelliJ-Plugin and Server).
If you want to execute the STF tests it is recommended to use eclipse. Otherwise it is also possible to develop with IntelliJ IDEA and execute the stf tests in a docker container.

## Common
* You have to [clone](https://help.github.com/articles/cloning-a-repository/) the Saros repository with [git](https://git-scm.com/book/en/v2/Getting-Started-Installing-Git).
* You need a **Java 8 JDK**.
* You need an **Eclipse 4.6** installation which is used for dependency resolution. You can either install a [minimal Eclipse](http://www.eclipse.org/downloads/packages/release/neon/3/eclipse-ide-java-developers) or [Eclipse for Eclipse Committers](http://www.eclipse.org/downloads/packages/release/neon/3/eclipse-ide-eclipse-committers) if you also want to develop with eclipse.
* Install **GEF Legacy** into Eclipse using the 'GEF-Legacy Releases' update site given [here](https://projects.eclipse.org/projects/tools.gef/downloads)
* You need an **Intellij IDEA** installation which is used for dependency resolution. Install a current version of [**IntelliJ IDEA**](https://www.jetbrains.com/idea/download/#section=linux) (we have only tested Saros/I for IntelliJ releases 2017.X and later)

Set the **system-wide environment variable `ECLIPSE_HOME`** to the eclipse installation dir that contains the directory `plugins`.<br/>
Set the **system-wide environment variable `INTELLIJ_HOME`** to the intellij installation dir that contains the directory `lib`.

If the `ECLIPSE_HOME` variable is not set or not global the correponding eclipse specific dependencies cannot be found during build.<br/>
If the `INTELLIJ_HOME` variable is not set or not global the intellij-gradle plugin will download a version of IntelliJ which is
defined in the gradle build description.

## Develop with Eclipse
If you develop on Eclipse you should have already installed the Eclipse version for "Eclipse Committers".

### Configure

#### Use the Saros Clean-Up Profile

* Right-click the "Saros" project in the project explorer and navigate to<br/>
  `Properties > Java Code Style > Clean up`
* Under the box `Active profile:`, click `Import...`
* Select the profile [`saros/clean-up-profile.xml`](https://github.com/saros-project/saros/blob/master/saros/clean-up-profile.xml)

#### Install and Enable Google Java Formatter
* Install the [eclipse Google Java Formatter](https://github.com/google/google-java-format#eclipse) which is available as Drop-In in [GitHub Releases](https://github.com/google/google-java-format/releases).
* Enable the formatter by choosing `google-java-format` in `Window > Preferences > Java > Code Style > Formatter > Formatter Implementation`

### Import the Saros Project
* Open a bash terminal, navigate to the repository directory and execute `./gradlew prepareEclipse` (use `./gradlew.bat` for windows)
* Import the project as Git project
* If you add dependencies you have to execute the `prepareEclipse` task again in order to regenerate the dependency information for eclipse.

## Develop with Intellij
It is necessary to import the saros project and change the project setting so that all build/test/debug actions are processed
by Gradle.

See [the saros testing framework documentation](saros-testing-framework.md) for more information about interactive testing in the Intellij environment.

### Configure

#### Install and Enable Google Java Formatter
* Install the [intellij Google Java Formatter](https://plugins.jetbrains.com/plugin/8527-google-java-format) which is available in the IntelliJ plugin repository. Search for `google-java-format`.
* Open `Settings > google-java-format Settings` and check the checkbox `Enable google-java-format`

#### Delegate IDE Action
This is necessary in order to allow IntelliJ to execute all build and test action via Gradle.
* Navigate to `Settings > Build, Exection, Deployment > Build Tools > Gradle > Runner`
* Check the box `Delegate IDE build/run actions to gradle`
* Select `Gradle Test Runner` in the dropdown box with the caption `Run tests using`

### Open Project
**Don't import the project.** Otherwise all existing IntelliJ configurations (e.g. formatting rules) will be removed.

* Click on `Open`
* Select the repository root as project root and click `OK`
* Open the gradle task view and execute the task `prepareIntellij`

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

### Formatting via standalone Google Java Formatter
* Download the latest release of the [Google Java Formatter](https://github.com/google/google-java-format/releases) `google-java-format-<version>-all-deps.jar`
* Call `java -jar google-java-format-<version>-all-deps.jar --dry-run --set-exit-if-changed **/*.jar` in a shell with enabled globstar (`shopt -s globstar`) to **check the formatting**
  and add the option `--replace` and remove the option `--dry-run` if you want to **trigger automated formatting**.
## Format checking in a githook
If you want to check that your commit does not contain wrong formatted code use a git pre-commit hook.
* Create an executable file in your repository `.git/hooks/pre-commit` which executes the standalone formatter as described in the previous section.
### Example git hook
Example hook that checks only java files that are staged.
```bash
  #!/bin/bash
  # check for dependent tools
  commands="grep tr curl java"
  for cmd in $commands; do
    which $cmd > /dev/null
    [ "$?" != "0" ] && echo "Command '$cmd' not available git hook is disabled" && exit 0
  done
  # define jar location
  formatter_jar_path=/tmp/formatter.jar
  # download stand-alone formatter
  function download_formatter() {
    local jar_path=$1
    local url="https://repo.maven.apache.org/maven2/com/google/googlejavaformat/google-java-format/1.6/google-java-format-1.6-all-deps.jar"
    set -e
    curl -Lo $jar_path "$url" > /dev/null
    set +e
  }
  # check whether formatter already exist
  [ ! -f "$formatter_jar_path" ] && download_formatter "$formatter_jar_path"
  # get staged java files (deleted files are excluded)
  files_to_check=`git diff --cached --name-only --diff-filter=d | grep "^.*.java$" | tr '\n' ' '`
  # execute formatter if java files are staged
  if [[ ! -z "${files_to_check// }" ]]; then
    java -jar "$formatter_jar_path" --dry-run --set-exit-if-changed $files_to_check
    rc=$?
    [ "$rc" != "0" ] && printf "\nGit hook: Formatting issues in the file listed above\n"
    exit $rc
  fi
  exit 0
```
