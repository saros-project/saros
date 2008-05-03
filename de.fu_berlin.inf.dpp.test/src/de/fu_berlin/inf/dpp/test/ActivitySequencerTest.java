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

import org.eclipse.core.runtime.Path;

import junit.framework.TestCase;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.TextSelectionActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.TextEditActivity;
import de.fu_berlin.inf.dpp.activities.EditorActivity;
import de.fu_berlin.inf.dpp.concurrent.ConcurrentManager;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.TimedActivity;
import de.fu_berlin.inf.dpp.net.internal.ActivitySequencer;
import de.fu_berlin.inf.dpp.project.IActivityProvider;

public class ActivitySequencerTest extends TestCase {
    private ActivitySequencer sequencer;
    private IActivityProvider providerMock;

    private TextEditActivity  textEditActivity1;
    private TextEditActivity  textEditActivity2;
    private TextEditActivity  textEditActivity3;
    
    @Override
    protected void setUp() throws Exception {
        sequencer = new ActivitySequencer();
        sequencer.initConcurrentManager(ConcurrentManager.Side.CLIENT_SIDE, new User(new JID("host@jabber.com")), new JID("user@jabber.com"));
        
        providerMock = createMock(IActivityProvider.class);
        sequencer.addProvider(providerMock);
        reset(providerMock);
        
        textEditActivity1 = new TextEditActivity(0, "a", 0);
        textEditActivity2 = new TextEditActivity(5, "b", 0);
        textEditActivity3 = new TextEditActivity(8, "c", 0);
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
        
        assertActivities(new IActivity[]{
            textEditActivity1, textEditActivity2}, 
            sequencer.getLog());
    }
    
    public void testIncomingActivitiesIncTime() {
        sequencer.exec(new TimedActivity(textEditActivity1, 0));
        sequencer.exec(new TimedActivity(textEditActivity2, 1));
        assertEquals(2, sequencer.getTimestamp());
    }
    
    public void testHasSimpleTextChange() {
        sequencer.activityCreated(new TextEditActivity(5, "a", 0));
        
        assertFlush(new IActivity[]{
            new TextEditActivity(5, "a", 0)});
    }

    public void testJoinConsecutiveTextChanges() {
        sequencer.activityCreated(new TextEditActivity(5, "a", 0));
        sequencer.activityCreated(new TextEditActivity(6, "bc", 0));
        sequencer.activityCreated(new TextEditActivity(8, "de", 0));
        
        assertFlush(new IActivity[]{
            new TextEditActivity(5, "abcde", 0)});
    }
    
    public void testDontJoinConsecutiveTextChangesInDifferentFiles() {
        sequencer.activityCreated(new TextEditActivity(5, "a", 0));
        sequencer.activityCreated(new EditorActivity(
            EditorActivity.Type.Activated, new Path("/bla")));
        sequencer.activityCreated(new TextEditActivity(8, "de", 0));
        
        assertFlush(new IActivity[]{
            new TextEditActivity(5, "a", 0), 
            new EditorActivity(EditorActivity.Type.Activated, new Path("/bla")), 
            new TextEditActivity(8, "de", 0)});
    }
    
    public void testSimpleStripRedundantTextOffsets() {
        sequencer.activityCreated(new TextEditActivity(5, "a", 0));
        sequencer.activityCreated(new TextSelectionActivity(6, 0));
        
        assertFlush(
            new IActivity[]{new TextEditActivity(5, "a", 0)});
    }
    
    public void testStripReverseRedundantTextOffsets() {
        sequencer.activityCreated(new TextSelectionActivity(5, 0));
        sequencer.activityCreated(new TextEditActivity(5, "a", 0));
        
        assertFlush(
            new IActivity[]{new TextEditActivity(5, "a", 0)});
    }
    
    public void testStripTextOffsetsWhenNoOtherActivitiesInBetween() {
        sequencer.activityCreated(new TextSelectionActivity(5, 0));
        sequencer.activityCreated(new TextSelectionActivity(15, 0));
        sequencer.activityCreated(new TextSelectionActivity(16, 0));
        
        assertFlush(
            new IActivity[]{new TextSelectionActivity(16, 0)});
    }
    
    public void testStripRedundantTextOffsetsAndConsiderStartOffset() {
        sequencer.activityCreated(new TextSelectionActivity(3, 2));
        sequencer.activityCreated(new TextEditActivity(5, "a", 0));
        sequencer.activityCreated(new TextSelectionActivity(6, 0));

        assertFlush(
            new IActivity[]{new TextEditActivity(5, "a", 0)});
    }
    
    private void assertFlush(IActivity[] expected) {
        TestHelper.assertList(expected, sequencer.flush());
    }
    
    private void assertActivities(IActivity[] expected, List<IActivity> actual) {
        TestHelper.assertList(expected, actual);
    }
}
