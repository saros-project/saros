package de.fu_berlin.inf.dpp.net;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.runtime.Path;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.activities.SPathDataObject;
import de.fu_berlin.inf.dpp.activities.business.EditorActivity;
import de.fu_berlin.inf.dpp.activities.serializable.EditorActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.net.internal.TimedActivityDataObject;

public class TimedActivityDataObjectTest {
    static TimedActivityDataObject timedADO;

    @BeforeClass
    public static void setUp() {
        SPathDataObject path = new SPathDataObject("pid", new Path("testpath"),
            "xtx");
        JID jid = new JID("userXYZ@jabber.org");
        int seqnr = 1;
        IActivityDataObject activityDataObject = new EditorActivityDataObject(
            jid, EditorActivity.Type.ACTIVATED, path);
        timedADO = new TimedActivityDataObject(activityDataObject, jid, seqnr);

    }

    /**
     * Test method for
     * {@link de.fu_berlin.inf.dpp.net.internal.TimedActivityDataObject#TimedActivityDataObject(de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject, de.fu_berlin.inf.dpp.net.JID, int)}
     * .
     */
    @Test
    public void testTimedActivityDataObject() {
        SPathDataObject path = new SPathDataObject("pid", new Path("testpath"),
            "xtx");
        JID jid = new JID("userXYZ@jabber.org");
        int seqnr = 1;
        IActivityDataObject activityDataObject = new EditorActivityDataObject(
            jid, EditorActivity.Type.ACTIVATED, path);

        new TimedActivityDataObject(activityDataObject, jid, seqnr);

    }

    /**
     * Test method for
     * {@link de.fu_berlin.inf.dpp.net.internal.TimedActivityDataObject#getActivity()}
     * .
     */
    @Test
    public void testGetActivity() {
        SPathDataObject path = new SPathDataObject("pid", new Path("testpath"),
            "xtx");
        JID jid = new JID("userXYZ@jabber.org");
        IActivityDataObject activityDataObject = new EditorActivityDataObject(
            jid, EditorActivity.Type.ACTIVATED, path);
        assertEquals("The activity is not returned correctly.",
            activityDataObject, timedADO.getActivity());
    }

    /**
     * Test method for
     * {@link de.fu_berlin.inf.dpp.net.internal.TimedActivityDataObject#getSender()}
     * .
     */
    @Test
    public void testGetSender() {
        JID jid = new JID("userXYZ@jabber.org");
        assertEquals("The sender is not returned correctly.", jid,
            timedADO.getSender());
    }

    /**
     * Test method for
     * {@link de.fu_berlin.inf.dpp.net.internal.TimedActivityDataObject#getSequenceNumber()}
     * .
     */
    @Test
    public void testGetSequenceNumber() {
        int seqnr = 1;
        assertEquals("The sequenznumber is not returned correctly.", seqnr,
            timedADO.getSequenceNumber());
    }

    /**
     * Test method for
     * {@link de.fu_berlin.inf.dpp.net.internal.TimedActivityDataObject#toString()}
     * .
     */
    @Test
    public void testToString() {
        SPathDataObject path = new SPathDataObject("pid", new Path("testpath"),
            "xtx");
        JID jid = new JID("userXYZ@jabber.org");
        int seqnr = 1;
        IActivityDataObject activityDataObject = new EditorActivityDataObject(
            jid, EditorActivity.Type.ACTIVATED, path);
        String s1 = timedADO.toString();
        String s2 = "[" + seqnr + ":" + activityDataObject.toString() + "]";
        assertEquals("ToString isn't working properly.", s2, s1);
    }

    /**
     * Test method for
     * {@link de.fu_berlin.inf.dpp.net.internal.TimedActivityDataObject#equals(java.lang.Object)}
     * .
     */
    @Test
    public void testEqualsObject() {
        Object o1 = null;
        assertEquals(
            "The method should return false because the object is null", false,
            timedADO.equals(o1));

        assertEquals(
            "The method should return true because the object is this", true,
            timedADO.equals(timedADO));

        Object o2 = 5;
        assertEquals(
            "The method should return false because the object is not an instance of TimedActivityDataObject",
            false, timedADO.equals(o2));
        // sequenceNumber is the same and ActivityObject is different
        SPathDataObject path = new SPathDataObject("pid", new Path("testpath"),
            "xtx");
        JID jid = new JID("userABC@jabber.org");
        int seqnr = 1;
        IActivityDataObject activityDataObject = new EditorActivityDataObject(
            jid, EditorActivity.Type.ACTIVATED, path);
        TimedActivityDataObject t2 = new TimedActivityDataObject(
            activityDataObject, jid, seqnr);

        assertEquals(
            "The method should return false because the TimedActivityDataObjects are not the same",
            false, timedADO.equals(t2));

        // sequenceNumber and ActivityObjects are the different
        SPathDataObject path2 = new SPathDataObject("pid",
            new Path("testpath"), "xtx");
        JID jid2 = new JID("userABC@jabber.org");
        int seqnr2 = 2;
        IActivityDataObject activityDataObject2 = new EditorActivityDataObject(
            jid2, EditorActivity.Type.ACTIVATED, path2);
        TimedActivityDataObject t3 = new TimedActivityDataObject(
            activityDataObject2, jid2, seqnr2);
        assertEquals(
            "The method should return false because the TimedActivityDataObjects are not the same",
            false, timedADO.equals(t3));

        // sequenceNumber is different and ActivityObjects is the same
        SPathDataObject path3 = new SPathDataObject("pid",
            new Path("testpath"), "xtx");
        JID jid3 = new JID("userXYZ@jabber.org");
        int seqnr3 = 2;
        IActivityDataObject activityDataObject3 = new EditorActivityDataObject(
            jid3, EditorActivity.Type.ACTIVATED, path3);

        new TimedActivityDataObject(activityDataObject3, jid3, seqnr3);

        assertEquals(
            "The method should return false because the TimedActivityDataObjects are not the same",
            false, timedADO.equals(t3));

        // sequenceNumber and ActivityObjects are the same
        SPathDataObject path4 = new SPathDataObject("pid",
            new Path("testpath"), "xtx");
        JID jid4 = new JID("userXYZ@jabber.org");
        int seqnr4 = 1;
        IActivityDataObject activityDataObject4 = new EditorActivityDataObject(
            jid4, EditorActivity.Type.ACTIVATED, path4);
        TimedActivityDataObject t5 = new TimedActivityDataObject(
            activityDataObject4, jid4, seqnr4);
        assertEquals(
            "The method should return true because the TimedActivityDataObjects are the same",
            true, timedADO.equals(t5));

    }

    /**
     * Test method for
     * {@link de.fu_berlin.inf.dpp.net.internal.TimedActivityDataObject#compareTo(de.fu_berlin.inf.dpp.net.internal.TimedActivityDataObject)}
     * .
     */
    @Test
    public void testCompareTo() {
        SPathDataObject path2 = new SPathDataObject("pid",
            new Path("testpath"), "xtx");
        JID jid2 = new JID("userABC@jabber.org");
        int seqnr2 = 2;
        IActivityDataObject activityDataObject2 = new EditorActivityDataObject(
            jid2, EditorActivity.Type.ACTIVATED, path2);
        TimedActivityDataObject t3 = new TimedActivityDataObject(
            activityDataObject2, jid2, seqnr2);
        assertEquals("The method should return -1", 1 - 2,
            timedADO.compareTo(t3));
    }
}
