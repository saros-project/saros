---
title: Saros/I 0.3.0 Release Notes
---

This is an alpha release of Saros/I, so expect it to still be a bit rough around the edges.
There are still some [restrictions](#restrictions) that apply to the usage and some basic [features are still missing](#missing-features).


## Disclaimer

Saros/I does not include sub-modules when sharing a module (see [module restrictions](#module-restrictions)).
As a consequence, such sub-modules might not be present for all session participants.
If a participant deletes a shared directory that contains a sub-module in the local setup of another participant, this sub-module will be deleted without any notice.

Furthermore, there are still some known bugs in the current release. Please have a look at the section [Known Bugs](#known-bugs).

## Installation

Saros/I 0.3.0 requires
 - `JDK 8` or newer
 - `IntelliJ 2019.2.3` or newer
   - Other [IDEs based on the IntelliJ platform](https://www.jetbrains.org/intellij/sdk/docs/intro/intellij_platform.html#ides-based-on-the-intellij-platform) version `2019.2.3` or newer are supported as well

Saros/I can be installed from the JetBrains plugin repository or from disk. A detailed guide is given [here](../documentation/installation.html?tab=intellij).

## Compatibility

The current release `0.3.0` is not compatible with the previous Saros/I releases or other Saros plugins (like Saros/E).

## Changes

- Bumped minimal required version to `2019.2.3`.
- Enable support for all [IDEs based on the IntelliJ platform](https://www.jetbrains.org/intellij/sdk/docs/intro/intellij_platform.html#ides-based-on-the-intellij-platform).
- Adds support for caret/cursor annotations.
- Adjusts the selection annotation logic to correctly display backwards selections.
- Fixed [#223](https://github.com/saros-project/saros/pull/223) - Re-creating a file deleted during a session now no longer leads to a desynchronization.
- Fixed [#711](https://github.com/saros-project/saros/pull/711) - Editors for shared non-text resources are now ignored by Saros.
- Fixed [#821](https://github.com/saros-project/saros/pull/821) - Disconnecting from the XMPP account during a session now no longer freezes the UI.
- Fixed [#891](https://github.com/saros-project/saros/pull/891) - Users are now notified about failed connection attempts to the XMPP server instead of just failing silently.
- Fixed [#922](https://github.com/saros-project/saros/pull/922) - Saros now explicitly ignores '.git' folders.
- Fixed [#931](https://github.com/saros-project/saros/pull/931) - Saros now saves all modified documents on session start instead of only the ones with open editors.

## Features

This alpha version provides most of the basic functionality of Saros.
You can

- add existing XMPP-accounts
- add contacts to XMPP-accounts
- start a session with another person
  - Sessions in Saros/I are currently limited to two participants (host and one client)
- share exactly one module through Saros; the shared module must meet the restrictions described [here](#module-restrictions)
- transfer the initial content of the module shared by the host to all participating clients
- work on shared resources
- create, delete, and move resources in the shared module
- interact freely with non-shared resources
- follow other participants of the session ([follow mode](../documentation/features.md#follow-mode))

For a guide on how to use Saros/I, have a look at our [Getting Started](../documentation/getting-started.html?tab=intellij) page.

## Restrictions

Some of the implemented features are still subject to some restrictions:

### Module Restrictions

You can currently only share a single module. A module has to adhere to the following restrictions to be shareable through Saros:

- The module must have exactly one content root.

Sharing a module will only share resources belonging to that module, not resources belonging to sub-module located inside a content root of the module.
Creating such a sub-module during a session will lead to an inconsistent state that can not be resolved by Saros. See [Known Bugs](#known-bugs).

### Working With Newly Created Modules

To share a newly created module, you will have to have saved your project at least once before trying to start a session.
This is necessary as the module file for a new module is only written to disk the first time the module is saved.

You can check if the module file was written to disk by looking at the base directory of the module. It should contain a `*.iml` file with the same name as the module.

### Resource Exclusion Options Are Not Shared

Saros/I does not currently share which resources are marked as 'Excluded' with other participants. This can lead to a situation where another participant creates a resource on their side that already exists as an excluded resource locally. This leads to a session desync. See [Known Bugs](#known-bugs).

### Number of Participants

Currently, Saros/I is restricted to two-participant sessions, meaning you can only create session containing the host and a single client.


## Missing Features

As this is only the first alpha release, there are still a lot of main features that are not yet implemented:

- Multi-user sessions
- Sharing multiple modules
- Sharing whole projects
- Display viewport annotations
- Display file awareness annotations (feature request [#114](https://github.com/saros-project/saros/issues/114))
- Display user color in Saros view
- Adjustable Saros settings (besides colors)
- Creation, management or deletion of XMPP accounts

### Missing Secondary Features

These are features that are part of the functionality provided by Saros/E but are not seen as a crucial aspects of the plugin and are therefore have a lower priority:

- Chat

## Known Bugs

There are some bugs in the alpha version of Saros/I that we are already aware of and that are going to be fixed in a later release. Some notable bugs are mentioned here. For a full overview, you can have a look at our [issue tracker](https://github.com/saros-project/saros/issues?q=is%3Aissue+label%3A%22Area%3A+IntelliJ%22+label%3A%22Type%3A+Bug%22+is%3Aopen).

- [#116](https://github.com/saros-project/saros/issues/116) - The position of local text selection is not updated correctly for closed files when text edits are received through Saros.
- [#610](https://github.com/saros-project/saros/issues/610) - Contact entries in the Saros view are not sorted correctly.
- [#683](https://github.com/saros-project/saros/issues/683) - Creating a file with an unknown file extension (or without a file extension) leads to a session desynchronization. Opening the file on the other side, choosing a file type, and then running the recovery might repair the session, but the state could also be irreparable, requiring a session restart.
- [#698](https://github.com/saros-project/saros/issues/698) - Creating a submodule in a shared directory leads to a session desync.
- [#699](https://github.com/saros-project/saros/issues/699) - Which resource are marked as excluded is not shared between participants.
- [#707](https://github.com/saros-project/saros/issues/707) - Client line endings are overwritten with host line endings when starting a session.
- [#822](https://github.com/saros-project/saros/issues/822) - The state of the Saros view is not reset correctly when the the host is unexpectedly disconnected from the XMPP server.
- [#888](https://github.com/saros-project/saros/issues/888) - Session and Contacts tab in Saros view is collapsed when non-host participant leaves the session.
- [#958](https://github.com/saros-project/saros/issues/958) - Annotations remain in editor after session end if file was open in multiple editors.
- [#962](https://github.com/saros-project/saros/issues/962) - Saros annotations overshadow the local selection.
- [#964](https://github.com/saros-project/saros/issues/964) - Edits can remove caret annotations.

### Report a Bug

If you encounter any other bugs not mentioned above, we would appreciate it if you would report them to our issue tracker (after checking that they have not already been reported).

Our bug tracker can be found on our [GitHub page](https://github.com/saros-project/saros/issues).
Please make clear that the issue pertains to Saros/I.

When reporting a bug that concerns the plugin behavior, please provide the Saros log file (or all relevant excerpts) for a session where the bug was encountered.

The log files for [IntelliJ platform based IDEs](https://www.jetbrains.org/intellij/sdk/docs/intro/intellij_platform.html#ides-based-on-the-intellij-platform) are located in the IDE system directory (here called `IDE_SYSTEM_DIR`).
An overview over all configurations is given on [the support forum](https://intellij-support.jetbrains.com/hc/en-us/articles/206544519-Directories-used-by-the-IDE-to-store-settings-caches-plugins-and-logs).
For specific releases, see the information for [2019.3 and earlier](https://www.jetbrains.com/help/idea/2019.3/tuning-the-ide.html#system-directory) or [2020.1 and later](https://www.jetbrains.com/help/idea/2020.1/tuning-the-ide.html#system-directory).

The Saros log files are located in `[IDE_SYSTEM_DIR]/log/SarosLogs/*.log`.

If you are encountering IDE errors connected to Saros (which will be displayed by a red, blinking symbol in the bottom right corner of the project view; the error can be viewed in more detail by clicking the symbol), please also include the IDE logs.
They are located in `[IDE_SYSTEM_DIR]/log/` and are named `idea.log` (the log will be truncated at some point and older logs will be moved to `idea.log.1`, etc.).
Please have a look at the contained timestamps to provide the correct file.

Before attaching any log files, please make sure to redact any private information (such as project or file names) that you do not wish to make publicly available.
