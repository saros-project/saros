package de.fu_berlin.inf.dpp.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.easymock.EasyMock;
import org.eclipse.core.runtime.Path;
import org.junit.Test;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.serializable.ChecksumActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.NOPActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.TextSelectionActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.ViewportActivityDataObject;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.ResourceAdapterFactory;
import de.fu_berlin.inf.dpp.net.JID;

public class ActivityUtilsTest {

    private final JID aliceJID = new JID("alice@junit");
    private final JID bobJID = new JID("bob@junit");

    private final IPath fooPath = ResourceAdapterFactory
        .create(new Path("foo"));
    private final IPath barPath = ResourceAdapterFactory
        .create(new Path("bar"));

    private final IProject fooProject = EasyMock.createMock(IProject.class);
    private final IProject barProject = EasyMock.createMock(IProject.class);

    private final NOPActivityDataObject nopADO = new NOPActivityDataObject(
        aliceJID, bobJID, 0);

    private final ChecksumActivityDataObject checksumADO = new ChecksumActivityDataObject(
        aliceJID, new SPath(fooProject, fooPath), 0, 0, null);

    @Test
    public void testContainsChecksumsOnly() {

        List<IActivityDataObject> emtpyADOs = Collections.emptyList();
        List<IActivityDataObject> checksumADOs = new ArrayList<IActivityDataObject>();
        List<IActivityDataObject> randomADOs = new ArrayList<IActivityDataObject>();

        checksumADOs.add(checksumADO);

        randomADOs.add(checksumADO);
        randomADOs.add(nopADO);

        assertFalse(
            "must return false on a collection with at least one non checksum ADO",
            ActivityUtils.containsChecksumsOnly(randomADOs));

        assertFalse("must return false on a collection without a checksum ADO",
            ActivityUtils.containsChecksumsOnly(emtpyADOs));

        assertTrue(
            "must return true on a collection containing only checksum ADOs",
            ActivityUtils.containsChecksumsOnly(checksumADOs));
    }

    @Test
    public void testOptimize() {

        SPath foofooSPath = new SPath(fooProject, fooPath);

        SPath foobarSPath = new SPath(fooProject, barPath);

        SPath barfooSPath = new SPath(barProject, fooPath);

        SPath barbarSPath = new SPath(barProject, barPath);

        TextSelectionActivityDataObject tsChange0ADO = new TextSelectionActivityDataObject(
            aliceJID, 0, 1, foofooSPath);

        TextSelectionActivityDataObject tsChange1ADO = new TextSelectionActivityDataObject(
            aliceJID, 1, 1, foofooSPath);

        TextSelectionActivityDataObject tsChange2ADO = new TextSelectionActivityDataObject(
            aliceJID, 0, 1, foobarSPath);

        TextSelectionActivityDataObject tsChange3ADO = new TextSelectionActivityDataObject(
            aliceJID, 1, 1, foobarSPath);

        TextSelectionActivityDataObject tsChange4ADO = new TextSelectionActivityDataObject(
            aliceJID, 0, 1, barfooSPath);

        TextSelectionActivityDataObject tsChange5ADO = new TextSelectionActivityDataObject(
            aliceJID, 1, 1, barfooSPath);

        TextSelectionActivityDataObject tsChange6ADO = new TextSelectionActivityDataObject(
            aliceJID, 0, 1, barbarSPath);

        TextSelectionActivityDataObject tsChange7ADO = new TextSelectionActivityDataObject(
            aliceJID, 1, 1, barbarSPath);

        // --------------------------------------------------------------------------------

        ViewportActivityDataObject vpChange0ADO = new ViewportActivityDataObject(
            aliceJID, 0, 1, foofooSPath);

        ViewportActivityDataObject vpChange1ADO = new ViewportActivityDataObject(
            aliceJID, 1, 1, foofooSPath);

        ViewportActivityDataObject vpChange2ADO = new ViewportActivityDataObject(
            aliceJID, 0, 1, foobarSPath);

        ViewportActivityDataObject vpChange3ADO = new ViewportActivityDataObject(
            aliceJID, 1, 1, foobarSPath);

        ViewportActivityDataObject vpChange4ADO = new ViewportActivityDataObject(
            aliceJID, 0, 1, barfooSPath);

        ViewportActivityDataObject vpChange5ADO = new ViewportActivityDataObject(
            aliceJID, 1, 1, barfooSPath);

        ViewportActivityDataObject vpChange6ADO = new ViewportActivityDataObject(
            aliceJID, 0, 1, barbarSPath);

        ViewportActivityDataObject vpChange7ADO = new ViewportActivityDataObject(
            aliceJID, 1, 1, barbarSPath);

        List<IActivityDataObject> ados = new ArrayList<IActivityDataObject>();

        ados.add(tsChange0ADO);
        ados.add(nopADO);
        ados.add(tsChange1ADO);
        ados.add(nopADO);
        ados.add(tsChange2ADO);
        ados.add(nopADO);
        ados.add(tsChange3ADO);
        ados.add(nopADO);
        ados.add(tsChange4ADO);
        ados.add(nopADO);
        ados.add(tsChange5ADO);
        ados.add(nopADO);
        ados.add(tsChange6ADO);
        ados.add(nopADO);
        ados.add(tsChange7ADO);
        ados.add(nopADO);
        ados.add(vpChange0ADO);
        ados.add(nopADO);
        ados.add(vpChange1ADO);
        ados.add(nopADO);
        ados.add(vpChange2ADO);
        ados.add(nopADO);
        ados.add(vpChange3ADO);
        ados.add(nopADO);
        ados.add(vpChange4ADO);
        ados.add(nopADO);
        ados.add(vpChange5ADO);
        ados.add(nopADO);
        ados.add(vpChange6ADO);
        ados.add(nopADO);
        ados.add(vpChange7ADO);
        ados.add(nopADO);

        List<IActivityDataObject> optimizedADOs = ActivityUtils.optimize(ados);

        assertEquals("ADOs are not optimized optimal", /* NOP */
            16 + /* TS */4 + /* VP */4, optimizedADOs.size());

        assertRange(0, 0, optimizedADOs, nopADO);
        assertRange(1, 1, optimizedADOs, tsChange1ADO);
        assertRange(2, 3, optimizedADOs, nopADO);
        assertRange(4, 4, optimizedADOs, tsChange3ADO);
        assertRange(5, 6, optimizedADOs, nopADO);
        assertRange(7, 7, optimizedADOs, tsChange5ADO);
        assertRange(8, 9, optimizedADOs, nopADO);
        assertRange(10, 10, optimizedADOs, tsChange7ADO);
        assertRange(11, 12, optimizedADOs, nopADO);
        assertRange(13, 13, optimizedADOs, vpChange1ADO);
        assertRange(14, 15, optimizedADOs, nopADO);
        assertRange(16, 16, optimizedADOs, vpChange3ADO);
        assertRange(17, 18, optimizedADOs, nopADO);
        assertRange(19, 19, optimizedADOs, vpChange5ADO);
        assertRange(20, 21, optimizedADOs, nopADO);
        assertRange(22, 22, optimizedADOs, vpChange7ADO);
        assertRange(23, 23, optimizedADOs, nopADO);
    }

    private void assertRange(int l, int h, List<IActivityDataObject> ados,
        IActivityDataObject ado) {
        for (int i = l; i <= h; i++)
            assertSame("optimization resulted in wrong ADOs order", ado,
                ados.get(i));
    }
}
