---
title: Saros for Eclipse 16.0.0 Release Notes
---

:tada: We are happy to announce **Saros for Eclipse 16.0.0** :tada:

The new Saros for Eclipse 16.0.0 contains multiple [bug fixes](#bug-fixes)
and [improvements](#improvements).

## Installation
Saros for Eclipse 16.0.0 requires
 - `JDK 8` or newer
 - Eclipse 4.6 (Neon) or newer

## Compatibility
Saros for Eclipse 16.0.0 is not compatible with other Saros versions.

## Feedback
If you find any issue with Saros, please report it in our [issue tracker](https://github.com/saros-project/saros/issues).
In case you are not sure whether you found a bug or simply want to talk about Saros, ask us at [Gitter](https://gitter.im/saros-project/saros/user).

## Changes

Beside multiple internal reworks, [bug fixes](#bug-fixes) and [improvements](#improvements) we also
[removed our Whiteboard](https://www.saros-project.org/contribute/deprecated/whiteboard.html).
It was to brittle and does not fit into our plan to support further Editors/IDEs in the future.

### Improvements
* The `.git` directory is now ignored by default during a session ([#922](https://github.com/saros-project/saros/issues/922)).
* Add Contacts Context Menu Item is enabled for Offline Contacts ([#19](https://github.com/saros-project/saros/issues/19))
* Choose only one "XMPP ID" vs "Jabber ID" ([#22](https://github.com/saros-project/saros/issues/22))
* Simplify reuse of existing projects ([#66](https://github.com/saros-project/saros/issues/66))
* Add XStream security framework setup ([#209](https://github.com/saros-project/saros/issues/209))

### Fixed Bugs
* Removing a file and re-creating it leads to an error ([#223](https://github.com/saros-project/saros/issues/922))
* Dirty editor is not always closed when file is deleted ([#758](https://github.com/saros-project/saros/issues/758))
* "Finish" button of "Start Saros Configuration" is unresponsive ([#676](https://github.com/saros-project/saros/issues/676))
* Project names with trailing numbers are truncated in Wizard ([#61](https://github.com/saros-project/saros/issues/61))
* EditorManager may fire duplicate partActivated events ([#392](https://github.com/saros-project/saros/issues/392))
* Make Balloons not overlap editorparts ([#28](https://github.com/saros-project/saros/issues/28))
* Contribution annotation does not disappear ([#32](https://github.com/saros-project/saros/issues/32))
* Strange behaviour of Connection configurations in ConfigurationSettingsWizardPage ([#456](https://github.com/saros-project/saros/issues/456))
* Warning dialog upon reusing an existing project is misleading ([#65](https://github.com/saros-project/saros/issues/65))
* The "Work Together On.." dropdown list contains only the first project ([#469](https://github.com/saros-project/saros/issues/469))
* Add Jupiter-Heartbeats via TimestampOperations ([#397](https://github.com/saros-project/saros/issues/397))
