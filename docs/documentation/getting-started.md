---
title: Getting started
---

{% capture eclipse %}

Here you will find a guideline **on how to set up Saros/E**.

After that you might want to find out **what Saros can do**: Here is the
complete [feature list](features.md).

## The Host

Before we get started you should know that Saros is a host-based system.

To get familiar with this concept check out our comic:

[//]: # This link does not point to a markdown file because Jekyll is unable to convert this nested link into a link with html extension
[![](images/comics/small_6-1_host-comic_frame-1.jpg)
![](images/comics/small_6-1_host-comic_frame-2.jpg)
![](images/comics/small_6-1_host-comic_frame-3.jpg)
![](images/comics/small_6-1_host-comic_frame-4.jpg)
![](images/comics/small_host-comic_frame-5.jpg)
![](images/comics/small_host-comic_frame-6.jpg)](host-comic.html)

------------------------------------------------------------------------

## First steps

### Step 1: Connecting

You need an **XMPP account** to use Saros.

1.  You can **create a new account** within Saros by using the
    FU-Berlin servers.
2.  You can **use an existing account.**
    For example your Google mail address. **This is a
    valid XMPP accounts**.
    To use your Google account you have to allow less secure apps access
    your account, see [here](https://support.google.com/accounts/answer/6010255?hl=en).
    Unfortunately, gmx and web.de accounts do not work currently.
     

To do that simply start the *Saros Configuration* wizard (*Menu &gt;
Saros &gt; Start Saros Configuration*)

![Connecting to
Server](images/1_GettingStartedCreateAccount_0.png)


### Step 2: Adding contacts

To add a contact to your list you need to **know his/her XMPP ID**.

**After entering your partner's XMPP ID**, a message will be sent to
your contact.
Once s/he accepts your request you will be able to see his/her **online
status in your contact list**.

![Adding
buddies](images/2_GettingStartedAddContacts.png)

### Step 3: Starting and joining sessions

You can work together with your contacts by either **starting your own
session** or by **being invited to one**.

When **starting** a session, **Saros will copy the project** to the
computer of your invited contacts.

![Share
Projects](images/3_GettingStartedShareProjects_0.png)

#### Start a session ([host](#the-host))

1.  Right-click on...
    a.  a **project** in your **Package Explorer** and select *Share
        With*
        or
    b.  a **contact** in the **Saros View** and select *Work Together
        on*

2.  Wait for your contacts to accept the **session invitation**

Want to know more about the Saros host role? Check out our comic
[here](host-comic.md).

#### Join a session (client)

1.  Wait for a **session invitation** to appear on your screen
2.  Click *Accept* to **accept the invitation**
3.  Tell Saros **where to save the incoming project**:
    a.  You can create a **new project**, or
    b.  synchronize with an **already existing project** on
        your computer.

4.  Select *Finish* and wait for the project to be copied to your
    computer

#### Additional information:

-   If you accept an invitation and decide to synchronie the incoming
    project with your own copy, Saros will automatically add, change, or
    delete all files as necessary.
-   Saros will share all files which are not marked as *derived* by
    Eclipse, because it should be possible to recreate such files
    (`.class` files for instance) at the client's side. If you use a
    build tool such as Ant, it might be necessary to manually set the
    resulting files or folders to *derived* on both the host's side (so
    they won't be copied to the client) and the client's side (otherwise
    the files will be deleted on synchronization).


### Step 4: Exploring the Saros View

The Saros View consists of three parts

-   the contact list
-   the session list
-   the chat area

The **contact list** is where all **your contacts** are, allowing you to
check their availability, send them instant messages, and invite them to
Saros sessions.

When you are in a Saros session you will see all **participants in the
session list**. Saros also provides **instant chat messaging** to
accompany your sessions.
Whenever you begin a Saros session, a **chat session** is also
automatically started.

![saros\_view\_with\_contextmenu](images/saros_view_with_contextmenu_1.png)

### Step 5: Exploring the Saros Toolbar

#### General

![connect
button](images/saros_connect_button.png)**Connecting:**

Click here to connect or disconnect from the server, or to switch
between user accounts if you have more than one. When you connect, you
will see information about your contacts (including whether they are
connected and also whether they support Saros).

![add buddy
icon](images/saros_addbuddy_button.png)**Add
Contact:**

To add a new contact click here (you will need the full XMPP address of
your contact in the format: `username@server`).

![saros preferences
button](images/preferences_open_tsk.png)**Open
Saros preferences:**

Opens the Saros section of the Eclipse preferences. This is a shortcut
for *Window &gt; Preferences &gt; Saros*.

![saros consistency
button](images/saros_consistency_button_0.png)**Inconsistency
repair:**

Sometimes accidents can happen and your copy of the project can become
out of sync with the host's copy. When it does, this button will light
up. When you click it, the inconsistencies will be repaired.

![follow mode](images/followmode_0.png)**Switch
Follow Mode on/off:**

You can turn the Follow Mode on and of. In Follow Mode Saros will
automatically sync your Eclipse view with the user you are following,
opening shared files the user is opening, and automatically scroll to
the visible part of the editor of open files, so you always see what the
followed user sees.

![saros leave button
](images/saros_leave_button.png)**Leave the
session:**

Click here to leave the current session. If you are the
[host](#the-host), the session will be
closed and all participants will be ejected from the session.

#### Context Menu

![write
access](images/buddy_saros_obj.png)**Grant Write
Access:**

Gives the selected participant write access.

![read-only
access](images/participant_readonly_0.png)**Restrict
to Read-Only Access:**

Removes the write access right from the selected participant. The
affected participant will only have read access afterwards.

![follow mode
icon](images/followmode_0.png)**Follow
Participant:**

Follows the selected participant as described in section [Follow Mode](#follow-mode).

![jump icon](images/jump.png)**Jump to Position:**

Jumps to the cursor position of the selected participant and open the
corresponding file if necessary.

**![open chat image](images/chat_misc.png) Open
chat:**

Opens a chat with this contact on the right side.

![share file
icon](images/saros_sharefile_button.png)**Send
File:**

Gives you the opportunity to select a file to be sent to the selected
participant.

### Good to know

#### User Roles

By default all participants of a session have **writing access**. To
restrict or grant this access to other participants, right-click on that
user in the session list and select *Restrict to read-only access* or
*Grant writing access*. This can **only be done by the
host**.

#### Follow Mode

You can use this feature to follow a single participant as s/he
navigates the project and performs changes. When you follow a
participant:

-   Whenever s/he opens a file on his/her computer, it is opened on
    yours too.
-   Any time s/he switches to view a different file, it is switched
    on yours.
-   As s/he scrolls through a file, the viewpoint is moved on your
    computer also, so that you see what s/he sees.

#### Staying Aware of your Fellow Participants

There are multiple ways of staying aware of what a driver is currently
doing:

-   In the package explorer (or resource navigator):
    - ![active file](images/active_file.png) A colored dot decorates the file that a participant has currently
        in focus. The color resembles the color of the active user.
    - ![shared file](images/shared_file.png) A
        blue arrow decorates a file that is shared with
        other participants.
-   Cursors:
    The position of a participant's cursor appears in the file in
    his/her color.
-   Selections:
    Any text selected by a participant also appears highlighted in
    the file.
-   Changes:
    If a participant writes something, his/her text will appear
    highlighted in his/her color.
-   Locate participants:
    On the right side of editors you can see colored bars representing
    the viewports of each user. These show which part of the file each
    user can see.
-   Follow participants:
    Follow mode allows you to follow all movements of another user as
    s/he moves within and between files. In the Session list, right
    click on the user you wish to follow and select Follow Participant.

{% endcapture %}


{% capture intellij %}

This is the first alpha release of Saros/I, so expect it to be a bit rough around the edges.
There are still some [restrictions](#restrictions) that apply to the usage and some basic [features are still missing](#missing-features).
This release is not compatible with any other Saros plugin/version (like Saros/E).

The GUI used in Saros/I is only a placeholder providing the minimal, necessary functionality.
We are currently working on a replacement in the form of an HTML GUI that can be used across all Saros versions.

## Disclaimer

Saros/I does not include sub-modules when sharing a module (see [Module Restrictions](#module-restrictions)).
As a consequence, such sub-modules might not be present for all session participants.
If a participant deletes a shared directory that contains a sub-module in the local setup of another participant, this sub-module will be deleted without any notice.

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
- follow other participants of the session ([follow mode](features.md#follow-mode))

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


## Restrictions

Some of the implemented features are still subject to some restrictions:

### Working with Multiple Projects

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

### Module Restrictions

You can currently only share a single module. A module has to adhere to the following restrictions to be shareable through Saros:

- The module must have exactly one content root.
- The module must be located under the project content root.
- The module file of the module must be located in the base directory of the content root.
- The module must not be a project module.
- The module file for the module must not contain absolute paths (or paths in general that can't be resolved by other participants, like network drives).

Sharing a module will only share resources belonging to that module, not resources belonging to sub-module located inside a content root of the module.
Creating such a sub-module during a session will lead to an inconsistent state that can not be resolved by Saros.

### Working with newly created modules

To share a newly created module, you will have to have saved your project at least once before trying to start a session.
This is necessary as the module file for a new module is only written to disk the first time the module is saved.

You can check if the module file was written to disk by looking at the base directory of the module. It should contain a `*.iml` file with the same name as the module.

### Number of Participants

Currently, Saros/I is restricted to two-participant sessions, meaning you can only create session containing the host and a single client.


## Missing Features

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

### Missing Secondary Features

These are features that are part of the functionality provided by Saros/E but are not seen as a crucial aspects of the plugin and are therefore have a lower priority:

- Partial sharing
- Saros help entry in menu-bar
- Whiteboard
- Chat

(Both the chat and the whiteboard will be available in Saros/i with the introduction of the new HTML GUI.)

## Known Bugs

There are some bugs in the alpha version of Saros/I that we are already aware of and that are going to be fixed in a later release. Some notable bugs are mentioned here. For a full overview, you can have a look at our [issue tracker](https://github.com/saros-project/saros/issues?q=is%3Aissue+label%3A%22Area%3A+IntelliJ%22+label%3A%22Type%3A+Bug%22+is%3Aopen).

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
They are located in `~/.IdeaXXXXXXXX/system/log/` and are named `idea.log` (the log will be truncated at some point and older logs will be moved to `idea.log.1`, etc.).
Please have a look at the contained timestamps to provide the correct file.

Before attaching any log files, please make sure to redact any private information that you do not wish to make publicly available.

{% endcapture %}

{% include ide-tabs.html eclipse=eclipse intellij=intellij %}
