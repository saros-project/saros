---
title: Release Process
---

## When?

Daily releases are created automatically by the CI Server. These releases are located in [GitHub](https://github.com/saros-project/saros/releases)
and have the naming pattern 'daily-<branch>'. These releases are tagged as 'prerelease', because it is necessary that the users directly see that
these releases are not stable and final.

Stable releases happen if a relevant set of changes is integrated in the master branch and the team wants to release.

## Roles

Everyone is involved in the release process to some degree -- at least
as a
![](http://icons.iconarchive.com/icons/icons-land/vista-people/16/Person-Male-Light-icon.png)**Developer
(D)**. Specifically, these are the roles and their responsibilities:

**![](http://icons.iconarchive.com/icons/icons-land/vista-people/32/Office-Customer-Male-Light-icon.png)Release
Manager (RM)**

*   is responsible for the Saros release as a whole
*   this includes having the last word on which bugs are "critical"
*   receives help from the **Assistant Release Manager (ARM)**

**![](http://icons.iconarchive.com/icons/icons-land/vista-people/32/Medical-Nurse-Male-Light-icon.png)Test
Manager (TM)**

*   is responsible for assuring the quality of the release
*   currently, he does so through creating and executing the test plan
*   receives help from the **Assistant Test Manager (ATM)**

**![](http://icons.iconarchive.com/icons/icons-land/vista-people/32/Occupations-Writer-Male-Light-icon.png)Documentation
Manager (DM)**

*   is responsible that both our code documentation and the Saros
    website do not lag behind the development

![](http://icons.iconarchive.com/icons/icons-land/vista-people/32/Person-Male-Light-icon.png)**Developer
(D)**

*   is everyone, including the roles mentioned above
*   works on documentation issues or bug fixes

The rest of this process description is *a recipe* how to fulfill the
above responsibilities in order to create a new Saros release within one
working week. It consists of checklists and timings, *none of which are
absolutely mandatory*.
We consider ourselves capable software developers and responsible team
players. So if you find another (a better?) way to fulfill your
responsibilities (without hampering the other developers in fulfilling
theirs), you're free to do so. However, the lists below provide a pretty
good guideline, so consider sticking to them for a while.

## Preparation

There are a few things that need to be done before the Release Week can
start. The right time are the last days of the week before the Release
Week.

### Assign the roles

Feel free to volunteer! Otherwise, role assignment is done by Franz.
These are the guidelines:

*   The RM should be someone who did not participate in a Saros release
    process before.
*   The ARM should be someone who was (A)RM more than once.
*   The TM should be someone who did not assume the TM role before.
*   The ATM should be someone who was (A)TM before.

### Create missing accounts

*   **![](http://icons.iconarchive.com/icons/icons-land/vista-people/16/Person-Male-Light-icon.png)D**
    (the **whole team**, i.e. **everyone** incl. **you**):
    *   Make sure you [have a Sourceforge
        account](https://sourceforge.net/user/registration) that is
        assigned to the Saros project. If Saros is not listed among your
        projects (to check: [login to
        Sourceforge](https://sourceforge.net/auth/), top left corner
        "Me" &gt; "Profile"), tell Franz your account name and he will
        add you.
    *   Make sure you have *Author* access to this
        website (saros-project.org).
        *   If you are with Freie Universität Berlin, you can use your
            Zedat credentials to [login](index.md%3Fq=user.md). Ask
            Franz afterwards to grant you *Author* rights.
        *   Otherwise, contact Franz and he will create an account
            for you.
    *   Make sure you [subscribed
        saros-devel](https://groups.google.com/forum/#!forum/saros-devel).
*   **![](http://icons.iconarchive.com/icons/icons-land/vista-people/16/Office-Customer-Male-Light-icon.png)RM**
    and
    **![](http://icons.iconarchive.com/icons/icons-land/vista-people/16/Office-Customer-Male-Light-icon.png)ARM**:
    *   Make sure you are [in the Gerrit
        group](http://saros-build.imp.fu-berlin.de/gerrit/#/settings/group-memberships) "Release-Managers".
        If you are not, ask Franz to do something about it.
    *   Make sure [Sourceforge knows about your public SSH
        key](release.md#How_to_create_a_ssh_key_forSourc).
    *   Ask Franz to make sure you are a [Sourceforge "Release
        Technician"](https://sourceforge.net/p/dpp/admin/files/releasers/).
    *   Ask Franz to make sure you are in the [Sourceforge
        group](https://sourceforge.net/p/dpp/admin/groups/) "Release-Manager".
    *   Ask Franz to make sure you are a [non-moderated member of the
        saros-announce](https://groups.google.com/forum/#!forum/saros-announce) list.
*   **![](http://icons.iconarchive.com/icons/icons-land/vista-people/16/Occupations-Writer-Male-Light-icon.png)DM**:
    *   Ask Franz to make sure you are in the [Sourceforge
        group](https://sourceforge.net/p/dpp/admin/groups/) "Tracker-Admin".

## The Release Week

This section gives an overview of the roles' activities. Technical
details for these tasks follow below.

### Monday (Create changelog, Create test plan, Open documentation issues and assign)

*   Before 12:00:
    *   **![](http://icons.iconarchive.com/icons/icons-land/vista-people/16/Office-Customer-Male-Light-icon.png)RM**
        creates a [new release branch from the master
        branch](release.md#CreateReleaseBranch)
    *   **![](http://icons.iconarchive.com/icons/icons-land/vista-people/16/Occupations-Writer-Male-Light-icon.png)DM**
        checks all non-closed entries from the [Documentation Issue
        Tracker](https://sourceforge.net/p/dpp/documentation-issues/),
        while focusing (not slavishly) on those assigned to the
        current milestone/release.
        *   Close if the documentation issue is resolved. Some open
            issues might be obsolete, e.g. when they refer to
            packages/features that do no longer exist. Close these
            issues as *invalid*.
*   Before 16:00:
    1.  **![](http://icons.iconarchive.com/icons/icons-land/vista-people/16/Office-Customer-Male-Light-icon.png)RM**
        and
        **![](http://icons.iconarchive.com/icons/icons-land/vista-people/16/Office-Customer-Male-Light-icon.png)ARM**
        create the **Changelog**, a list of newly fixed bugs and
        features and send it to `saros-devel`
        *   Use the Git [commit
            history](https://github.com/saros-project/saros/commits/master)
            as a starting point. Look for commits with the tags `[GUI]`,
            `[FEATURE]`, or `[FIX]`; it should be safe to ignore
            `[INTERNAL]` and `[REFACTOR]` commits.
        *   Look at the [bug
            tracker](https://github.com/saros-project/saros/issues) for the
            entries referenced in the `[FIX]` commits; make sure they
            are in the state "pending-fixed" (i.e. they are technically
            fixed, but not yet released to the wide public).
        *   Describe how the fixed bugs can be reproduced. This is
            especially useful for the testers.
        *   Try to sort items by importance and write in a language
            which *users* can understand (if you run out of time, this
            sub-task can be deferred until Friday).
        *   For each item on the list, please include the author's name
        *   **![](http://icons.iconarchive.com/icons/icons-land/vista-people/16/Medical-Nurse-Male-Light-icon.png)TM**
            and
            **![](http://icons.iconarchive.com/icons/icons-land/vista-people/16/Medical-Nurse-Male-Light-icon.png)ATM**
            might help in this progress (they are blocked by this
            anyway), especially when the commit history is long

    2.  (a)
        **![](http://icons.iconarchive.com/icons/icons-land/vista-people/16/Medical-Nurse-Male-Light-icon.png)TM**
        and
        **![](http://icons.iconarchive.com/icons/icons-land/vista-people/16/Medical-Nurse-Male-Light-icon.png)ATM**
        use the **Changelog** created by the RMs to compile a **Test
        Plan** to cover all these new features (incl. GUI changes) and
        fixed bugs
        *   You may want to ask the original commit author for ways to
            reproduce newly fixed bugs
        *   [DOES THIS WORK?] Also: Consider backward-compatibility. In a test session,
            one person should use the release branch version, the other
            uses the previous Saros release. This information is
            important in the releasing process that follows.

    2.  (b) As soon as the RMs completed their **Changelog**, the
        **![](http://icons.iconarchive.com/icons/icons-land/vista-people/16/Occupations-Writer-Male-Light-icon.png)DM**
        adds new documentation issues, prioritizes and assigns them to
        the developers.
        *   For the newly introduced `[FEATURE]`s and changes to the
            `[GUI]`: Are they adequately represented on our website? See
            also: [Documentation issue
            priorities](documentation.md#DocIssuePrios).
        *   Assign each open issue to someone with the knowledge to work
            on it: start with the highest priority, aim for an even
            distribution; leave it as *"assigned to: none"* only if
            there really is no one.
        *   Send the list with the assigned documentation issues to
            `saros-devel`, e.g. like this
```
                Assignee 1:
                   * #127 [Code] Saros editor package: package description (package-info.java) is missing
                   * #156 [Website] GettingStarted: add instructions on how to add someone to the contact list
                Assignee 2:
                   * #134 ...
```

### Tuesday (Execute Test Plan, Decide whether found bugs are show stopper, fix docu issues and showstopper)

*   10:00-16:00 -
    **http://icons.iconarchive.com/icons/icons-land/vista-people/16/Medical-Nurse-Male-Light-icon.png)TM**
    &
    **http://icons.iconarchive.com/icons/icons-land/vista-people/16/Medical-Nurse-Male-Light-icon.png)ATM**
    execute the test plan (assisted by RM & ARM)
    *   Test managers close entries in the bug tracker that are verified
        as fixed (new status "closed-fixed")
*   after 16:00 -
    **http://icons.iconarchive.com/icons/icons-land/vista-people/16/Office-Customer-Male-Light-icon.png)RM**
    decides which of the discovered/still-open bugs are *critical* (= no
    release without them being fixed);
    **http://icons.iconarchive.com/icons/icons-land/vista-people/16/Medical-Nurse-Male-Light-icon.png)TM**
    sends to `saros-devel` ...
    *   the list of the critical bugs
    *   a list of comments on the Changelog (in particular whether a
        `[FIX]` or actually worked)
    *   all logfiles generated during the tests as one big archive
*   Before the TM's mail on `saros-devel` arrives:
    **![](http://icons.iconarchive.com/icons/icons-land/vista-people/16/Person-Male-Light-icon.png)Everyone
    else** takes the time to correct/complete the documentation issues
    (see also [How to handle documentation
    issues](documentation.md#DocIssues)). Mark the tracker issue as
    "fixed" when you're done.
    *   In case you really don't find the time to take care of the
        issues assigned to you during the release week, please do so in
        the following week.

### Wednesday (fix docu issues and showstopper, prepare user acceptance test)

*   The
    **![](http://icons.iconarchive.com/icons/icons-land/vista-people/16/Person-Male-Light-icon.png)whole
    team** fixes critical bugs on the release branch.
    *   Use slack time to work on your documentation issues.
*   The
    **![](http://icons.iconarchive.com/icons/icons-land/vista-people/16/Office-Customer-Male-Light-icon.png)RM**
    makes arrangements for the user acceptance test:
    *   Inform the **working group** (i.e. contact [Franz
        Zieris](http://www.mi.fu-berlin.de/w/Main/FranzZieris)) about
        the progress and the anticipated date of completion. The working
        group will find a time slot for testing, and then infors the RM
        about the agreed time slot.
    *   Once all critical bugs are fixed on the release branch, the
        [prepare the beta-update
        site](#how-to-prepare-the-user-acceptance-test) for
        the user acceptance test.
    *   Send the changelog to the working group so they know on which
        parts to pay particular attention.

### Thursday (perform acceptance tests, define showstopper issues)

*   The **working group** performs the user acceptance test.
    *   After the test, the working group sends their feedback,
        logfiles, and references to new bug tracker entries (if any) to
        the RM.
    *   *Note: Sometimes, the working group is able to perform their
        test already on Wednesday, which leaves more time for the
        development team to fix any showstoppers before Friday.*
*   **![](http://icons.iconarchive.com/icons/icons-land/vista-people/16/Person-Male-Light-icon.png)Everyone**:
    If you *still* got documentation issues left: Work on them.

### Friday (fix showstopper. run junit, release, announce, merge release branch back)

*   09:00-14:00 - The
    **![](http://icons.iconarchive.com/icons/icons-land/vista-people/16/Person-Male-Light-icon.png)whole
    team** works on any newly found critical bugs on the release branch
*   14:00 - The release begins
    *   **http://icons.iconarchive.com/icons/icons-land/vista-people/16/Office-Customer-Male-Light-icon.png)RM** +
        **http://icons.iconarchive.com/icons/icons-land/vista-people/16/Medical-Nurse-Male-Light-icon.png)TM**
        perform a last sanity test check and [run the JUnit test cases
        locally](testing.md#How_to_run_the_JUnit_test_cases).
    *   **http://icons.iconarchive.com/icons/icons-land/vista-people/16/Medical-Nurse-Male-Light-icon.png)TM**
        closes all additional bugs, that have been fixed in the meantime
        (new status "closed-fixed")
    *   **http://icons.iconarchive.com/icons/icons-land/vista-people/16/Office-Customer-Male-Light-icon.png)RM**
        [performs the
        release](release.md#How_to_create_a_new_release)
    *   **http://icons.iconarchive.com/icons/icons-land/vista-people/16/Office-Customer-Male-Light-icon.png)RM**
        [announces the new
        release](release.md#How_to_announce_a_new_release) on
        `saros-devel`, `saros-announce`, and the Sourceforge news
        section
    *   **http://icons.iconarchive.com/icons/icons-land/vista-people/16/Office-Customer-Male-Light-icon.png)RM**
        [merges the release branch
        back](release.md#How_to_merge)into the master branch
    *   **http://icons.iconarchive.com/icons/icons-land/vista-people/16/Office-Customer-Male-Light-icon.png)RM**
        tells everybody that they did a good job
    *   **![](http://icons.iconarchive.com/icons/icons-land/vista-people/16/Person-Male-Light-icon.png)Everyone**: Party.

## How Tos

### Create and prepare the release branch

#### Prepare current master branch

Let's assume we are currently preparing a release on **2013-12-06**,
thus the release id is ` 13.12.6`. Your task is to create a branch for
the current release **`13.12.6`** , but also prepare the `master` for
development for the next release, `14.1.31`.

*   Create a new pull request that bases on the master branch and contains only the following changes:
    *   In the `master`, edit `META-INF/MANIFEST.MF` for the projects Saros/Core,
        Saros/E and Saros/Whiteboard and set the Bundle-Version to the
        next release. The format is `$next release id$.DEVEL`, so in
        this case it's `14.1.31.DEVEL`.
    *   The commit message should be
        "`[BUILD] Master ready for development towards 14.1.31`"
*   Travis CI will run the JUnit tests; the **ARM** will review this pull request.
*   After Jenkins and ARM approved: Merge the pull request.

#### Prepare release branch

*   Create a new remote release branch `release/13.12.6` in GitHub that bases on the master branch.
*   Create a new pull request that has the new release branch as target and contains the following:
    *   edit `META-INF/MANIFEST.MF` for the projects Saros/Core, Saros/E and
        Saros/Whiteboard and set the Bundle-Version to
        `$current release id$.TESTING` (in this case
        ` 13.12.6.TESTING`)
    *   make sure that in project Saros the file saros.properties
        contains the line:
        `saros.debug=true`
    *   The commit message should be something like
        "`[BUILD] Open release branch 13.12.6`"

*   Travis CI will run the JUnit tests; the **ARM** will review this pull request.
    * After Travis CI and ARM approved: Merge the pull request.

*   Announce the release branch to `saros-devel` as open for testing using the
    following template:

        Release Branch is open for testing

           *> https://github.com/saros-project/saros/tree/release/13.12.6

           * The branch is ready to be tested
           * Only bug-fixes should be committed to the branch
           * Bug-fixes in the branch will be merged when 13.12.6 is released

         Master is open for 14.1.31.DEVEL

           *> https://github.com/saros-project/saros/tree/master

           * All new features should go to master

### How to prepare the User Acceptance Test

Note: There is actually no need for the `update_beta` project. We
should use the normal "update" project instead, which will then be
changed twice on the release branch, and delete the `update_beta` project
once the HowTo below is adapted.

#### Preconditions

Before you can roll-out the actual release of Saros, you need to prepare
the Eclipse Update Site for the acceptance test
(<http://dpp.sf.net/update-beta>). As for the actual release, there are
some important preconditions:

*   [Eclipse
    3.6](http://archive.eclipse.org/eclipse/downloads/drops/R-3.6.2-201102101200/)
    for building the test files.
*   Up-to-date working copy and the following projects in your Eclipse
    workspace
    *   Saros Eclipse plugin: `saros`
    *   Saros core project: `saros.core`
    *   Whiteboard plugin: `saros.whiteboard`
    *   Saros Eclipse Feature: `saros.feature`
    *   Saros Eclipse Update Site for Beta tests:
        `saros.update_beta`

#### Step-by-Step

Note: This section needs an update to include the Core project.

1.  In each of the projects **Saros/E**, **Saros/Core** and **Whiteboard**:
    *   File `META-INF/MANIFEST.MF`: Make sure that the version number
        is set to ` 13.12.6``.TESTING`

2.  In the **Feature** project:
    *   File `feature.xml`: Make sure that all version numbers are set
        to ` 13.12.6``.TESTING`
    *   Update plugin version of feature
        a.  Go to tab *Plug-ins*
        b.  Make sure `saros` and
            `saros.whiteboard` and
            `saros.core `are included
        c.  For all projects, click on *Versions...*
        d.  Select *Copy versions from plugin and fragment manifests*
        e.  Press *Finish*
        f.  (Make sure the version in the overview tab is correct.)

3.  Prepare the **Beta Update Site** project. Edit the `site.xml`.
    a.  Go to tab *Site Map*
    b.  Remove the previous feature (right-click &gt; "Remove")
    c.  Add current feature ("Add Feature" &gt; select
        "`saros.feature (``13.12.6``.TESTING)`")
    d.  Go to tab *Archives*
    e.  In the section "Archive Mapping" adjust the version numbers in
        the three entries
    f.  Save the file `site.xml`

4.  Build the update site
    a.  Press *Build* button on the selected new version in Site Map tab
        *   This will create the two folders `plugins/` and `features/`
            and create jar files in them.
        *   (NOTE: If they don't appear, it may help if you delete
            `artifacts.jar` and `content.jar`.)

5.  Copy update-site to sourceforge
    a.  login with ssh into `[username],dpp@web.sourceforge.net` (e.g.
        with WinSCP)
    b.  copy the files `index.md` and `site.xml` as well as the
        folders `features/ `and `plugins/` into `htdocs/update-beta/`
    c.  Make sure that all copied files have read/write permissions for
        group to allow other developers to overwrite and delete them
    d.  Make sure that all copied files have read permissions for
        everyone, otherwise users can't access our update site
    e.  This means the correct rights are `rwxrwsr-x` or
        `2775 `(folders) and `rw-rw-r--` or `664 `(files).

6.  Test the update-site out of an Eclipse of your choice:
    <http://dpp.sf.net/update-beta>
7.  Commit your changes (as
    "`[BUILD] Prepare update site for 13.12.6 beta test`") and push them
    to the release branch. Make sure your commit
    does not contain changes that go beyond these files (if it does,
    unstage them):
    *   `saros.feature/feature.xml`
    *   `saros.update_beta/artifacts.jar`
    *   `saros.update_beta/content.jar`
    *   `saros.update_beta/site.xml`

### How to create a new release

#### Preconditions

*   [Eclipse
    3.6](http://archive.eclipse.org/eclipse/downloads/drops/R-3.6.2-201102101200/)
    for building the release files.
    *   **Do not use Eclipse 3.7**! Eclipse 3.7 seems to have introduced
        an API incompatibility with previous versions via the method
        `org.osgi.framework.Version.compareTo()`. If Saros is compiled
        using Eclipse 3.7 it will not work on earlier versions.
*   Up-to-date working copy and the following projects in your Eclipse
    workspace
    *   Saros Eclipse plugin: `saros`
    *   Saros core project: `saros.core`
    *   Whiteboard plugin: `saros.whiteboard`
    *   Saros Eclipse Feature: `saros.feature`
    *   Saros Eclipse Update Site: `saros.update`
*   A change log of all relevant changes (fixes, features and
    important GUI-changes) since the last release (the test manager can
    be asked for a compiled list of changes up until the release)
*   Eclipse with previous version of Saros installed
    (through update-site)

#### Step-by-Step

Note: This section needs an update to include the Core project.

1.  Prepare the release notes. For [an example look
    here](http://dfn.dl.sourceforge.net/project/dpp/saros/DPP%209.8.21.r1660/release-notes-9.8.21.r1660.txt)
    *   Name the file `README `(this is necessary so it will be
        recognized by Sourceforge as the accompanying readme file).
    *   Include
        *   most important changes as a short natural-language paragraph
            (keep it simple)
        *   notable Regressions (if any)
        *   link to the update site: `http://dpp.sourceforge.net/update`
        *   link to the homepage: `http://www.saros-project.org`
        *   detailed changelog in a language our *users* can understand
        *   Thanks to:
            *   Users who reported bugs and participated in our survey
            *   Everybody who participated

2.  Make sure that you are on the release branch and that everything
    is up-to-date. Use `git reset --hard` followd by git clean -f to
    ensure that you do not have any local changes
3.  In the **Saros** project
    *   Update the `CHANGELOG `file
    *   Update the `credits.txt` file
    *   Go to the version compatibility configuration (file
        `version.comp`) and add a compatibility information
    *   Make sure the `saros.properties` file sets the
        `saros.debug` property to `false`

4.  In each of the projects **Saros** and **Whiteboard**:
    *   Go to `plugin.xml.` Set plugin version number (for instance
        ` 13.12.6`) in the overview tab

5.  Prepare the **Feature project**
    a.  Go to `feature.xml`. Set feature version number (for instance
        ` 13.12.6 `)
    b.  Update plugin version of feature
        1.  Go to tab *Plug-ins*
        2.  Make sure
            `saros.core`, `saros`, and
            `saros.whiteboard` are included
        3.  For all projects, click on *Versions...*
        4.  Select *Copy versions from plugin and fragment manifests*
        5.  Press *Finish*
        6.  (Make sure the version in the overview tab is correct.)

6.  Prepare the **Update Site project**. Edit the `site.xml` manually.
    We only provide the most recent Saros version.
    a.  Replace the version number in the `feature `tag; there are two
        version numbers.
    b.  Replace the version number in the three new `archive `tags (one
        for the `dpp `plugin, one for the core and for the
        `dpp.whiteboard` plugin). Each tag has two version numbers,
        update them too.

7.  Build the update site
    a.  Press *Build* button on the selected new version in *Site Map*
        tab
        *   This will create the two folders `plugins `and
            `features `and create jar files in them.
        *   (NOTE: If they don't appear, it may help if you delete
            `artifacts.jar` and `content.jar`.)

8.  Create release on Sourceforge
    a.  Go to the [File Explorer on
        Sourceforge.net](http://sourceforge.net/projects/dpp/files/)
        (Admin -&gt; File Explorer)
        *   Create a new Folder "`DPP 13.12.6 `"
        *   Upload the `README`
        *   Upload the jars created in the build step from the `plugins`
            directory

9.  Copy update-site to Sourceforge
    a.  login with ssh as `[username],dpp` to
        `web.sourceforge.net` (e.g. using WinSCP)
    b.  copy `index.md`, `site.xm`l, and the
        `saros.feature_13.12.6.jar` from the
        `features/ `folder into `htdocs/update/`
        *   copy `web/` directory to `htdocs/update/` in the unlikely
            case the style files changed

    c.  Make sure all copied files have the right permissions:
        *   Make sure that all copied files have read/write permissions
            for group to allow other developers to overwrite or delete
            them
        *   Make sure that all copied files have read permissions for
            everyone, otherwise users can't access our update site
        *   This means the correct rights are `rwxrwsr-x` or
            `2775 `(folders) and `rw-rw-r--` or `664 `(files).

10. Test the update site:
    a.  Start an existing Eclipse with Saros installed.
        *   Help -&gt; Check for Updates (note that the update site may
            not be updated for some time...)

11. Create dropin archive:
    a.  File -&gt; Export... -&gt; Plug-in Development -&gt; Deployable
        features
    b.  Choose feature
    c.  Destination: Directory
    d.  Choose target directory name: `saros-dropin-13.12.6 `
    e.  Check "Use class files from workspace" (otherwise JUnit won't be
        found which leads to compile errors)
    f.  Click Finish
    g.  In the dropin folder, **delete** the files **artifacts.jar** and
        **content.jar**
    h.  Add the readme:
        This is the Saros Eclipse plugin dropin archive. You can install
        it by unzipping it to eclipse/dropins and restarting Eclipse.
    i.  Create a .zip file from the folder
    j.  Upload archive to SourceForge
        *   Select all operating systems (in order to make the dropin
            archive the default download)

12. Test the dropin archive. (See
    [instructions](../documentation/installation.md).)
13. Commit your changes (as
    "`[BUILD] Changes necessary for Release 13.12.6`") and push the
    commit to Gerrit (on `refs/for/release/13.12.6`). Make sure your
    commit does not contain changes that go beyond these files (unstage
    them otherwise):
    *   `saros.feature/feature.xml`
    *   `saros.update/artifacts.jar`
    *   `saros.update/``content.jar`
    *   `saros.update/``site.xml`
    *   `saros.whiteboard/META-INF/MANIFEST.MF`
    *   `saros/CHANGELOG`
    *   `saros/``META-INF/MANIFEST.MF`
    *   `saros/``credits.txt`
    *   `saros/``saros.properties`
    *   `saros/``version.comp`

### How to announce a new release

*   The announcement mailing-list's address is
    <saros-announce@googlegroups.com>
*   Send the release notes to the list. See the [dpp-announce
    archive](https://sourceforge.net/mailarchive/forum.php?forum_name=dpp-announce) and
    [saros-announce](https://groups.google.com/forum/#!forum/saros-announce) for reference.
*   Always include compatibility information:
    *   *This update breaks compatibility with previous versions
        of Saros. You should not use this version of Saros with
        previous versions.* **OR**
    *   *This update is compatible with all Saros version since 10.9.19*

### How to merge

1.  Make sure to fetch the most recent commits of the release and the
    master branch.
2.  Use `git reset --hard` to set your working copy to the state of
    `origin/master`.
3.  Use Git Merge to merge with the release branch
    (`git merge release/13.12.6`), and the conflicts (`git mergetool`).
    *   There should be conflicts in all `MANIFEST.MF` files because
        there were changes on the release branch as well as on the
        master branch. Always use the master version (e.g.
        ` 14.1.31.DEVEL`).
    *   For all other conflicted files (if any), you may prefer the
        release-branch version because these changes were
        important bug-fixes.
    *   In the unlikely case there were both bug-fixes (release-branch)
        *and* feature-developments (master-branch) in the same files
        there is no general rule. You'll have to use your software
        development skills to find a solution that incorporates both
        change types.

4.  Commit your changes (e.g.
    "`[BUILD] Merge 'release/13.12.6' back into 'master'`"), push your
    change to Gerrit (to `refs/for/master`), and watch for the result of
    the JUnit tests.
    *   In case the JUnit tests or the compilation fails, fix the issue
        locally, amend your commit, and push it again.

5.  In case your conflict-resolving was trivial, you may approve your
    change yourself and submit it directly. The change will be
    integrated as a merge commit.

### How to create a ssh key for Sourceforge on Windows

1.  Execute PuttyGen
2.  select SSH-2 DSA and press Generate
3.  move mouse until new key is generated
4.  press Save private key and choose a filename to save private key on
    disk (ignore warning of empty passphrase)
5.  copy public key into the clipboard
6.  log in on [Sourceforge](https://sourceforge.net/account/login.php)
7.  go to [SSH Keys](https://sourceforge.net/account/ssh)
8.  paste your public key into the area labeled Authorized keys
9.  click Update

### How to make a re-release

*   Sometimes a critical bug is shipped with a release that just
    happened, that makes it necessary to re-release.
*   Since making a release is a lot of work and also is confusing to the
    users, we should use the following criteria to determine whether a
    re-release is appropriate:
    *   Is the release unusable for users due to this bug?
    *   Does the bug represent a regression?
    *   Have less than 72 hours passed since the release?
*   These rules can be bend at will, but it might also make sense to
    just withdraw the release, by updating site.xml

*   How a re-release is done:
    *   The first rule is:
        *   A release is always made from the release branch
    *   To achieve this, remember:
        *   Fixes go to the release branch
        *   Features go to trunk
    *   Once the release branch has been patched and fixed, the release
        is made from the branch. Afterwards the branch is merged to
        trunk to integrate the fixes to trunk.
    *   If another bug is found, repeat:
        *   Fixed go to the release branch
        *   Features stay in trunk
    *   The second rule is:
        *   Don't forget to merge back.

### Updating Dependencies

1.  The JARs should have their version numbers in the name.
2.  Make sure you have copied the new JARs to
    &lt;rootofsarosproject&gt;/lib/&lt;/rootofsarosproject&gt;
3.  right click on your Saros project in eclipse, choose "Build
    Path"-&gt;"Configure Build Path.."
4.  click tab "Libraries"
5.  click button "Add JARs" to add new JARs to the project
6.  click "Ok" and you are ready, maybe you have to rebuild the project
7.  update all relevant information (classpath, build properties, etc.)
    in the plugin.xml
