/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universit�t Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package de.fu_berlin.inf.dpp.test;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;

import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.EditorActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.TextEditActivity;
import de.fu_berlin.inf.dpp.activities.TextSelectionActivity;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentManager;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.TimedActivity;
import de.fu_berlin.inf.dpp.net.internal.ActivitySequencer;
import de.fu_berlin.inf.dpp.project.IActivityProvider;
import de.fu_berlin.inf.dpp.project.internal.SharedProject;

public class ActivitySequencerTest extends TestCase {
    private ActivitySequencer sequencer;
    private IActivityProvider providerMock;

    private TextEditActivity textEditActivity1;
    private TextEditActivity textEditActivity2;
    private TextEditActivity textEditActivity3;

    private static final IPath p1 = new Path("p1.txt");
    private static final IPath p2 = new Path("p1.txt");
    
    private static final String s1 = "user1";
    
    @Override
    protected void setUp() throws Exception {
	sequencer = new ActivitySequencer();
	sequencer
		.initConcurrentManager(ConcurrentDocumentManager.Side.CLIENT_SIDE,
			new User(new JID("host@jabber.com"), 0), new JID(
				"user@jabber.com"), new SharedProject(null,
				null, new JID("user@jabber.com")));

	providerMock = createMock(IActivityProvider.class);
	sequencer.addProvider(providerMock);
	reset(providerMock);

	textEditActivity1 = new TextEditActivity(0, "a", "", p1, s1);
	textEditActivity2 = new TextEditActivity(5, "b", "", p1, s1);
	textEditActivity3 = new TextEditActivity(8, "c", "", p1, s1);
    }

    public void testTwoConsecutive() {
	providerMock.exec(textEditActivity1);
	providerMock.exec(textEditActivity2);
	replay(providerMock);

	sequencer.exec(new TimedActivity(textEditActivity1, 0));
	sequencer.exec(new TimedActivity(textEditActivity2, 1));
	verify(providerMock);
    }

    public void testTwoReversedConsecutive() {
	providerMock.exec(textEditActivity1);
	providerMock.exec(textEditActivity2);
	replay(providerMock);

	sequencer.exec(new TimedActivity(textEditActivity2, 1));
	sequencer.exec(new TimedActivity(textEditActivity1, 0));
	verify(providerMock);
    }

    public void testThreeMixedConsecutive() {
	providerMock.exec(textEditActivity1);
	providerMock.exec(textEditActivity2);
	providerMock.exec(textEditActivity3);
	replay(providerMock);

	sequencer.exec(new TimedActivity(textEditActivity2, 1));
	sequencer.exec(new TimedActivity(textEditActivity1, 0));
	sequencer.exec(new TimedActivity(textEditActivity3, 2));
	verify(providerMock);
    }

    public void testLogFlushedActivities() {
	sequencer.activityCreated(textEditActivity1);
	sequencer.activityCreated(textEditActivity2);
	sequencer.flush();

	assertActivities(
		new IActivity[] { textEditActivity1, textEditActivity2 },
		sequencer.getLog());
    }

    public void testIncomingActivitiesIncTime() {
	sequencer.exec(new TimedActivity(textEditActivity1, 0));
	sequencer.exec(new TimedActivity(textEditActivity2, 1));
	assertEquals(2, sequencer.getTimestamp());
    }

    public void testHasSimpleTextChange() {
	sequencer.activityCreated(new TextEditActivity(5, "a", "", p1, s1));

	assertFlush(new IActivity[] { new TextEditActivity(5, "a", "", p1, s1) });
    }

    public void testJoinConsecutiveTextChanges() {
	sequencer.activityCreated(new TextEditActivity(5, "a", "", p1, s1));
	sequencer.activityCreated(new TextEditActivity(6, "bc", "", p1, s1));
	sequencer.activityCreated(new TextEditActivity(8, "de", "", p1, s1));

	assertFlush(new IActivity[] { new TextEditActivity(5, "abcde", "", p1, s1) });
    }

    public void testDontJoinConsecutiveTextChangesInDifferentFiles() {
	sequencer.activityCreated(new TextEditActivity(5, "a", "", p1, s1));
	sequencer.activityCreated(new EditorActivity(
		EditorActivity.Type.Activated, p2));
	sequencer.activityCreated(new TextEditActivity(8, "de", "", p2, s1));

	assertFlush(new IActivity[] {
		new TextEditActivity(5, "a", "", p1, s1),
		new EditorActivity(EditorActivity.Type.Activated, new Path(
			"/bla")), new TextEditActivity(8, "de", "", p2, s1) });
    }

    public void testSimpleStripRedundantTextOffsets() {
	sequencer.activityCreated(new TextEditActivity(5, "a", "", p1, s1));
	sequencer.activityCreated(new TextSelectionActivity(6, 0, p1));

	assertFlush(new IActivity[] { new TextEditActivity(5, "a", "", p1, s1)});
    }

    public void testStripReverseRedundantTextOffsets() {
	sequencer.activityCreated(new TextSelectionActivity(5, 0, p1));
	sequencer.activityCreated(new TextEditActivity(5, "a", "", p1, s1));

	assertFlush(new IActivity[] { new TextEditActivity(5, "a", "", p1, s1) });
    }

    public void testStripTextOffsetsWhenNoOtherActivitiesInBetween() {
	sequencer.activityCreated(new TextSelectionActivity(5, 0, new Path(
		"/foo/text.txt")));
	sequencer.activityCreated(new TextSelectionActivity(15, 0, new Path(
		"/foo/text.txt")));
	sequencer.activityCreated(new TextSelectionActivity(16, 0, new Path(
		"/foo/text.txt")));

	assertFlush(new IActivity[] { new TextSelectionActivity(16, 0,
		new Path("/foo/text.txt")) });
    }

    public void testStripRedundantTextOffsetsAndConsiderStartOffset() {
	sequencer.activityCreated(new TextSelectionActivity(3, 2, p1));
	sequencer.activityCreated(new TextEditActivity(5, "a", "", p1, s1));
	sequencer.activityCreated(new TextSelectionActivity(6, 0, p1));

	assertFlush(new IActivity[] { new TextEditActivity(5, "a", "", p1, s1) });
    }

    private void assertFlush(IActivity[] expected) {
	TestHelper.assertList(expected, sequencer.flush());
    }

    private void assertActivities(IActivity[] expected, List<IActivity> actual) {
	TestHelper.assertList(expected, actual);
    }
}
