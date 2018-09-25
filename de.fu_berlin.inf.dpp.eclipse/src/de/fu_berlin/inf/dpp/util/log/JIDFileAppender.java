package de.fu_berlin.inf.dpp.util.log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.FileAppender;
import org.apache.log4j.spi.LoggingEvent;
import org.jivesoftware.smack.Connection;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.xmpp.IConnectionListener;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;

/**
 * FileAppender which can replace date patterns in the file name but also can
 * replace a %s with the JID of the currently connected users.
 * 
 * Will cache events until the user logs in for the first time and use then this
 * name (and time). The user name is used until a new connection has been
 * established with a different name.
 * 
 * @author s-ziller
 */
@Component(module = "logging")
public class JIDFileAppender extends FileAppender {

    /**
     * While there is no localJID known the LoggingEvents are cached in this
     * list
     */
    protected List<LoggingEvent> cache = new LinkedList<LoggingEvent>();

    /**
     * The JID of the local user, it is set when the user successfully
     * establishes a connection
     */
    protected JID localJID = null;

    /**
     * The original fileName passed to us from the configuration. The
     * {@link #fileName} we inherited will be expanded with the current date and
     * the {@link #localJID}
     */
    protected String fileNameBackup;

    /*
     * Dependencies
     */
    @Inject
    protected XMPPConnectionService connectionService;

    @Inject
    protected Saros saros;

    @Override
    public synchronized void append(LoggingEvent event) {

        if (connectionService == null && Saros.isInitialized()) {
            initialize();
        }

        // Cache events until we have a localJID
        if (localJID != null) {
            super.append(event);
        } else {
            cache.add(event);
        }
    }

    @Override
    public void activateOptions() {
        // Drop activateOptions silently if we do not have a JID yet
        if (localJID != null)
            super.activateOptions();
    }

    private IConnectionListener listener = new IConnectionListener() {

        @Override
        public void connectionStateChanged(Connection connection,
            ConnectionState newState) {

            if (newState == ConnectionState.CONNECTED) {
                setJID(new JID(connection.getUser()));
            }
        }
    };

    protected void initialize() {
        SarosPluginContext.initComponent(this);

        connectionService.addListener(listener);

        // If already connected use the current JID.
        setJID(connectionService.getJID());
    }

    protected synchronized void setJID(JID newJID) {

        if (newJID == null || newJID.equals(localJID))
            return;

        if (localJID != null)
            closeFile();

        localJID = newJID;

        if (fileNameBackup == null) {
            /*
             * Make a back-up of the filename, because we are going to overwrite
             * it
             */
            fileNameBackup = getFile();
        }

        // log4j config changed, we are out
        if (fileNameBackup == null) {
            connectionService.removeListener(listener);
            return;
        }

        // directory of the Eclipse log
        File directory = saros.getStateLocation().toFile();

        String jidString = localJID.getBase();

        String format = String.format(fileNameBackup, jidString);

        String actualFileName = directory + File.separator
            + new SimpleDateFormat(format).format(new Date());

        setFile(actualFileName);

        // Open new file
        activateOptions();

        // Flush Cache
        for (LoggingEvent log : cache)
            append(log);

        cache.clear();
    }
}
