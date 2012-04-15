package de.fu_berlin.inf.dpp.net.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.CompactWriter;

import de.fu_berlin.inf.dpp.activities.SPathDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.AbstractActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.ChecksumActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.ChecksumErrorActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.EditorActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.FileActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.FolderActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.JupiterActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.PermissionActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.ProgressActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.TextEditActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.TextSelectionActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.VCSActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.ViewportActivityDataObject;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.JupiterVectorTime;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.DeleteOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.InsertOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.NoOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.SplitOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.TimestampOperation;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.TimedActivityDataObject;
import de.fu_berlin.inf.dpp.net.internal.TimedActivities;
import de.fu_berlin.inf.dpp.util.xstream.IPathConverter;
import de.fu_berlin.inf.dpp.util.xstream.JIDConverter;
import de.fu_berlin.inf.dpp.util.xstream.UrlEncodingStringConverter;

public class TimedActivitiesPacket extends Packet {

    private static final XStream XSTREAM;

    private static final int GZIP_BUFFER_SIZE = 32 * 1024;

    static {
        XStream xstream = new XStream();
        xstream.registerConverter(new IPathConverter());
        xstream.registerConverter(new JIDConverter());
        xstream.registerConverter(new UrlEncodingStringConverter());

        xstream.processAnnotations(new Class[] {

        TimedActivities.class,

        AbstractActivityDataObject.class,

        EditorActivityDataObject.class,

        FileActivityDataObject.class,

        FolderActivityDataObject.class,

        PermissionActivityDataObject.class,

        TextEditActivityDataObject.class,

        TextSelectionActivityDataObject.class,

        ViewportActivityDataObject.class,

        TimedActivityDataObject.class,

        JupiterActivityDataObject.class,

        JupiterVectorTime.class,

        DeleteOperation.class,

        InsertOperation.class,

        NoOperation.class,

        SplitOperation.class,

        TimestampOperation.class,

        JID.class, SPathDataObject.class,

        ChecksumActivityDataObject.class,

        ChecksumErrorActivityDataObject.class,

        ProgressActivityDataObject.class,

        VCSActivityDataObject.class });

        XSTREAM = xstream;
    }

    private TimedActivities timedActivities;

    public TimedActivitiesPacket() {
        super(PacketType.TIMED_ACTIVITIES);
    }

    public TimedActivitiesPacket(TimedActivities timedActivities) {
        super(PacketType.TIMED_ACTIVITIES);
        this.timedActivities = timedActivities;
    }

    public TimedActivities getTimedActivities() {
        return timedActivities;
    }

    @Override
    public void serialize(OutputStream out) throws IOException {
        GZIPOutputStream gzip = new GZIPOutputStream(out, GZIP_BUFFER_SIZE);
        DataOutputStream dos = new DataOutputStream(gzip);

        // some activities are too large ( > 1 MB !)
        // dos.writeUTF(XSTREAM.toXML(timedActivities));

        StringWriter writer = new StringWriter(512);
        XSTREAM.marshal(timedActivities, new CompactWriter(writer));

        byte[] xml = writer.toString().getBytes("UTF-8");
        dos.writeInt(xml.length);
        dos.write(xml);

        dos.flush();
        gzip.finish();
    }

    @Override
    public void deserialize(InputStream in) throws IOException {
        GZIPInputStream gzip = new GZIPInputStream(in, GZIP_BUFFER_SIZE);
        DataInputStream dis = new DataInputStream(gzip);

        int length = dis.readInt();
        byte[] xml = new byte[length];
        dis.readFully(xml);
        timedActivities = (TimedActivities) XSTREAM.fromXML(new String(xml,
            "UTF-8"));
    }
}
