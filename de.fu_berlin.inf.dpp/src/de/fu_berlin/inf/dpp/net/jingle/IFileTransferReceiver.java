package de.fu_berlin.inf.dpp.net.jingle;

import java.net.InetAddress;

public interface IFileTransferReceiver extends IJingleFileTransferConnection {

    public InetAddress getLocalHost();

    public int getLocalPort();

    public InetAddress getRemoteHost();

    public int getRemotePort();

    public void stop();
}
