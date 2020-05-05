---
title: Saros Server Development
---

## What is the Saros Server?

Please refer to the [User Documentation](../documentation/how-tos/hostless-sessions.md) for an explanation and the use-case of the Saros server.

Technically it is a headless session-host.

## Code Structure

The Saros Server codebase is comparably slim, as it only requires a [small subset of components](https://github.com/saros-project/saros/blob/master/server/src/saros/server/ServerContextFactory.java) compared to IDE-Plugins. Therefor it may also help to understand Saros overall architecture and as a reference for starting new Saros-Implementations.

Notable exceptions are the [filesystem](https://github.com/saros-project/saros/tree/master/server/src/saros/server/filesystem), [editor](https://github.com/saros-project/saros/tree/master/server/src/saros/server/editor) and [preferences](https://github.com/saros-project/saros/tree/master/server/src/saros/server/preferences) implementations, which are usually adapted from IDE-specific APIs.

## Open Topics

### Permission System

Traditionally the Session-Host is responsible for moderating an active Saros-Session. The Host-Role allows adding new users and accepting new projects into the session. A headless implementation like the server cannot do this, which is why the server currently accepts every request for invitations and new projects. Therefor it can only be executed in a trusted environment.

The server will need some kind of permission system to allow a remote user to manage these tasks. The simplest (and likely sufficient) variant would move this role to some kind of "Admin"-User, that may be configured for a given server.

#### Challenges

- New Activities will likely need to be introduced, that need to be understood by all existing IDE-Clients and show the appropriate dialogs.
- There needs to be a system to transfer the admin rights, otherwise the admin-user becomes the new session-host, that will leave behind a broken (rather then disconnected) session, once he leaves.

### Super-Server

One server currently represents only one session. To lift this short-coming a Super-Server could be developed, that starts a new session-server on demand. This would also play nicely with the idea of an admin-user, as the user requesting the session could automatically be configured to be the first admin.

This concept could prove to be very memory inefficient, as a new process and JVM would be started for each session. The overhead this brings needs to be evaluated to decide, how this should exactly be implemented. If the additional memory requirements are not overshadowed by Saros overall memory usage, an in-process solution could prove to be more reasonable.

#### Challenges

- It is not clear, how this can be represented in the XMPP-Protocol. The Super-Server would be logged with an XMPP-ID to setup the initial communication, but each session-server would need to be dynamically assigned a new JID or the same JID. New JIDs would need some kind of configuration and cooperation with the XMPP-Server. Re-using the same JID is currently not tested with Saros and might cause other unpredictable issues.

### Cleanup of Old Files

Currently the Server creates it's workspace as a temporary folder, it would be better, if the folder could be cleaned up on shutdown, although caching might be appropriate for larger frequently shared projects.
