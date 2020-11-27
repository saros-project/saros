---
title: Saros Version Compatibility
---

Before inviting another user to join the session, Saros checks whether the plugin version of the invited user is compatible with the local version (i.e. the version of the session host).
If the remote version is detected as incompatible with the local version, the invitation process is aborted.

The Saros plugin version is specified using the format `MAJOR.MINOR.MICRO`.
**For two Saros versions to be compatible, they have to have the same `MAJOR` and `MINOR` version number.**
The `MICRO` version number is ignored for compatibility checks.


## Development Builds

For [development builds](../releases/#development-builds) of Saros, the version string is extended to include a qualifier: `MAJOR.MINOR.MICRO.QUALIFIER`.
This qualifier contains a shortened version of the hash for the commit the build was created on.

**When using such development builds (or any Saros version containing a `QUALIFIER` element), the complete version strings have to match (including `MICRO` and `QUALIFIER`) to be seen as compatible.**


## IDE Cross-Compatibility

Currently, the Saros versions for Eclipse (Saros/E) and IntelliJ IDEA (Saros/I) are not compatible with each other.
