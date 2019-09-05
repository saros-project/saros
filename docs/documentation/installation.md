---
title: Install Saros
---

{% capture eclipse %}

## Prerequisites

*   Saros requires **Java SE 8** or higher
*   Saros is developed and tested using **Eclipse 4.6**, but it
    should work on any newer version without problems.
    *   [Download Eclipse](http://www.eclipse.org/downloads/)
*   Saros ought to run on all platforms that can execute Eclipse
    cleanly. 
*   If you want to try Saros, you may use our public XMPP/Jabber server
    `saros-con.imp.fu-berlin.de`. However, if you want to use Saros for
    commercial purposes, we recommend
    [setting up your own XMPP server](setup-xmpp.md) for maximum
    control over all exchanged data.

## Installation Steps

### Via Eclipse Marketplace

You can install Saros from the [Eclipse Marketplace
website](http://marketplace.eclipse.org/content/saros-distributed-collaborative-editing-and-pair-programming-0)
or from within Eclipse:

1.  Open Eclipse
2.  Open the marketplace: *Menu &gt; Help &gt; Eclipse Marketplace...*
3.  Search for "Saros"
4.  Click on the "Install" button

### Via Update Site

**The Eclipse plugin update site is:
<https://www.saros-project.org/update-site/eclipse>**

1.  Open Eclipse
2.  Open the software updates manager: *Menu &gt; Help &gt; Install
    New Software...*
3.  In the *Available Software* wizard select *Add*
4.  Enter `https://www.saros-project.org/update-site/eclipse` in the Location field;
    press OK

5.  Choose the plugin in the the list of available software and select
    it
6.  In the next window select *Next *to confirm installation
7.  Accept the license agreement and select *Finish*
8.  Restart Eclipse

### As Dropin

1.  Download the [latest *saros-dropin-\*.zip*
    from GitHub](https://github.com/saros-project/saros/releases).
2.  Uncompress the archives into the dropins folder of your
    Eclipse installation.
3.  Restart Eclipse.

## Good to know

*   Please note that Saros requires higher bandwidth and lower latency
    than normal chatting, so public chat servers might be too slow for
    smooth operation of Saros.

{% endcapture %}


{% capture intellij %}

This is the first alpha release of Saros/I, so expect it to be a bit rough around the edges. Before using the plugin for the first time, please have a look at the page [Getting Started with Saros/I](getting-started.html?tab=intellij) and read the [disclaimer](getting-started.html?tab=intellij#disclaimer) and the current [restrictions of the plugin](getting-started.html?tab=intellij#restrictions).

## Prerequisites

Saros/I requires
 - `JDK 8` or newer
 - `IntelliJ 2018.2.7` or newer

Saros/I can be installed from the JetBrains plugin repository or from disk.

## Installation Steps

### From the plugin repository

Saros/I is currently released through the `alpha` release channel. To be able find the plugin on the market place, you will first have to add the `alpha` release channel to your list of plugin repositories. A guide on how to do this is given [here](https://www.jetbrains.org/intellij/sdk/docs/plugin_repository/custom_channels.html#configuring-a-custom-channel-in-intellij-platform-based-ides).

- Choose "Settings..." > "Plugins".
- Select the tab "Marketplace"
- Search for "Saros" in the search bar and select the entry from the list.
- Click the "Install" button.
- Close the settings menu.
- Restart IntelliJ.


### From Disk
The zip file containing the plugin can be downloaded from our [release page](https://github.com/saros-project/saros/releases).

- Choose "Settings..." > "Plugins".
- Click the settings icon (gear/cog) and choose "Install plugin from disk...".
- Navigate to the directory containing the plugin zip.
- Select the zip file.
- Click OK.
- Restart IntelliJ.

{% endcapture %}

{% include ide-tabs.html eclipse=eclipse intellij=intellij %}
