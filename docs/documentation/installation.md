---
title: Install Saros
---

Here you will find a guide **on how to install Saros**
see the [getting started](getting-started.md) guide if
you want to know how to set up Saros.

## Prerequisites

{% capture eclipse %}

Saros/E requires
*  `JDK 8` or newer
*  `Eclipse 4.8` or newer (download [here](http://www.eclipse.org/downloads/))

{% endcapture %}
{% capture intellij %}

Saros/I requires
 - `JDK 8` or newer
 - `IntelliJ 2018.2.7` or newer (download [here](https://www.jetbrains.com/idea/download/))

{% endcapture %}
{% include ide-tabs.html eclipse=eclipse intellij=intellij %}

## Installation Steps

{% capture eclipse %}

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
2.  Open the software updates manager: *Menu &gt; Help &gt; Install New Software...*
3.  In the *Available Software* wizard select *Add*
4.  Enter `https://www.saros-project.org/update-site/eclipse` in the Location field; press OK
5.  Choose the plugin in the list of available software and select it
6.  In the next window select *Next *to confirm installation
7.  Accept the license agreement and select *Finish*
8.  Restart Eclipse

### As Dropin

1.  Download the [latest *saros-dropin-\*.zip*
    from GitHub](https://github.com/saros-project/saros/releases).
2.  Uncompress the archives into the dropins folder of your
    Eclipse installation.
3.  Restart Eclipse.

{% endcapture %}
{% capture intellij %}
This is still an alpha release of Saros/I, so expect it to be a bit rough around the edges. Before using the plugin for the first time, please have a look at the release
notes, especially the [disclaimer](/releases/saros-i_0.2.2.html#disclaimer) and the current [restrictions of the plugin](/releases/saros-i_0.2.2.html#restrictions).

Saros/I can be installed from the JetBrains plugin repository or from disk.

### From the Plugin Repository

Saros/I is currently released through the `alpha` release channel. To be able find the plugin on the market place, you will first have to add the `alpha` release channel to your list of plugin repositories. A guide on how to do this is given [here](https://plugins.jetbrains.com/docs/marketplace/custom-release-channels.html#CustomReleaseChannels-ConfiguringaCustomChannelinIntelliJPlatformBasedIDEs).

- Open the [IntelliJ settings/preferences menu](https://www.jetbrains.com/help/idea/settings-preferences-dialog.html).
- Select the "Plugins" section.
- Select the tab "Marketplace".
- Search for "Saros" in the search bar and select the entry from the list.
- Click the "Install" button.
- Close the settings menu.
- Restart IntelliJ.


### From Disk
The zip file containing the plugin can be downloaded from our [release page](https://github.com/saros-project/saros/releases).


- Open the [IntelliJ settings/preferences menu](https://www.jetbrains.com/help/idea/settings-preferences-dialog.html).
- Select the "Plugins" section.
- Click the settings icon (gear/cog) and choose "Install plugin from disk...".
- Navigate to the directory containing the plugin zip.
- Select the zip file.
- Click OK.
- Restart IntelliJ.

{% endcapture %}

{% include ide-tabs.html eclipse=eclipse intellij=intellij %}

## Good to Know

*   Please note that Saros requires higher bandwidth and lower latency
    than normal chatting, so public chat servers might be too slow for
    smooth operation of Saros.