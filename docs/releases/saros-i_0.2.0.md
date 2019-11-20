---
title: Saros/I 0.2.0 Release Notes
---

This is the second alpha release of Saros/I, so expect it to still be a bit rough around the edges.
There are still some [restrictions](#restrictions) that apply to the usage and some basic [features are still missing](#missing-features).


## Disclaimer

Saros/I does not include sub-modules when sharing a module (see [module restrictions](#module-restrictions)).
As a consequence, such sub-modules might not be present for all session participants.
If a participant deletes a shared directory that contains a sub-module in the local setup of another participant, this sub-module will be deleted without any notice.

Furthermore, there are still some known bugs in the current release. Please have a look at the section [Known Bugs](#known-bugs).

## Installation

Saros/I 0.2.0 requires
 - `JDK 8` or newer
 - `IntelliJ 2018.2.7` or newer

Saros/I can be installed from the JetBrains plugin repository or from disk. A detailed guide is given [here](../documentation/installation.html?tab=intellij).

## Compatibility

The current release `0.2.0` is not compatible with the previous Saros/I release (`0.1.0`) or other Saros plugins (like Saros/E).

## Changes

- Made plugin project-independent
  - Previously, the plugin was project bound. This lead to issues when changing the project. With the plugin now being loaded on the application scope, project changes are now handled correctly. Furthermore, the Saros UI is not synchronized across all project windows.
- Removed module name restrictions
  - The client can now choose any name for the shared module or can use any existing module (adhering to the remaining module restrictions).
- Removed module location restrictions
  - The shared module can now be located anywhere in the file system.
- Removed module file location restrictions
  - The shared module can now have a module file located anywhere in the file system.
- Improved module sharing logic
  - Makes the logic module file independent.
  - Saros sessions now no longer include the module files.
  - The negotiation now also transfers which folders are marked as "Source", "Test Source", "Resource", and "Test Resource" and adjusts the client module accordingly.
- Improved Negotiation UI
  - The user can now choose which open project (and which module of that project) to share.
  - The client can now choose which path to use when creating a new module as part of the session.
  - Added a drop-down menu for existing project to the wizard to accept the session invitation.
  - Adjusted the wizard to accept the session invitation to better communicate which fields contain illegal values.
- Added custom color support
  - The user can now to customize the Saros annotation colors.
- Improved IDE color scheme support
  - Fixed UI-elements that were not correctly adjusted to different color schemes.
  - Added preset Saros annotation colors for the "Darcula" color scheme to improve readability.
- Minor bug fixes

## Features

This alpha version provides most of the basic functionality of Saros.
You can

- add existing XMPP-accounts
- start a session with another person
  - Sessions in Saros/I are currently limited to two participants (host and one client)
- share exactly one module through Saros; the shared module must meet the restrictions described [here](#module-restrictions)
- transfer the initial content of the module shared by the host to all participating clients
- work on shared resources
- create, delete, and move resources in the shared module
- interact freely with non-shared resources
- follow other participants of the session ([follow mode](../documentation/features.md#follow-mode))

For a guide on how to use Saros/I, have a look at our [Getting Started](../documentation/getting-started.html?tab=intellij#how-to-use-sarosi) page.

## Restrictions

Some of the implemented features are still subject to some restrictions:

### Module Restrictions

You can currently only share a single module. A module has to adhere to the following restrictions to be shareable through Saros:

- The module must have exactly one content root.
- The module must not be a project module.

Sharing a module will only share resources belonging to that module, not resources belonging to sub-module located inside a content root of the module.
Creating such a sub-module during a session will lead to an inconsistent state that can not be resolved by Saros.

### Working With Newly Created Modules

To share a newly created module, you will have to have saved your project at least once before trying to start a session.
This is necessary as the module file for a new module is only written to disk the first time the module is saved.

You can check if the module file was written to disk by looking at the base directory of the module. It should contain a `*.iml` file with the same name as the module.

### Excluded Resources Are Not Ignored

Saros/I does not currently ignore excluded resources. Instead, such resources will also be shared with the other participants.

### Number of Participants

Currently, Saros/I is restricted to two-participant sessions, meaning you can only create session containing the host and a single client.


## Missing Features

As this is only the first alpha release, there are still a lot of main features that are not yet implemented:

- Multi-user sessions
- Sharing multiple modules
- Sharing whole projects
- Display viewport annotations
- Display file awareness annotations
- Display cursor annotation
- Display user color in Saros view
- Adjustable Saros settings (besides colors)
- Creation, management or deletion of XMPP accounts

### Missing Secondary Features

These are features that are part of the functionality provided by Saros/E but are not seen as a crucial aspects of the plugin and are therefore have a lower priority:

- Partial sharing
- Saros help entry in menu-bar
- Whiteboard
- Chat

## Known Bugs

There are some bugs in the alpha version of Saros/I that we are already aware of and that are going to be fixed in a later release. Some notable bugs are mentioned here. For a full overview, you can have a look at our [issue tracker](https://github.com/saros-project/saros/issues?q=is%3Aissue+label%3A%22Area%3A+IntelliJ%22+label%3A%22Type%3A+Bug%22+is%3Aopen).

- [#116](https://github.com/saros-project/saros/issues/116) - The position of local text selection is not updated correctly for closed files when text edits are received through Saros.
- [#223](https://github.com/saros-project/saros/issues/223) - Deleting and then re-creating a file with the same name (or moving a file and then moving it back, etc.) causes the session to desynchronize irreparably, requiring a session restart.
- [#683](https://github.com/saros-project/saros/issues/683) - Creating a file with an unknown file extension (or without a file extension) leads to a session desynchronization. Opening the file on the other side, choosing a file type, and then running the recovery might repair the session, but the state could also be irreparable, requiring a session restart.
- [#684](https://github.com/saros-project/saros/issues/683) - Renaming the content root of the shared module leads to the content being deleted for all other participants.

### Report a Bug

If you encounter any other bugs not mentioned above, we would appreciate it if you would report them to our issue tracker (after checking that they have not already been reported).

Our current bug tracker can be found on our [GitHub page](https://github.com/saros-project/saros/issues).
Please make it clear that the issue is dealing with Saros/I.

When reporting a bug that concerns the plugin behavior, please provide the Saros log file (or all relevant excerpts) for a session where the bug was encountered.
The log files can be found in the IntelliJ settings directory, which usually resides in the home directory under `~/.IdeaXXXXXXXX/` (".IdeaXXXXXXXX" designates the used IntelliJ release; for IntelliJ IDEA Community Edition 2018.2, this would be `.IdeaIC2018.2`).
The log files are located in `~/.IdeaXXXXXXXX/system/log/SarosLogs/`.

If you are encountering IntelliJ IDEA errors connected to Saros (which will be displayed by a red, blinking symbol in the bottom right corner of the IntelliJ project view; the error can be viewed in more detail by clicking the symbol), please also include the IntelliJ IDEA logs.
They are located in `~/.IdeaXXXXXXXX/system/log/` and are named `idea.log` (the log will be truncated at some point and older logs will be moved to `idea.log.1`, etc.).
Please have a look at the contained timestamps to provide the correct file.

Before attaching any log files, please make sure to redact any private information that you do not wish to make publicly available.

