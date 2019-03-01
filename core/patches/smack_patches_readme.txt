Currently needed Smack Patches for Saros with SMACK Release 3.3.0

http://issues.igniterealtime.org/browse/SMACK-312
This is not a full patch for 312 - see http://community.igniterealtime.org/thread/44102
Addresses contact renaming in the roster (listener was not notified)


http://issues.igniterealtime.org/browse/SMACK-357
Addresses SASL issues with GTALK and other XMPP server

IMPORTANT:

When you update the Smack Library.

Either DELETE those patches or modify them BEFORE you commit the changed libraries.

If you delete the patches then you have to reapply them if they are not already fixed
in the updated SMACK version.

As this mostly not work automatically because the file will get auto formated if you doing
changes the following steps are recommended.

Look through the patches which files are affected.

Copy them from the source to the patches source folder.

Format them to apply our format rules.

Apply the patches (mostly manually, so be careful).

Create a patch from the patches files and override the old patch diff with the new generated patch.

Commit your changes.