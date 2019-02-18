---
title: Frequently asked questions about the Saros project
---

This page lists common questions about Saros. If your question is not
answered here you should look aroud the
[Support](../support/) area. There you will
find more information that may help you.

## General

### Why is the software called Saros?

A [Saros cycle is an eclipse
cycle](http://en.wikipedia.org/wiki/Saros_cycle), i.e. after one Saros
cycle the Sun, Earth, and Moon return to approximately the same relative
geometry. When creating the plug-in, Riad Djemili thought this would
make a nice name for an Eclipse plug-in about Pair Programming, where
driver and observer cycle their roles while revolving around code of
interest.

### How does Saros compare to screen sharing (e.g. VNC)?

**Advantages**:

-   Saros requires much less bandwidth (as it transfers only editing
    commands, not screen contents)
-   Each Saros participant can use a different screen resolution,
    different IDE-settings, keyboard layout -- and soon, even [different
    IDEs](saros-for-intellij.md).
-   Saros does not slavishly chain users together. For instance, Saros
    allows multiple participants to write to the same file in different
    spots at the same time. It also allows one participant to look
    something up (in the same or a different file) while another user
    is writing.
-   Saros scales to more than two participants naturally.

**Disadvantages**:

-   Saros does not support sharing information which occur outside of
    the IDE (so even all participants can run the application under
    development independently, they cannot see the others doing so).
-   Saros requires each participant to have an Eclipse
    installation setup.

### How many users does Saros support?

Saros supports up to 5 users. However, this is not a hard limit. But the
sixth and seventh and n-th user will get the same gray-ish color.

### On which operating systems does Saros work?

In theory, the same version of Saros will run on any platform for which
there is a version of Eclipse. We generally perform our testing on
multiple flavours of Windows, Linux and Mac OS X.

### I'm working with a version control system, can I use Saros?

**SVN:** Yes, you can. In theory, there should be no problems since so
called *derived files* are not shared by Saros. Usually, the
corresponding Eclipse plugins (such as Subclipse) take care of setting
the "derived" attribute for ".svn" directories (as well as Java ".class"
files, which are also not synced).
If in doubt or if you don't use such plugins, you can always set this
attribute on your own: Just right-click on the respective directory,
select "Properties", and check the attribute "Derived".

**Git:** To make completely sure that Saros won't mess with your
precious versioning data, you might consider a folder layout where your
Eclipse project(s) reside on a level below the .git folder (see below).
That's the way we organize our own source code (even though we do so for
other reasons).
We're working on Saros's Git-friendliness so it also supports
repositories adhering to the one-project-per-repository policy.

**Safe layout** ("multiple projects per repository")
```
my-project
  .git/
  module-a/  <-- totally safe to share
    src/
    test/
  module-b/  <-- so is this one
    src/
    test/
```

**Risky Layout** ("one project per repository"")
```
my-project/  <-- you shouldn't share this one
  .git/
  src/
  test/
  readme.html
```

## Network Issues

### How much bandwidth do you need to run Saros?

The real bandwidth problem is usually not during a Saros session, but
rather with the project synchronization needed for starting one, which
may take quite long (many minutes) for larger projects over a
low-bandwidth connection. Make sure all participants have local copies
of the project that are not too different before the session starts.

### Does Saros work between two home users? Or through two firewalls?

Home users (using DSL) and most corporate networks employ
[NAT](http://en.wikipedia.org/wiki/Network_address_translation).
Computers behind a NAT can open TCP connections to other computers, but
other computers cannot open TCP connections to them.

So how can two Saros users that both sit behind a NAT create a joint
Saros session?

-   Saros uses an [XMPP](http://en.wikipedia.org/wiki/XMPP) server to
    get in contact with the other participant(s). Companies can [run
    their own XMPP server](setup-xmpp.md) for maximum privacy; home
    users can use almost any public XMPP server.
-   Thoughout the session (if possible) Saros uses
    [Socks5](http://en.wikipedia.org/wiki/SOCKS) connections (direct
    or mediated) between the participants. Saros supports optional [UPnP
    port
    forwarding](http://en.wikipedia.org/wiki/Universal_Plug_and_Play#NAT_traversal)
    to improve chances of direct connections. (see Saros
    network preferences)

### How to use a Google account / Google Talk service with Saros?

We don't recommend to use you Google account for XMPP anymore, since
Google has droped Google Talk XMPP support with the introduction of
Google Hangouts.

### Is Saros language dependent or which languages does Saros support?

No, Saros is not language-dependent as it works on a character-level.

------------------------------------------------------------------------

In case of problems see [troubleshooting](troubleshooting.md).
