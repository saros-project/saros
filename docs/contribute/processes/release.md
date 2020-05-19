---
title: Release Process
---


## When do we release?

We publish releases when a relevant set of changes is integrated in the master branch and the team wants to release.

## How do I create the release artifacts?
In the following the task which are required to build all release artifacts are described.

Please make sure before creating the artifacts that **all unit and stf tests are successful.**

### Eclipse

**Artifacts:**
* Update Site
* Drop-in

#### Prerequisite
Make sure to increase the version number of the `saros.eclipse` **and** `saros.core` osgi bundle.
If only the `saros.eclipse` version is increased, a Saros update (via the update-site) would lead to a corrupted Saros installation that consists of the new `saros.eclipse` bundle and the `saros.core` version of the previous release.

#### Update Site

* Open an eclipse instance which contains a working [Saros development setup](https://www.saros-project.org/contribute/development-environment.html)
* Open the project `Saros Update Site`
* Open the file `site.xml`
* Make sure that the category `DPP` contains the current feature `saros.feature*`
* Click on `Build All`
* The Update Site is created in the `Saros Update Site` project directory

#### Drop-in

* Open an eclipse instance which contains a working [Saros development setup](https://www.saros-project.org/contribute/development-environment.html)
* Open `File > Export… > Plug-in Development > Deployable features`
* Choose feature
* Destination: Directory
* Choose target directory name: `saros-dropin-<version>`
* Click Finish
* In the dropin folder, delete the files `artifacts.jar` and `content.jar`
* Add the readme: `This is the Saros Eclipse plugin dropin archive. You can install it by unzipping it to eclipse/dropins and restarting Eclipse.`
* Create a zip file from the folder

### IntelliJ IDEA

**Artifacts:**
* Plug-in zip

#### Plug-In zip

* Open bash
* Navigate to the Saros repository dir
* Execute the command `./gradlew sarosIntellij`
* You find the zip here: `./build/distribution/intellij/saros.inteliij.zip`

## How do I test the artifacts?

As mentioned before you should already executed all unit and STF tests (which has to be successful).

The release artifacts are manually tested with at least one other person:
* You have to install the artifact and start a session.
* During this session you should check (at least) that the basic features are working.

## How do I release the artifacts?
In the following the technical steps which are required for releasing artifacts are described.

### Eclipse

**Channels:**
* Update Site
* Eclipse Marketplace
* GitHub Releases (See below)

**Attention**: The **Eclipse Marketplace** uses the **Update Site** in order to provide the plug-in.
Therefore it is not possible to release via the update site without updating the marketplace.

#### Update Site
The update site is hosted via GitHub Pages. Therefore you just have to change the content of the repository [update-site-artifacts](https://github.com/saros-project/update-site-artifacts)
in order to change the update site.

**What is released via this channel?**
We release the update site (which contains the Eclipse plug-in jars and additional resources) via this channel.

**Login process**

* Login with the `saros-infrastructure` user

**Release process**

* Create a pull request that contains the new update site
* Merge the pull request
* Check that the update site is deployed successfully

#### Eclipse Marketplace

**What is released via this channel?**
The Saros for Eclipse plug-in is released via this channel, but you don't have to release the artifact explicit via
this channel, because the Marketplace uses the **Update Site**.
As long as the infrastructure does not change (like the update site location) just update **Version number** information
in the marketplace.

**Login process**

* Login with the saros-outreach user

**Release/Metadata change process**

* Search for the plug-in "Saros"
* Click on Saros
* Click on the `Edit` tab
* Change corresponding metadata (as **Version number**)
* Submit form with `Save`

### IntelliJ IDEA

**Channels:**
* JetBrains Plugin Repository
* GitHub Releases (See below)

Attention: We are currently releasing the Saros for IntelliJ plug-in via the `alpha` channel of the JetBrains Plugin Repository
instead of the `stable` channel (which is the default channel).

#### JetBrains Plugin Repository

**What is released via this channel?**
We release the IntelliJ IDEA plug-in zip via this repository.

**Login process**

* Login as user `saros-infrastructure` in [GitHub](https://github.com)
* Open the [JetBrains Plugin Repository](https://plugins.jetbrains.com/)
* Click on `Sign In`
* Instead of entering user credentials, choose the login via GitHub (click on the GitHub icon)

**Release process**

* Open the drop-down menu which appears if you hover over the username
* Click on `Upload plugin`
* Fill the form:
  * JAR/ZIP file to upload: Upload the plug-in as zip archive
  * License: Paste link to GPLv2: <https://opensource.org/licenses/GPL-2.0>
  * Category: Choose **TeamWork**
  * Channel: Use the `alpha` channel
* Submit form

### GitHub Releases (IntelliJ and Eclipse)

**What is released via this channel?**
We release the Saros for Eclipse drop-in and the Saros for IntelliJ zip via this channel.

**Login process**

* Login as a user with write access in [GitHub](https://github.com) (e.g. `saros-infrastructure`)

**Release process**

* Navigate to the [GitHub Saros page](https://github.com/saros-project/saros)
* Click on the `releases` tab
* Click on `draft a new release`
* Fill the form:
  * Tag: `saros-<ide>-<version>` (e.g. `saros-intellij-0.1.1`)
  * Tag target: a commit id
  * Release title: Short title as `Saros for <ide> <version>`
  * Description: Short description of artifacts and link the release notes
  * Upload binaries as artifacts (e.g. drop-in, plug-in zip)
* Submit form with `Publish release` (or `Save draft` if you don't want to publish the release)
