/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2006
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
import de.fu_berlin.inf.dpp.IActivityProvider;
import de.fu_berlin.inf.dpp.activities.CursorOffsetActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.TextEditActivity;
import de.fu_berlin.inf.dpp.activities.TextLoadActivity;
import de.fu_berlin.inf.dpp.internal.ActivitySequencer;

public class ActivitySequencerTest extends TestCase {
    private ActivitySequencer sequencer;
    private IActivityProvider providerMock;

    private TextEditActivity  textEditActivity1;
    private TextEditActivity  textEditActivity2;
    private TextEditActivity  textEditActivity3;
    
    @Override
    protected void setUp() throws Exception {
        sequencer = new ActivitySequencer();
        
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
        
        sequencer.exec(0, textEditActivity1);
        sequencer.exec(1, textEditActivity2);
        verify(providerMock);
    }
    
    public void testTwoReversedConsecutive() {
        providerMock.exec(textEditActivity1);
        providerMock.exec(textEditActivity2);
        replay(providerMock);
        
        sequencer.exec(1, textEditActivity2);
        sequencer.exec(0, textEditActivity1);
        verify(providerMock);
    }
    
    public void testThreeMixedConsecutive() {
        providerMock.exec(textEditActivity1);
        providerMock.exec(textEditActivity2);
        providerMock.exec(textEditActivity3);
        replay(providerMock);
        
        sequencer.exec(1, textEditActivity2);
        sequencer.exec(0, textEditActivity1);
        sequencer.exec(2, textEditActivity3);
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
    
    public void testSimpleIncTime() {
        int start = sequencer.incTime(1);
        assertEquals(start + 1, sequencer.incTime(1));
        assertEquals(start + 2, sequencer.incTime(1));
    }
    
    public void testIncomingActivitiesIncTime() {
        sequencer.exec(0, textEditActivity1);
        sequencer.exec(1, textEditActivity2);
        assertEquals(2, sequencer.incTime(0));
    }
    
    public void testHasSimpleTextChange() {
        sequencer.activityCreated(new TextEditActivity(5, "a", 0));
        
        TestHelper.assertList(new IActivity[]{
            new TextEditActivity(5, "a", 0)}, 
            sequencer.flush());
    }

    public void testJoinConsecutiveTextChanges() {
        sequencer.activityCreated(new TextEditActivity(5, "a", 0));
        sequencer.activityCreated(new TextEditActivity(6, "bc", 0));
        sequencer.activityCreated(new TextEditActivity(8, "de", 0));
        
        TestHelper.assertList(new IActivity[]{
            new TextEditActivity(5, "abcde", 0)}, 
            sequencer.flush());
    }
    
    public void testDontJoinConsecutiveTextChangesInDifferentFiles() {
        sequencer.activityCreated(new TextEditActivity(5, "a", 0));
        sequencer.activityCreated(new TextLoadActivity("/bla"));
        sequencer.activityCreated(new TextEditActivity(8, "de", 0));
        
        TestHelper.assertList(new IActivity[]{
            new TextEditActivity(5, "a", 0), 
            new TextLoadActivity("/bla"), 
            new TextEditActivity(8, "de", 0)}, 
            sequencer.flush());
    }
    
    public void testSimpleStripRedundantTextOffsets() {
        sequencer.activityCreated(new TextEditActivity(5, "a", 0));
        sequencer.activityCreated(new CursorOffsetActivity(5, 0));
        
        TestHelper.assertList(
            new IActivity[]{new TextEditActivity(5, "a", 0)},
            sequencer.flush());
    }
    
    public void testStripReverseRedundantTextOffsets() {
        sequencer.activityCreated(new CursorOffsetActivity(5, 0));
        sequencer.activityCreated(new TextEditActivity(5, "a", 0));
        
        TestHelper.assertList(
            new IActivity[]{new TextEditActivity(5, "a", 0)},
            sequencer.flush());
    }
    
    public void testStripTextOffsetsWhenNoOtherActivitiesInBetween() {
        sequencer.activityCreated(new CursorOffsetActivity(5, 0));
        sequencer.activityCreated(new CursorOffsetActivity(15, 0));
        sequencer.activityCreated(new CursorOffsetActivity(16, 0));
        
        TestHelper.assertList(
            new IActivity[]{new CursorOffsetActivity(16, 0)},
            sequencer.flush());
    }
    
    public void testStripRedundantTextOffsetsAndConsiderStartOffset() {
        sequencer.activityCreated(new CursorOffsetActivity(3, 2));
        sequencer.activityCreated(new TextEditActivity(5, "a", 0));
        sequencer.activityCreated(new CursorOffsetActivity(5, 0));

        TestHelper.assertList(
            new IActivity[]{new TextEditActivity(5, "a", 0)},
            sequencer.flush());
    }
    
    private void assertActivities(IActivity[] expected, List<IActivity> actual) {
        TestHelper.assertList(expected, actual);
    }
}
