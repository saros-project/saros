package de.fu_berlin.inf.dpp.net;

import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.NetTransferMode;

public interface ITransferModeListener {

    /**
     * The TransferMode for the User with the given JID in either incoming or
     * outgoing direction has changed.
     * 
     * @param jid
     *            The JID of the user for which the TransferMode has changed.
     * @param newMode
     * @param incoming
     */
    public void setTransferMode(JID jid, NetTransferMode newMode,
        boolean incoming);

    /**
     * Is called when the transferMode information is reset (probably because
     * the connection changed)
     */
    public void clear();

}
