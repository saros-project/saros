---
title: Setup XMPP Server
---

If you are an experienced user or if you want to use Saros in your
company and you want to use another XMPP Server or set up your own with
Openfire, you will find information below.


## XMPP Servers

In order to use Saros you need to configure an XMPP/Jabber account.

-   It is recommended to **use the same XMPP server** for all users
    which participate in a Saros session, because our tests have shown
    that many public XMPP servers use outdated software
    and certificates. Thus in many cases communication between federated
    servers will fail.
-   If you want to **use Saros in a company** or with more than one
    peer, we recommend using your own XMPP server. From our own testing
    we can
    recommend [OpenFire](https://www.igniterealtime.org/projects/openfire/index.jsp) (others
    such as [ejabberd](https://www.process-one.net/en/ejabberd/) should
    be suitable as well)

## eJabberD (Linux)

See [https://www.ejabberd.im](https://www.ejabberd.im/).


## Openfire Installation (Windows)

1.  Download [Openfire for
    Windows](http://www.igniterealtime.org/downloads/index.jsp)
2.  The installation requires administrator privileges.
3.  When the installation wizard asks about database settings choose the
    embedded database.
4.  Enable the proxy service under Server &gt; Server Settings &gt; File
    Transfer Settings. (Should be the default value but it had to be
    disabled for Saros in the past.)

![](images/openfire_settings_02.png){:class="img-fluid"}

5. Create user for example: username: low, password: low.
   ![](images/openfire_create_user.png){:class="img-fluid"}
6. In saros plugin fill: 
- username: low
- password: low
- server: (check picture)
- port: (check picture)
![](images/openfire_server_info.png){:class="img-fluid"}

