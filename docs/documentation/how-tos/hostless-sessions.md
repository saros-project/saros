---
title: Hostless Sessions
---

{% alert warning %}
## Note

This is an *alpha* feature and not intended for general use yet. However please feel free to play around and give some initial feedback. Contributors to the Saros-Server project are very welcome and might want to take a look at [contribution wiki](/contribute/saros-server.html) for further information.
{% endalert %}

## Use-case

The use-case of the server is to host session independently of any participant. As it takes the role of the session-host any participant may leave or join the session at any time.

## Usage

The Saros-Server is currently not available for download, but is scheduled to be on the next Saros/E Release (no ETA).

If you would like to use the server in the meantime, you will have to build it yourself.
Furthermore, you will also have to build the Saros/E versions you want to use with the server yourself to avoid incompatibilities.
A guide on how to build Saros (and its different components) is given [here](../contribute/development-environment.md).

The server needs it's own XMPP account and can then be started via:
`java -Dsaros.server.jid=max@mustermann.de -Dsaros.server.password=1234 -jar saros.server.jar`

It will host one Saros-Session, that can be joined via Saros/E:
- add the server JID to your contact list
- right-click your newly added contact
- click "*Request Session Invitation*"

You may share projects with the server as usual.

### Workspace

By default Saros-Server will create a workspace folder in a temporary folder to store projects.
You may specify your own folder via `saros.server.workspace`.

E.g.: `java -Dsaros.server.jid=max@mustermann.de -Dsaros.server.password=1234 -Dsaros.server.workspace=/home/user/workspace -jar saros.server.jar`

*Warning*: Be aware that you should not trust the server with existing projects, as this is still an alpha version. For example there is a known issue with multiple projects having the same name, as the server will happily overwrite the existing project as it is assuming the name is unique.

### Interactive Mode

The server can optionally provide an interactive console via setting `saros.server.interactive` to *true*/*yes*/*y*.

E.g.:
```
$ java -Dsaros.server.interactive=yes -Dsaros.server.jid=max@mustermann.de -Dsaros.server.password=1234 -jar saros.server.jar

# ... omitted log messages

# Welcome to Saros Server (type 'help' for available commands)
> help
invite <JID>... - Invite users to session
share <PATH>... - Share projects relative to the workspace with session participants
help - Print this help
quit - Quit Saros Server
> 
```

This is mostly intended for debugging/developing.
