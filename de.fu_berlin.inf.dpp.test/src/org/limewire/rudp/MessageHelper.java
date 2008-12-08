package org.limewire.rudp;

import org.limewire.rudp.messages.DataMessage;
import org.limewire.util.PrivilegedAccessor;

/** Helps creates simulated messages. */
public class MessageHelper {

    public static DataMessage createDataMessage(byte connectionID,
                                                long sequenceNumber,
                                                byte[] data,
                                                int len) throws Exception {
        Class clazz = Class.forName("org.limewire.rudp.messages.impl.DataMessageImpl");
        Object obj = PrivilegedAccessor.invokeConstructor(clazz,
                new Object[] { connectionID, sequenceNumber, data, len },
                new Class[] { byte.class, long.class, byte[].class, int.class });
        return (DataMessage)obj;
    }
    
}
