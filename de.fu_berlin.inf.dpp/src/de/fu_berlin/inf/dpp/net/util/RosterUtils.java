package de.fu_berlin.inf.dpp.net.util;

import java.util.concurrent.CancellationException;

import org.eclipse.core.runtime.SubMonitor;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.ServiceDiscoveryManager;

import de.fu_berlin.inf.dpp.net.JID;

/**
 * Utility class for classic {@link Roster} operations
 * 
 * @author bkahlert
 */
public class RosterUtils {

    private RosterUtils() {
        // no instantiation allowed
    }

    /**
     * Adds given buddy to the {@link Roster}.
     * 
     * @blocking
     * 
     * @param connection
     * @param jid
     *            the {@link JID} of the contact
     * @param name
     *            the nickname under which the new contact should appear in the
     *            {@link Roster}
     * @param groups
     *            the groups to which the new contact should belong to. This
     *            information will be saved on the server
     * @param monitor
     *            a {@link SubMonitor} to report progress to; may be null
     * @throws XMPPException
     *             is thrown if no connection is established or an error
     *             occurred when adding the user to the {@link Roster} (which
     *             does not mean that the user really exists on the server)
     */
    public static void addToRoster(Connection connection, JID jid, String name,
        String[] groups, SubMonitor monitor) throws XMPPException {

        if (monitor == null)
            monitor = SubMonitor.convert(monitor);

        monitor.beginTask("Adding buddy " + jid + "...", 2);

        try {
            if (!connection.isConnected()) {
                throw new XMPPException("Not connected");
            }

            monitor.worked(1);

            if (connection.getRoster().contains(jid.toString())) {
                monitor.worked(1);
                throw new XMPPException("Buddy already exists.");
            }

            monitor.worked(1);

            connection.getRoster().createEntry(jid.toString(), name, groups);
        } finally {
            monitor.done();
        }
    }

    /**
     * Removes given buddy from the {@link Roster}.
     * 
     * @blocking
     * 
     * @param rosterEntry
     *            the buddy that is to be removed
     * @throws XMPPException
     *             is thrown if no connection is established.
     */
    public static void removeFromRoster(Connection connection,
        RosterEntry rosterEntry) throws XMPPException {
        if (!connection.isConnected()) {
            throw new XMPPException("Not connected");
        }
        connection.getRoster().removeEntry(rosterEntry);
    }

    /**
     * Returns whether the given JID can be found on the server.
     * 
     * @blocking
     * @cancelable
     * 
     * @param connection
     * @param monitor
     *            a {@link SubMonitor} to report progress to; may be null
     * @throws XMPPException
     *             if the service discovery failed. Use
     *             {@link RosterUtils#isDiscoFailedException(XMPPException)} to
     *             figure out, whether this might mean that the server does not
     *             support discovery at all.
     */
    public static boolean isJIDonServer(Connection connection, JID jid,
        SubMonitor monitor) throws XMPPException {
        if (monitor != null)
            monitor.beginTask("Performing Service Discovery on JID " + jid, 2);

        ServiceDiscoveryManager sdm = ServiceDiscoveryManager
            .getInstanceFor(connection);

        if (monitor != null) {
            monitor.worked(1);
            if (monitor.isCanceled())
                throw new CancellationException();
        }

        try {
            boolean discovered = sdm.discoverInfo(jid.toString())
                .getIdentities().hasNext();
            /*
             * discovery does not change any state, if the user wanted to cancel
             * it, we can do that even after the execution finished
             */
            if (monitor != null && monitor.isCanceled())
                throw new CancellationException();
            return discovered;
        } finally {
            if (monitor != null)
                monitor.done();
        }
    }

    /**
     * Given an XMPP Exception this method will return whether the exception
     * thrown by isJIDonServer indicates that the server does not support
     * ServiceDisco.<br>
     * <br>
     * In other words: If isJIDonServer throws an Exception and this method
     * returns true on the exception, then we should call addContact anyway.
     * 
     * @return true, if the exception occurred because the server does not
     *         support ServiceDiscovery
     */
    public static boolean isDiscoFailedException(XMPPException e) {

        /* feature-not-implemented */
        if (e.getMessage().contains("501"))
            return true;

        /* service-unavailable */
        if (e.getMessage().contains("503"))
            return true;

        return false;
    }

}
