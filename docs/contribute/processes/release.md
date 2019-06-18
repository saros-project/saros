---
title: Release Process
---

The documentation is divided into three parts:

1. When do we release
2. How do I create the release artifacts
3. How do I test the release artifacts
4. How do I release the artifacts

## When?

Stable releases happen if a relevant set of changes is integrated in the master branch and the team wants to release.

## How to release?
In the following the technical steps which are required for releasing are described.

### Eclipse

**Attention**: The **Eclipse Marketplace** uses the **Update Site** in order to provide the plug-in.
Therefore it is not possible to release via the update site without updating the marketplace.

#### Update Site
The update site is hosted via GitHub Pages. Therefore you just have to change the content of the repository [update-site-artifacts](https://github.com/saros-project/update-site-artifacts)
in order to change the update site.

**Login process**

* Login with the `saros-infrastructure` user

** Release process**

* Create a pull request that contains the new update site
* Merge the pull request
* Check that the update site is deployed successfully

#### Eclipse Marketplace

**What is released via this channel?**
The Saros for Eclipse plug-in is released via this channel, but you don't have to release the artifact explicit via
this channel, because the Marketplace uses the **Update Site**.
As long as the infrastructure does not change (as the update site location) just update **Version number** information
in the marketplace.

**Login process**

* Login with the saro-outreach user

**Release/Metadata change process**

* Search for the plug-in "Saros"
* Click on Saros
* Click on the `Edit` tab
* Change corresponding metadata (as **Version number**)
* Submit form with `Save`

### IntelliJ IDEA

Attention: We are currently releasing the Saros for IntelliJ plug-in via the `alpha` channel of the JetBrains Plugin Repository
instead of the `stable` channel (which is the default channel).

#### JetBrains Plugin Repository

**What is released via this channel?**
We release the IntelliJ IDEA plug-in zip via this repository.

**Login process**

* Login as user `saros-infrastructure` in [GitHub](https://github.com)
* Open the [JetBrains Plugin Repository](https://plugins.jetbrains.com/)
* Click on `Sign In`
* Instead of entering user credentials, choose the login via Github (click on the GitHub icon)

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
  * Tag taget: a commit id
  * Release title: Short title as `Saros for <ide> <version>`
  * Description: Short description of artifacts and link the release notes
  * Upload binaries as artifacts (e.g. drop-in, plug-in zip)
* Submit form with `Publish release` (or `Save draft` if you don't want to publish the release)
