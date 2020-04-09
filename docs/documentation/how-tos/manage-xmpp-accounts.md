---
title: Manage XMPP Accounts
---

This page described how to manage your XMPP accounts (**not your contacts**).
Therefore, the information is only relevant if you want to replace an old account with a new one,
update the information for an added account (like the password), or you want to use multiple accounts.

{% capture eclipse %}
## Saros Account Preferences

Most of the following configuration steps are located in the
Saros account preferences. You can open these preferences:
* By opening `Window > Preferences` and selecting the `Saros` entry. **OR**
* By opening the "Saros" drop-down menu in the menu bar and clicking on `Preferences`. **OR**
* By opening the drop-down menu beside the "Connect" button (![connect icon](../images/icons/connect.png)) in the Saros view
  and clicking on `Configure Accounts...`.

## Add an Account

- In the top bar of the Saros view, open the drop-down menu beside the "Connect" button (![connect icon](../images/icons/connect.png)).
- Select option `Add Account...`.
- Follow the steps of the wizard.

*Alternatively* via menu bar:

- Open the "Saros" drop-down menu in the menu bar.
- Select option `Add Account...`.
- Follow the steps of the wizard.

*Alternatively* via preferences:

- Open the [Saros account preferences](#saros-account-preferences)
- Click on `Add Account`.
- Follow the steps of the wizard.

## Remove an Account

- Open the [Saros account preferences](#saros-account-preferences)
- Select the account you want to remove in the `XMPP Accounts` list.
- Click on `Remove Account`.

## Modify an Account

- Open the [Saros account preferences](#saros-account-preferences)
- Select the account you want to modify in the `XMPP Accounts` list.
- Click on `Edit Account`.
- Change the JID or the password. (If you want to change network specific information, open the `Advanced Options`.)
- Click on `Finish`.

## Change the Default Account

- Open the [Saros account preferences](#saros-account-preferences).
- Select the account you want to define as default account.
- Click on `Activate Account`.

{% endcapture eclipse %}
{% capture intellij %}
## Add an Account

- In the top bar of the Saros view, click the "Connect" button (![connect icon](../images/icons/connect.png)).
- Choose `Add account...` from the pop-up menu.

{% alert warning %}
## Note
As mentioned in the section [missing features](/releases/saros-i_0.3.0.html#missing-features), Saros for IntelliJ **does currently not support the creation, management, or deletion of XMPP accounts**.

If you accidentally made a typo while entering your username or password, the created account entry can currently only be changed or deleted through Saros for Eclipse.

Alternatively, you could delete the Saros account store `~/.saros/config.dat`. **Note that this will remove all stored XMPP accounts from the local configuration.** They can be re-added after restarting Saros.

{% endalert %}

{% endcapture %}
{% include ide-tabs.html eclipse=eclipse intellij=intellij %}
