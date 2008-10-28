package de.fu_berlin.inf.dpp.concurrent;

import java.util.List;

import de.fu_berlin.inf.dpp.net.JID;

/**
 * This interface handles the driver event and the appropriate documents.
 * 
 * @author orieger
 * 
 */
public interface IDriverManager {

    public void addDriver(JID jid);

    public void removeDriver(JID jid);

    public boolean isDriver(JID jid);

    /**
     * only one active driver exists in the system.
     * 
     * @return
     */
    public boolean exclusiveDriver();

    /**
     * get all active driver
     * 
     * @return list of active driver.
     */
    public List<JID> getActiveDriver();

}
