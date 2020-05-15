---
title: Resolve Desynchronization
toc: false
---

### Resolving a Desynchronization

By default, the synchronization button (![synchronization button off](images/icons/in_sync.png)) is disabled.
If Saros detects that the local content has become out of sync with the host (i.e. differs in any way), it will notify the user and enable the synchronization button (![synchronization button on](images/icons/out_sync.png)).
To resolve the desynchronization:

- Click the "Synchronization" button (![synchronization button on](images/icons/out_sync.png)). This will open the recovery dialog.
- Click "Yes".

{% alert warning %}
### Warning
As stated in the dialog, **this will replace the content of the affected file(s)** with the content of the corresponding file(s) on the host's side.
This **might override recent changes** to the local files. To avoid data-loss, consider making a backup of the affected files/changes before executing the synchronization recovery.
{% endalert %}