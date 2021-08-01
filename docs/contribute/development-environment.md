---
title: Saros Development Environment
---

The following page describes how a development environment can be set up in order to develop all Saros products (Eclipse-Plugin, IntelliJ-Plugin and Server).
If you want to execute the STF tests it is recommended to use Eclipse. Otherwise it is also possible to develop with IntelliJ IDEA and execute the STF tests in a docker container.

## Common

* You have to [clone](https://help.github.com/articles/cloning-a-repository/) the Saros repository with [git](https://git-scm.com/book/en/v2/Getting-Started-Installing-Git).
* You need a **Java 8 JDK** (e.g. from [AdoptOpenJDK](https://adoptopenjdk.net/?variant=openjdk8&jvmVariant=hotspot) or using [SDKMAN](https://sdkman.io/install) to manage [multiple JDKs](https://sdkman.io/jdks)).
* *Optional:* You can use a local **IntelliJ IDEA** installation (version `2019.2.3` or newer) for dependency resolution by setting the **system-wide environment variable `INTELLIJ_HOME`** to the IntelliJ installation directory that contains the directory `lib`.
If the `INTELLIJ_HOME` variable is not set, the intellij-gradle-plugin will download and use the IntelliJ version specified in the `build.gradle` file of the 'intellij' project.
* *Optional:* You can also set the **system-wide environment variable `SAROS_INTELLIJ_SANDBOX`** to specify the base directory in which the IntelliJ sandboxes will be created. Otherwise, the directory `intellij/build` in the repository will be used by default.

### Google Java Format

We are using [google java format](https://github.com/google/google-java-format) to ensure that our source code adheres to unified formatting rules.
This is checked on our build server, so please make sure to format your code with the tool before pushing.
For ease of use, the formatter can also be integrated into the default formatting logic of Eclipse and IntelliJ through a plugin.
Installation instructions are given in the IDE specific sections on the topic ([Eclipse](#install-and-enable-google-java-formatter), [IntelliJ](#install-and-enable-google-java-formatter-1)).

**Important:** We still use [**google java format 1.10**](https://github.com/google/google-java-format/releases/tag/v1.10.0).



### Gradle Setup on Widows

If you are developing on a Windows system using multiple drives, please make sure that the Gradle cache (contained in the `.gradle` directory) is located on the same drive as the Saros repository.

This can be done by either ensuring that the git repository is located on the same drive as the [default Gradle user home directory](https://docs.gradle.org/current/userguide/directory_layout.html#dir:gradle_user_home) or by setting a different base directory for the Gradle user home using the environment variable `GRADLE_USER_HOME`.

## Develop with Eclipse

If you develop on Eclipse you should have already installed the Eclipse version (if you plan to run the STF tests, please install the version specified in the [STF documentation](saros-testing-framework.md#prerequisites)) for "Eclipse Committers".

### Configure

#### Use the Saros Clean-Up Profile

* Right-click the "Saros" project in the project explorer and navigate to<br/>
  `Properties > Java Code Style > Clean up`
* Under the box `Active profile:`, click `Import...`
* Select the profile [`saros/clean-up-profile.xml`](https://github.com/saros-project/saros/blob/master/eclipse/clean-up-profile.xml)

#### Install and Enable Google Java Formatter

* Install the [Eclipse Google Java Formatter](https://github.com/google/google-java-format#eclipse), which is available as a Drop-In in the [GitHub Releases](https://github.com/google/google-java-format/releases).
* Enable the formatter by choosing `google-java-format` in `Window > Preferences > Java > Code Style > Formatter > Formatter Implementation`

### Import the Saros Project

* Open a bash terminal, navigate to the repository directory and execute `./gradlew prepareEclipse` (use `./gradlew.bat` for windows)
* Import the project as Git project
* If you add dependencies you have to execute the `prepareEclipse` task again in order to regenerate the dependency information for Eclipse.

## Develop with IntelliJ

It is necessary to import the Saros project and change the project setting so that all build/test/debug actions are processed
by Gradle.

See [the Saros testing framework documentation](saros-testing-framework.md) for more information about interactive testing in the IntelliJ environment.

### Configure

#### Install and Enable Google Java Formatter

* Install the [IntelliJ Google Java Formatter](https://plugins.jetbrains.com/plugin/8527-google-java-format) which is available in the IntelliJ plugin repository (search for `google-java-format`).

#### Delegate IDE Action

This is necessary in order to allow IntelliJ to execute all build and test action via Gradle.
* Navigate to `Settings > Build, Exection, Deployment > Build Tools > Gradle > Runner`
* Check the box `Delegate IDE build/run actions to gradle`
* Select `Gradle Test Runner` in the drop-down box with the caption `Run tests using`

### Open Project

**Don't import the project.** Otherwise all existing IntelliJ configurations (e.g. formatting rules) will be removed.

* Click on `Open`
* Select the repository root as project root and click `OK`

## Develop Without an IDE

If you prefer to develop with a text editor (like Vim or Emacs) you can build and test
the code via Gradle. In order to call a task you have to execute `./gradlew <task>...` in
your local Saros repository root directory.

The following tasks are used to build and test the different Saros components:
* `cleanAll` - Removes all build and test artifacts
* `sarosEclipse` - Triggers the build and test of the Saros Eclipse Plugin
* `sarosIntellij` - Triggers the build and test of the Saros IntelliJ Plugin
* `sarosServer` - Triggers the build and test of the Saros Server
* `prepareEclipse` - Executes all tasks which are required before developing in Eclipse
* `runIde` - Starts a IntelliJ IDE containing the Saros Plugin. The IDE version depends on the value of `INTELLIJ_HOME` or the `intellijVersion` specified in the build file of the IntelliJ package.

In order to build the whole project without using existing build artifacts simply call `./gradlew cleanAll sarosEclipse sarosIntellij sarosServer`.

Gradle checks whether the component specific sources are changed. Therefore a task become a NOP if nothing changed and the build results still exist.
If you want to force Gradle to re-execute the tasks, you have to call `./gradlew --rerun-tasks <task>...` or call the `cleanAll` task before other tasks.
The final build results are copied into the directory `<repository root>/build/distribute/(eclipse|intellij)`.

### Formatting via Standalone Google Java Formatter

* Download [Google Java Formatter](https://github.com/google/google-java-format/releases/) `google-java-format-<version>-all-deps.jar`
* Call `java -jar google-java-format-<version>-all-deps.jar --dry-run --set-exit-if-changed **/*.java` in a shell with enabled globstar (`shopt -s globstar`) to **check the formatting**
  and add the option `--replace` and remove the option `--dry-run` if you want to **trigger automated formatting**.

## Format Checking in a Git Hook

If you want to check that your commit does not contain wrong formatted code use a git pre-commit hook.
* Create an executable file in your repository `.git/hooks/pre-commit` which executes the standalone formatter as described in the previous section.

### Example Git Hook

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
    local url="https://repo.maven.apache.org/maven2/com/google/googlejavaformat/google-java-format/1.10.0/google-java-format-1.10.0-all-deps.jar"
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
