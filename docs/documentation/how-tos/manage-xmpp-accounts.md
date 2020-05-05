---
title: Manage XMPP Accounts
---

This page described how to manage your XMPP accounts (**not the accounts of your contacts**).
Therefore, the information are only relevant if you want to replace an old account with a new one
you want to use multiple accounts.

{% capture eclipse %}
## Saros Account Preferences

Most of the following configuration steps are located in the
Saros account preferences. You can open these preferences:
* With opening `Window > Preferences` and selecting the `Saros` entry. **OR**
* With opening the "Saros" drop-down menu in the menu bar and clicking on option `Preferences`. **OR**
* With opening the drop-down menu beside the "Connect" button (![connect icon](../images/icons/connect.png)) in the Saros view
  and clicking on option `Configure Accounts...`.

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
- Change the JID or the password (if you want to change network specific information open the `Advanced Options`)
- Click on `Finish`

## Change the Default Account

- Open the [Saros account preferences](#saros-account-preferences)
- Select the account you want to define as default account.
- Click on `Activate Account`

{% endcapture eclipse %}
{% capture intellij %}
## Add an Account

- In the top bar of the Saros view, click the "Connect" button (![connect icon](images/icons/connect.png)).
- Choose `Add account...` from the pop-up menu.

{% alert warning %}
## Note
As mentioned in the section [missing features](/releases/saros-i_0.2.2.html#missing-features), Saros for IntelliJ **does currently not support the creation, management or deletion of XMPP accounts**. As only people on your friends list can be invited to join your Saros session, you will have to create an account and add friends to your friends list through a different client. Any XMPP client (including Saros for Eclipse) can be used for this purpose.

If you accidentally made a typo while entering your username or password, the created account entry can also only be changed or deleted through Saros for Eclipse (or by deleting the account store `~/.saros/config.dat`). Saros for Eclipse does not permit the deletion of the currently chosen account. If you only added one account and would like to remove it, you will have to add a second account (for example with random values) and choose this new account as the default. You can then delete the first account, add a new account with the right values, choose it as the correct default and delete the temporary account entry.

{% endalert %}

{% endcapture %}
{% include ide-tabs.html eclipse=eclipse intellij=intellij %}