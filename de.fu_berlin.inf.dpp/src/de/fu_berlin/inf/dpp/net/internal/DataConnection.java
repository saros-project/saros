package de.fu_berlin.inf.dpp.net.internal;

import de.fu_berlin.inf.dpp.net.JID;

public interface DataConnection {

    public JID getPeer();

    public void sendData();

    public void close();

    public void addListener();

}
