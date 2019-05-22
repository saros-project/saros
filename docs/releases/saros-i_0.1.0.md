---
title: Saros/I 0.1.0 Release Notes
---

This is the first alpha release of Saros/I, so expect it to be a bit rough around the edges.
There are still some [restrictions](#restrictions) that apply to the usage and some basic [features are still missing](#missing-features).
This release is not compatible with any other Saros plugin/version (like Saros/E).

The GUI used in Saros/I is only a placeholder providing the minimal, necessary functionality.
We are currently working on a replacement in the form of an HTML GUI that can be used across all Saros versions.

## Disclaimer

Saros/I does not include sub-modules when sharing a module (see [Module Restrictions](#module-restrictions)).
As a consequence, such sub-modules might not be present for all session participants.
If a participant deletes a shared directory that contains a sub-module in the local setup of another participant, this sub-module will be deleted without any notice.

## Installation

Saros/I 0.1.0 requires
 - `JDK 8` or newer
 - `IntelliJ 2018.2.7` or newer

Saros/I can be installed from the JetBrains plugin repository or from disk.

### Step-by-Step Guide

- Choose "Settings..." > "Plugins".
- Choose a source to install from:
  - *From the plugin repository:* Select the tab "Marketplace"
    - If you have not already done so, add the `alpha` release channel to your list of plugin repositories. A guide on how to do this is given [here](https://www.jetbrains.org/intellij/sdk/docs/plugin_repository/custom_channels.html#configuring-a-custom-channel-in-intellij-platform-based-ides).
    - Search for "Saros" in the search bar and select the entry from the list.
    - Click the "Install" button.
    - Close the settings menu.
  - *From disk:* Click the settings icon (gear/cog) and choose "Install plugin from disk...".
    - Download the plugin zip from our [release page](https://github.com/saros-project/saros/releases).
    - Navigate to the directory containing the plugin zip.
    - Select the zip file.
    - Click OK
- Restart IntelliJ.

## Features

This alpha version provides most of the basic functionality of Saros.
You can

- add existing XMPP-accounts
- start a session with another person
  - Sessions in Saros/I are currently limited to two participants (host and one client)
- share exactly one module through Saros; the shared module must meet the restrictions set [here](#module-restrictions)
- transfer the initial content of the module shared by the host to all participating clients
- work on shared resources
- create, delete, and move resources in the shared module
- interact freely with non-shared resources
- follow other participants of the session ([follow mode](../documentation/getting-started.md#follow-mode))

### Restrictions

Some of the implemented features are still subject to some restrictions:

#### Working with Multiple Projects

Saros/I works with multiple projects open but does not allow the user to actively choose which project to use. By default, the first opened project will be used by Saros.
As the current solution is only a workaround while a complete fix is implemented, we would advise against opening multiple projects when working with Saros.

If you are still determined to work with multiple projects open, please have a look at the details of the workaround to understand its restrictions, how to share a module of a specific project and how to recover from a headless state.

<details>

With the current workaround introduced in [PR #417](https://github.com/saros-project/saros/pull/417), Saros/I always holds a reference to a specific IntelliJ project.
Only modules of this project can be shared.

The held project is determined on runtime as follows:
- Initially, the first opened IntelliJ project is used.
- Whenever a new project is opened, Saros checks if the held project object is still valid (if the project represented by the object is still open).
If the object is no longer valid, it will be replaced with the newly opened project.
As a result, you can now only share modules of the newly opened project.

If you only have one project open at a time or always open the project you want to share modules of first, this restriction should not be a noticeable.

If you want to change the shareable project, you will have to close the currently shareable project and then open the project you want to share modules of.
If you have a hard time figuring out which project is currently selected as shareable, you can resort to closing all open projects before opening the project to share.

The functionality described above still leaves the possibility of entering a headless state:
If you close the currently shareable project and then don't open a new project, Saros is left in a headless state where it does not have a valid reference to a project.
Trying to start a session in this state will lead to exceptions. This headless state can be resolved by opening a new project.

</details>

#### Module Restrictions

You can currently only share a single module. A module has to adhere to the following restrictions to be shareable through Saros:

- The module must have exactly one content root.
- The module must be located under the project content root.
- The module file of the module must be located in the base directory of the content root.
- The module must not be a project module.
- The module file for the module must not contain absolute paths (or paths in general that can't be resolved by other participants, like network drives).

Sharing a module will only share resources belonging to that module, not resources belonging to sub-module located inside a content root of the module.
Creating such a sub-module during a session will lead to an inconsistent state that can not be resolved by Saros.

#### Working with newly created modules

To share a newly created module, you will have to have saved your project at least once before trying to start a session.
This is necessary as the module file for a new module is only written to disk the first time the module is saved.

You can check if the module file was written to disk by looking at the base directory of the module. It should contain a `*.iml` file with the same name as the module.

#### Number of Participants

Currently, Saros/I is restricted to two-participant sessions, meaning you can only create session containing the host and a single client.


### Missing Features

As this is only the first alpha release, there are still a lot of main features that are not yet implemented:

- Multi-user sessions
- Sharing multiple modules
- Sharing whole projects
- Selecting where to create modules when starting a Saros session
- Display viewport annotations
- Display file awareness annotations
- Display cursor annotation
- Display user color in Saros view
- Adjustable Saros settings (like user colors, etc.)
- Creation, management or deletion of XMPP accounts

#### Missing Secondary Features

These are features that are part of the functionality provided by Saros/E but are not seen as a crucial aspects of the plugin and are therefore have a lower priority:

- Partial sharing
- Saros help entry in menu-bar
- Whiteboard
- Chat

(Both the chat and the whiteboard will be available in Saros/i with the introduction of the new HTML GUI.)

## Known Bugs

There are some bugs in the alpha version of Saros/I that we are already aware of and that are going to be fixed in a later release.

- [#116](https://github.com/saros-project/saros/issues/116) - The position of local text selection is not updated correctly for closed files when text edits are received through Saros.
- [#223](https://github.com/saros-project/saros/issues/223) - Deleting and then re-creating a file with the same name (or moving a file and then moving it back, etc.) causes the session to de-synchronize irreparably, requiring a session restart.


### Report a Bug

If you encounter any other bugs not mentioned above, we would appreciate it if you would report them to our issue tracker (after checking that they have not already been reported).

Our current bug tracker can be found on our [GitHub page](https://github.com/saros-project/saros/issues).
Please make it clear that the issue is dealing with Saros/I.

When reporting a bug that concerns the plugin behavior, please provide the Saros log file (or all relevant excerpts) for a session where the bug was encountered.
The log files can be found in the IntelliJ settings directory, which usually resides in the home directory under `~/.IdeaXXXXXXXX/` (".IdeaXXXXXXXX" designates the used IntelliJ release; for IntelliJ IDEA Community Edition 2018.2, this would be `.IdeaIC2018.2`).
The log files are located in `~/.IdeaXXXXXXXX/system/log/SarosLogs/`.

If you are encountering IntelliJ IDEA errors connected to Saros (which will be displayed by a red, blinking symbol in the bottom right corner of the IntelliJ project view; the error can be viewed in more detail by clicking the symbol), please also include the IntelliJ IDEA logs.
They are located in `~/.IdeaXXXXXXXX/system/log/` and are named `idea.log`, `idea.log.1`, etc.
Please have a look at the contained timestamps to provide the correct file.

Before attaching any log files, please make sure to redact any private information that you do not wish to make publicly available.


## How to Use Saros/I

### Adding an XMPP Account

- Choose the Saros window in bottom bar.
  - If it is not shown, click the "Saros" button in bottom bar or open it by clicking on the square on bottom left and choosing "Saros".
- In the top bar of the Saros view, choose the "Connect" icon (connected plug icon; the left icon).
- Choose "Add account..." from the pop-up menu.
- Enter the fully qualified user name (`USER_NAME@YOUR.DOMIAN`).
  - If the basic Saros XMPP server is used, the domain would be `@saros-con.imp.fu-berlin.de`.
- Click OK.
- Enter your password.
- Click OK.
- Enter the XMPP server address (optional).
  - This option should not be necessary in most cases.
  - If no server is supposed to be specified, just leave the field empty.
  - If a server is specified, a port has to be specified as well.

**NOTE:** As mentioned in the section [missing features](#missing-features), Saros/I does currently not support the creation, management or deletion of XMPP accounts.
As only people on your friends list can be invited to join your Saros session, you will have to create an account and add friends to your friends list through a different client.
Any XMPP client (including Saros/E) can be used for this purpose.

If you accidentally made a typo while entering your username or password, the created account entry can also only be changed or deleted through Saros/E (or by deleting the account store `~/.saros/config.dat`).
Saros/E does not permit the deletion of the currently chosen account.
If you only added one account and would like to remove it, you will have to add a second account (for example with random values) and choose this new account as the default.
You can then delete the first account, add a new account with the right values, choose it as the correct default and delete the temporary account entry.


### Starting a Session - Host

- Choose the Saros window in the bottom bar.
- Choose the "Connect" icon (connected plug icon; the left icon).
- Choose an account.
- Choose the section "Contacts" in the window on the left side of the Saros view.
- Choose a friend that is online.
- Right-click the name of that friend. This will open a list of all shareable modules in the current project.
  - If the module you would like to share is not listed, it most likely does not adhere to the mentioned restrictions (see "What should work" and "What does not work").
- Choose the module that is supposed to be shared from the displayed list of modules.


### Starting a Session - Client

- Choose the Saros window in the bottom bar.
- Choose the "Connect" icon (connected plug icon; the left icon).
- Choose an account.
- Wait until the host invites you to join their session.
- After the host invited you, the session negotiation will open.
- Click "Next". This will open the project negotiation.
- Choose which local module to use for the session. The automatically chosen option and value should be correct in most cases, so you should not have to change anything.
  - *To create a new module:* Choose "Create new module" if the shared module is not already present in your local project.
  - *To use an existing module:* Choose "Use existing module" if a version of the shared module is already present in your local project.
    - Click on "Browse..." and choose the base directory of the shared module.
    - The base directory has to have the same name as the shared module.
    - If the field is left empty, the project negotiation is aborted due to a local error.
- Click "Next". This will show you the local file changes that will be made during the negotiation. These are the differences between the local version of the module and the version held by the host.
  - The shown actions are the actions necessary to align the local module with the host module.
  - Any local differences will be removed during the project negotiation. These adjustments will only be done if the "Finish" button is selected. If the session negotiation is aborted at this stage, no local files are changed.
- Click "Finish".


### Leaving a Session

- Choose the Saros window in the bottom bar.
- Click on "Leave session" (door icon; the right icon).
- Select "OK".
  - If you are the host of the session, the client will subsequently be kicked from the session.


### Disconnecting from the XMPP-Server

- Choose the Saros window in the bottom bar.
- Choose the "Connect" icon (connected plug icon; the left icon).
- Choose "Disconnect server".

