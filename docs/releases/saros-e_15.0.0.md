---
title: Saros Release Notes
---

The Saros Team is happy to announce Saros for Eclipse 15.0.0

Since the last release of Saros for Eclipse we mostly restructured, refactored and reworked the project in more than 1000 commits. From now on we want to use a lot shorter release cycles in order to
provide fixes and features faster.

## Upgrade Instructions
**Attention:** If you install the new Saros version, your saved XMPP Account data will get lost.

We changed the structure of our configuration files and the location of our update-site.
Therefore, if you used the previous Saros version, please:
* [Uninstall the old saros plugin](https://help.eclipse.org/2018-09/index.jsp?topic=%2Forg.eclipse.platform.doc.user%2Ftasks%2Ftasks-126.htm)
* Change the update site to: <https://www.saros-project.org/update-site/eclipse>

## Where is the Whiteboard?
You may know the Saros whiteboard from the previous release.
Due to stability issues we did not release the Whiteboard feature of Saros, which allowed to share drawings between Saros users.
We plan to release a new whiteboard as soon as it works reliable.

## JDK requirements
Saros 15.0.0 requires JDK8+, since we switched to JDK8 internally.

## Feedback
If you find any issue with Saros, please report it in our [issue tracker](https://github.com/saros-project/saros/issues).
In case you are not sure whether you found a bug or simply want to talk about Saros, ask us at [Gitter](https://gitter.im/saros-project/saros/user).

