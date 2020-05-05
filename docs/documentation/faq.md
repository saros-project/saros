---
title: Frequently Asked Questions
---

This page lists common questions about Saros and known issues. If your question is not
answered here you should look around the [Contact](/contact) area.
There you will find more information that may help you.

## General

{% accordion general %}

{% collapsible Why is the software called Saros? %}

A [Saros cycle is an eclipse
cycle](http://en.wikipedia.org/wiki/Saros_cycle), i.e. after one Saros
cycle the Sun, Earth, and Moon return to approximately the same relative
geometry. When creating the plug-in, Riad Djemili thought this would
make a nice name for an Eclipse plug-in about Pair Programming, where
driver and observer cycle their roles while revolving around code of
interest.

{% endcollapsible %}
{% collapsible Is Saros language dependent or which languages does Saros support? %}
No, Saros is not language-dependent as it works on a character-level.
{% endcollapsible %}
{% collapsible How does Saros compare to other collaborative real-time editing tools? %}

These tables compare known alternatives to Saros.

In the following we are using these definitions:
* **Open-Source** - A software using a license contained in the [list of approved licenses of the Open Source Initiative](https://opensource.org/licenses/alphabetical)
* **Commercial** - It is not possible to use the software for free without limitations.
* **Freeware** - You can use the software for free, but the code is not published.
* **Self-hosted** - The possibility to host a corresponding server that manages the connection or also the shared workspaces.

#### Web-Based IDEs

|Name         |Category    |Self-hosted |
|-------------|------------|------------|
|CodeEnvy     |Commercial  |No          |
|Cloud9       |Commercial  |No          |

#### Plug-Ins

|Name         |IDE                                              |Category    |Self-hosted |
|-------------|-------------------------------------------------|------------|------------|
|FlooBits     |Atom, Emacs, IntelliJ IDEA, Neovim, Sublime Text |Commercial  |Yes         |
|Saros        |Eclipse, IntelliJ IDEA                           |Open-Source |Yes         |
|Live Share   |Visual Studio (Code)                             |Freeware    |No          |
|Teletype     |Atom                                             |Open-Source |Yes         |

#### Stand-Alone Editors

|Name         |OS                  |Category    |Self-hosted |
|-------------|--------------------|------------|------------|
|Gobby        |Win, Linux, Mac OS  |Open-Source |Yes         |
|SubEthaEdit  |Mac OS              |Open-Source |Yes         |

{% endcollapsible %}

{% collapsible How does Saros compare to screen sharing (e.g. VNC)? %}

**Advantages**:

-   Saros requires much less bandwidth (as it transfers only editing
    commands, not screen contents)
-   Each Saros participant can use a different screen resolution,
    different IDE-settings, keyboard layout -- and even [different
    IDEs](https://www.saros-project.org/documentation/installation.html?tab=intellij).
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

{% endcollapsible %}
{% collapsible How many users does Saros support? %}

Saros supports up to 5 users (see [here](/releases/saros-i_0.2.2.html#number-of-participants) for the current restrictions in IntelliJ).
However, this is not a hard limit. But the sixth and seventh and n-th user will get the same gray-ish color.

{% endcollapsible %}
{% collapsible On which operating systems does Saros work? %}

In theory, the same version of Saros will run on any platform for which
there is a version of Eclipse/IntelliJ. We generally perform our testing on
multiple flavours of Windows, Linux and Mac OS X.

{% endcollapsible %}
{% collapsible I'm working with a version control system, can I use Saros? %}

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
├── .git
├── module-a  <-- totally safe to share
│   ├── src
│   ├── test
└── module-b  <-- so is this one
    ├── src
    └── test
```

**Risky Layout** ("one project per repository"")
```
my-project  <-- you shouldn't share this one
├── .git
├── src
├── test
└── readme.html
```
{% endcollapsible %}
{% collapsible Where can I find the Saros log files? %}
{% capture eclipse %}

You can find the Saros related logs in your eclipse workspace. The current
workspace directory is shown if you open `"Switch Workspace" > "Other..."`.

* IDE logs - `<workspace>/.metadata/.log`
* Saros logs - `<workspace>/.metadata/.plugins/saros.eclipse/log/<date>/*.log`

{% endcapture %}
{% capture intellij %}

The log files for JetBrains IDEs are located in the IDE system directory (here called `IDE_SYSTEM_DIR`).
An overview over all configurations is given on https://intellij-support.jetbrains.com/hc/en-us/articles/206544519-Directories-used-by-the-IDE-to-store-settings-caches-plugins-and-logs

For releases 2019.3 and earlier: https://www.jetbrains.com/help/idea/2019.3/tuning-the-ide.html#system-directory
For release 2020.1 and later: https://www.jetbrains.com/help/idea/2020.1/tuning-the-ide.html#system-directory

* IDE logs - `[IDE_SYSTEM_DIR]/log/idea.log*`
* Saros logs - `~[IDE_SYSTEM_DIR]/log/SarosLogs/*.log`

{% endcapture %}
{% include ide-tabs.html eclipse=eclipse intellij=intellij %}

Before attaching any log files, please **make sure to redact any private information** that you do not wish to make publicly available.

{% endcollapsible %}
{% endaccordion %}


## Network Issues

{% accordion network %}
{% collapsible How much bandwidth do you need to run Saros? %}

The real bandwidth problem is usually not during a Saros session, but
rather with the project synchronization needed for starting one, which
may take quite long (many minutes) for larger projects over a
low-bandwidth connection. Make sure all participants have local copies
of the project that are not too different before the session starts.

{% endcollapsible %}
{% collapsible Does Saros work between two home users? Or through two firewalls? %}

Home users (using DSL) and most corporate networks employ
[NAT](http://en.wikipedia.org/wiki/Network_address_translation).
Computers behind a NAT can open TCP connections to other computers, but
other computers cannot open TCP connections to them.

So how can two Saros users that both sit behind a NAT create a joint
Saros session?

-   Saros uses an [XMPP](http://en.wikipedia.org/wiki/XMPP) server to
    get in contact with the other participant(s). Companies can [run their own XMPP server](how-tos/setup-xmpp.md)
    for maximum privacy; home users can use almost any public XMPP server.
-   Throughout the session (if possible) Saros uses
    [Socks5](http://en.wikipedia.org/wiki/SOCKS) connections (direct
    or mediated) between the participants. Saros supports optional [UPnP
    port
    forwarding](http://en.wikipedia.org/wiki/Universal_Plug_and_Play#NAT_traversal)
    to improve chances of direct connections. (see Saros
    network preferences)

{% endcollapsible %}
{% collapsible I keep getting Mediated Socks5 or slower IBB connections. What am I doing wrong? %}

### Troubleshooting Socks5 Bytestream Establishment

If you keep getting Mediated Socks5 Bytestreams or In-Band Bytestreams,
other peers cannot connect to you directly (cannot create a TCP
connection to you). What can you do to improve chances?

*   Your default Socks5 proxy port (7777) might be in conflict. You can
    change the Socks5 proxy port in the Saros network preferences.
*   Your firewall might block connection requests for/from Saros. You
    can configure your firewall to allow Saros (Eclipse, Java)
    communication.
*   If you are connected through a Universal Plug and Play compatible
    gateway device, you can enable Saros to perform a port mapping for
    the computer it is running on and make it reachable from
    the outside. The UPnP option can be enabled in Saros
    network preferences. If your gateway device is not found, it might
    require enabling UPnP support as well.
*   If your gateway is not UPnP compatible, it may be possible to
    manually configure a port mapping for the Socks5 proxy port and your
    private IP to be reachable from the outside. Consult your
    gateway (e.g. Router) manual for port mapping or sometimes labelled
    virtual servers.

### What Might Stop You From Using Socks5

Some factors might prevent you from using S5B. Lets have a quick look at
the S5B protocol when you want connect a buddy.

*   Your Saros requests your connected XMPP server for an
    available proxy.
*   Your Saros detects its local and global network addresses.
*   Your Saros sends the list of your addresses and the proxy address
    (if any) to your buddy's Saros.
*   Your buddy's Saros attempts to create a TCP connection to any of the
    addresses you provided.
    *   If your buddy connected to one of your addresses, you'll get a
        direct S5B connection.
    *   If your buddy couldn't connect to your addresses but to a proxy
        and so does your Saros, you will get a mediated S5B connection.
    *   Otherwise the S5B connection fails.

Saros actually attempts the S5B creation in both directions, so both
sides attempts to connect each other. This improves chances of one peer
connect to the other one. You can check your contact list to see which
bytestream type is established between you and a contact (if any).

For further information check out data connections in Saros on our network layer page.
**Important:** The page may be quite outdated as it is part of our legacy documentation and has not been migrated to our new documentation.
It can be found [here](https://saros-project.github.io/legacy_docs/networklayer.html#Data%20connections%20in%20Saros).
{% endcollapsible %}
{% collapsible Why is updating Saros over the Eclipse Update mechanism so slow? %}

We think it is basically a problem with Eclipse, which keeps checking
for updates to all plug-ins that you have installed. To work around this
problem, uncheck "Contact all update sites during install to find
required software" in Eclipse's installation dialog.
{% endcollapsible %}
{% collapsible I can not connect with my jabber account. %}

Go to Eclipse -&gt; Saros -&gt; Preferences -&gt; General -&gt; Network
Connections and make sure, that there are no proxy settings checked.

{% endcollapsible %}
{% collapsible I cannot access public servers from my location. Can I still use Saros over the local network somehow? %}

Yes. See the [Installation and setup page](how-tos/setup-xmpp.md) for
information on setting up your own local XMPP server.
{% endcollapsible %}
{% collapsible The updates I'm getting from others in the session seem to arrive slowly. %}

All edits go through a server. Because it can be used with a public
server, Saros attempts to be courteous and sends edits out in intervals
(a few hundred milliseconds), so the server is not overloaded. However,
everyone in your session can reduce this interval in order to remove the
perceived latency. Just go the Saros advanced preferences and enter a
lower value in the box labelled "Interval (in milliseconds) between
outgoing edits to peers".

{% endcollapsible %}
{% endaccordion %}

## Known Issues

The following issues are general issues that arise from
the general approach of Saros.

If you are searching for
bugs look into our [issue tracker](https://github.com/saros-project/saros/issues).

{% accordion known-issues %}
{% collapsible About Data Transfer %}

*   Transferring large amounts of data during session initiation
    (project synchronization) can take a lot of time. Do as much of the
    synchronization via your version control repository as you can to
    keep session initiation fast by using "copy of existing project"
    when accepting a session invitation.

*   Refactoring operations can produce a huge number of events to be
    transfered by Saros, which may take very long and can thus be
    confusing for participants.

*   In particular, on-the-fly refactorings such as 'rename' perform one
    such (possibly large) set of operations for each keypress. It is
    wise to avoid these operations.

{% endcollapsible %}
{% collapsible Making Your Session Mates Aware of Your Actions %}

*   Be aware that Saros transfers only the text editor pane. When you
    use other elements of Eclipse, e.g. structure browsers or the HTML
    browser, your session mates cannot automatically see this. Thus,
    talking aloud about the things that you are doing there is probably
    required to make them aware of your actions.

{% endcollapsible %}
{% collapsible Eclipse Editor Technicalities %}

*   You and your session mates should use the same editor settings
    regarding formatting and encoding; in particular regarding TAB
    width, TAB/spaces handling and character encoding.

{% endcollapsible%}
{% collapsible About Eclipse Plugins %}

*   If you are sharing a project which is managed by a source code
    management system such as Subversion, make sure that all
    participants have compatible versions of the SCM plugins installed.
    Otherwise Saros might corrupt the version information or transfer
    revision data unnecessarily.

{% endcollapsible %}
{% endaccordion %}