---
title: Troubleshooting
---

## Technical Questions

### Setting up

#### Why is updating Saros over the Eclipse Update mechanism so slow?

We think it is basically a problem with Eclipse, which keeps checking
for updates to all plug-ins that you have installed. To work around this
problem, uncheck "Contact all update sites during install to find
required software" in Eclipse's installation dialog.

#### I can not connect with my jabber account.

Go to Eclipse -&gt; Saros -&gt; Preferences -&gt; General -&gt; Network
Connections and make sure, that there are no proxy settings checked.

#### I cannot access public servers from my location. Can I still use Saros over the local network somehow?

Yes. See the [Installation and setup page](setup-xmpp.md) for
information on setting up your own local XMPP server.

## Editing

#### The updates I'm getting from others in the session seem to arrive slowly.

All edits go through a server. Because it can be used with a public
server, Saros attempts to be courteous and sends edits out in intervals
(a few hundred milliseconds), so the server is not overloaded. However,
everyone in your session can reduce this interval in order to remove the
perceived latency. Just go the Saros advanced preferences and enter a
lower value in the box labelled "Interval (in milliseconds) between
outgoing edits to peers".

#### I don't want to see the selections and/or contributions of my participants highlighted.

The corresponding setting is not specifically Saros related and is
therefore not located in Saros' own preferences. Saros uses so called
"Annotations" for highlighting your partners' activities and
contributions.

Simply open your Eclipse Preferences ("Window" &gt; "Preferences") and
navigate to "General" &gt; "Editors" &gt; "Text Editors" &gt;
"Annotations" (or enter "Annotations" in the search field in the upper
left corner).

The list of "Annotation types" should contain several entries with the
"DPP" prefix. If you want to disable the highlighting of your partners'
selections, click the corresponding entry (e.g. "DPP Selection of buddy
1") and uncheck the box next to "Text as \[Highlighted\]" — you may want
to repeat this step for all five buddies. The same goes for the
"contributions" (that portion of source code someone authored).

### Network Issues

#### I keep getting Mediated Socks5 or slower IBB connections. What am I doing wrong?

##### Troubleshooting Socks5 Bytestream Establishment

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

##### What Might Stop You From Using Socks5

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

For further information check out data connections in Saros on our
network layer
page [here](../old/networklayer.md#Data%20connections%20in%20Saros).

## Known Issues

### About Data Transfer

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

### Making Your Session Mates Aware of Your Actions

*   Be aware that Saros transfers only the text editor pane. When you
    use other elements of Eclipse, e.g. structure browsers or the HTML
    browser, your session mates cannot automatically see this. Thus,
    talking aloud about the things that you are doing there is probably
    required to make them aware of your actions.

### Eclipse Editor Technicalities

*   You and your session mates should use the same editor settings
    regarding formatting and encoding; in particular regarding TAB
    width, TAB/spaces handling and character encoding.

### About Eclipse Plugins

*   If you are sharing a project which is managed by a source code
    management system such as Subversion, make sure that all
    participants have compatible versions of the SCM plugins installed.
    Otherwise Saros might corrupt the version information or transfer
    revision data unnecessarily.
