package de.fu_berlin.inf.dpp.util;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.jivesoftware.smack.packet.Message;

import de.fu_berlin.inf.dpp.Saros;

// TODO CJ: review needed
public class PacketProtokollLogger {

    private static Logger logger;

    /* Singleton Pattern. */
    private static PacketProtokollLogger connectionLogger;

    private PacketProtokollLogger() {
        PacketProtokollLogger.logger = Logger.getLogger("Connection Logger");
        PacketProtokollLogger.initLogger();
    }

    public static PacketProtokollLogger getInstance() {
        if (PacketProtokollLogger.connectionLogger == null) {
            PacketProtokollLogger.connectionLogger = new PacketProtokollLogger();
        }
        return PacketProtokollLogger.connectionLogger;
    }

    public static void initLogger() {
        try {
            PatternLayout layout = new PatternLayout("%d{ISO8601} %-5p %m%n");
            // DailyRollingFileAppender fileAppender = new
            // DailyRollingFileAppender( layout,
            // "ConnectionDatei"+Saros.getDefault().getMyJID().getName()+".log",
            // "'.'yyyy-MM-dd_HH-mm" );
            FileAppender fileAppender = new FileAppender(layout,
                "ConnectionDatei" + Saros.getDefault().getMyJID().getName()
                    + ".log", false);
            PacketProtokollLogger.logger.addAppender(fileAppender);
            PacketProtokollLogger.logger.setLevel(Level.ALL);
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    /**
     * log time in millis and message properties of outgoing message.
     * 
     * @param msg
     *            sended message
     */
    public void sendPacket(Message msg) {
        // Date send = Calendar.getInstance().getTime();
        // logger.info(msg.getFrom()+" "+msg.getThread()+" send : "+DateFormat.
        // getDateTimeInstance().format(send));
        // logger.info(Saros.getDefault().getMyJID().getName()+" send : "+msg.
        // getPacketID());
    }

    /**
     * log time in millis and message properties of incomming message.
     * 
     * @param msg
     *            received message.
     */
    public void receivePacket(Message msg) {

        //logger.info(Saros.getDefault().getMyJID().getName()+" receive : "+msg.
        // getPacketID());
    }
}
