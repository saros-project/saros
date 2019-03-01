---
title: Getting Started
---

Here you will find a guideline **on how to set up Saros**.

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
