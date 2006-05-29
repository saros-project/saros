///*
// * DPP - Serious Distributed Pair Programming
// * (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2006
// * (c) Riad Djemili - 2006
// * 
// * This program is free software; you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation; either version 1, or (at your option)
// * any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program; if not, write to the Free Software
// * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
// */
//package de.fu_berlin.inf.dpp.test;
//
//import java.util.List;
//
//import junit.framework.TestCase;
//
//import org.eclipse.core.runtime.Path;
//
//import de.fu_berlin.inf.dpp.ActivityCollector;
//import de.fu_berlin.inf.dpp.activities.CursorLineActivity;
//import de.fu_berlin.inf.dpp.activities.CursorOffsetActivity;
//import de.fu_berlin.inf.dpp.activities.IActivity;
//import de.fu_berlin.inf.dpp.activities.RoleActivity;
//import de.fu_berlin.inf.dpp.activities.TextEditActivity;
//import de.fu_berlin.inf.dpp.activities.TextLoadActivity;
//import de.fu_berlin.inf.dpp.test.stubs.TextSelectionStub;
//import de.fu_berlin.inf.dpp.xmpp.JID;
//
//public class ActivityCollectorTest extends TestCase {
//    
//    private ActivityCollector collector;
//
//    @Override
//    protected void setUp() throws Exception {
//        collector = new ActivityCollector();
//    }
//    
//    public void testHasSimpleTextChange() {
//        collector.textChanged(5, "a", 0, 1);
//
//        assertFlush(new IActivity[]{
//            new TextEditActivity(5, "a", 0)});
//    }
//
//    public void testJoinConsecutiveTextChanges() {
//        collector.textChanged(5, "a", 0, 1);
//        collector.textChanged(6, "bc", 0, 1);
//
//        assertFlush(new IActivity[]{
//            new TextEditActivity(5, "abc", 0)});
//    }
//    
//    public void testJoinConsecutiveAndContinueTextChanges() {
//        collector.textChanged(5, "a", 0, 1);
//        collector.textChanged(6, "bc", 0, 1);
//
//        assertFlush(new IActivity[]{
//            new TextEditActivity(5, "abc", 0)});
//        
//        collector.textChanged(8, "d", 0, 1);
//
//        assertFlush(new IActivity[]{
//            new TextEditActivity(8, "d", 0)});
//    }
//    
//    public void testHasDriverChange() {
//        collector.textChanged(5, "abc", 0, 1);
//        collector.driverChanged(new JID("riad@jabber.org"), false);
//        
//        assertFlush(new IActivity[]{
//            new TextEditActivity(5, "abc", 0), 
//            new RoleActivity(new JID("riad@jabber.org"))});
//    }
//    
//    public void testHasTextLoadActivity() {
//        collector.driverPathChanged(new Path("/ju/test.txt"), false);
//        
//        assertFlush(new IActivity[]{
//            new TextLoadActivity("/ju/test.txt")});
//    }
//    
//    public void testDontResendSameTextLoad() {
//        collector.driverPathChanged(new Path("/ju/test.txt"), false);
//
//        assertFlush(new IActivity[]{
//            new TextLoadActivity("/ju/test.txt")});
//
//        collector.driverPathChanged(new Path("/ju/test.txt"), false);
//        assertNull(collector.flush());
//    }
//    
//    public void testCursorLineCreatedOnLowControllerLoD() {
//        collector.setLevelOfDetail(1);
//        collector.cursorChanged(new TextSelectionStub(5, 30, 1, 3));
//        
//        assertFlush(new IActivity[]{
//            new CursorLineActivity(1, 3)});
//    }
//    
//    public void testCursorOffsetCreatedOnHighControllerLoD() {
//        collector.setLevelOfDetail(10);
//        collector.cursorChanged(new TextSelectionStub(5, 30, 1, 3));
//        
//        assertFlush(new IActivity[]{
//            new CursorOffsetActivity(5, 30)});
//    }
//    
//    public void testCursorLineMultipleSimpleFlushes() {
//        collector.setLevelOfDetail(1);
//        collector.cursorChanged(new TextSelectionStub(5, 30, 1, 3));
//        assertFlush(new IActivity[]{
//            new CursorLineActivity(1, 3)});
//        
//        collector.cursorChanged(new TextSelectionStub(50, 10, 4, 5));
//        assertFlush(new IActivity[]{
//            new CursorLineActivity(4, 5)});
//    }
//    
//    public void testCursorOffsetMultipleSimpleFlushes() {
//        collector.setLevelOfDetail(10);
//        collector.cursorChanged(new TextSelectionStub(5, 30, 1, 3));
//        assertFlush(new IActivity[]{
//            new CursorOffsetActivity(5, 30)});
//        
//        collector.cursorChanged(new TextSelectionStub(50, 10, 4, 5));
//        assertFlush(new IActivity[]{
//            new CursorOffsetActivity(50, 10)});
//    }
//    
//    public void testCursorLineIgnorePriorCursorEventsUntilFlush() {
//        collector.setLevelOfDetail(1);
//        collector.cursorChanged(new TextSelectionStub(5, 0, 1, 1));
//        collector.cursorChanged(new TextSelectionStub(6, 0, 1, 1));
//        
//        assertFlush(new IActivity[]{
//            new CursorLineActivity(1, 1)});
//    }
//    
//    public void testCursorOffsetIgnorePriorCursorEventsUntilFlush() {
//        collector.setLevelOfDetail(10);
//        collector.cursorChanged(new TextSelectionStub(5, 0, 1, 1));
//        collector.cursorChanged(new TextSelectionStub(6, 0, 1, 1));
//        
//        assertFlush(new IActivity[]{
//            new CursorOffsetActivity(6, 0)});
//    }
//    
//    public void testCursorLineIsReplacedByTextEdit() {
//        collector.setLevelOfDetail(1);
//        collector.textChanged(5, "abc", 1, 1);
//        collector.cursorChanged(new TextSelectionStub(18, 0, 1, 1));
//        collector.cursorChanged(new TextSelectionStub( 8, 0, 1, 1));
//        
//        assertFlush(new IActivity[]{
//            new TextEditActivity(5, "abc", 1)});
//    }
//    
//    public void testCursorOffsetIsReplacedByTextEdit() {
//        collector.setLevelOfDetail(10);
//        collector.textChanged(5, "abc", 1, 1);
//        collector.cursorChanged(new TextSelectionStub(18, 0, 1, 1));
//        collector.cursorChanged(new TextSelectionStub( 8, 0, 1, 1));
//        
//        assertFlush(new IActivity[]{
//            new TextEditActivity(5, "abc", 1)});
//    }
//    
//    public void testCursorLineDontResendUnneededCursorEventAfterFlush() {
//        collector.setLevelOfDetail(1);
//        collector.cursorChanged(new TextSelectionStub( 5, 0, 1, 1));
//        collector.flush();
//        
//        collector.cursorChanged(new TextSelectionStub( 5, 0, 1, 1));
//        assertNull(collector.flush());
//    }
//    
//    public void testCursorOffsetDontResendUnneededCursorEventAfterFlush() {
//        collector.setLevelOfDetail(1);
//        collector.cursorChanged(new TextSelectionStub( 5, 0, 1, 1));
//        collector.flush();
//        
//        collector.cursorChanged(new TextSelectionStub( 5, 0, 1, 1));
//        assertNull(collector.flush());
//    }
//    
//    /**
//     * Helper method that flushes the activitity controller and compares its
//     * returned activities list with the given parameter <code>expected</code>.
//     */
//    private void assertFlush(IActivity[] expected) {
//        List<IActivity> activities = collector.flush();
//        
//        for (int i = 0; i < expected.length; i++) {
//            assertEquals(expected[i], activities.get(i));
//        }
//        
//        assertEquals(expected.length, activities.size());
//    }
//
///* tests for real diff engine - currently not functional */
//    
//    
//    public void testJoinIntermingledTextChanges() {
//        collector.textChanged(5, 0, "ade");
//        collector.textChanged(6, 0, "bc");
//
//        assertMessage("textChange(5,0,abcde) ");
//    }
//    
//    public void testNonJoinableTextChange() {
//        collector.textChanged(5, 0, "abc");
//        collector.textChanged(50, 0, "def");
//        
//        assertMessage("textChange(5,0,abc) textChange(50,0,def) ");
//    }
//    
//    public void testJoiningTextChangesIncrementsLaterChanges() {
//        collector.textChanged(5,  0, "abc");
//        collector.textChanged(25, 0, "xyz");
//        collector.textChanged(8,  0, "def");
//        
//        assertMessage("textChange(5,0,abcdef) textChange(28,0,xyz) ");
//    }
//    
//    public void testJoinTextChangePartlyDeleted() {
//        collector.textChanged(5, 0, "abcde");
//        collector.textChanged(6, 3, "");
//
//        assertMessage("textChange(5,0,ae) ");
//    }
//    
//    public void testJoinTextChangeDeletedAndAppend() {
//        collector.textChanged(5, 0, "axy");
//        collector.textChanged(6, 2, "bcde");
//
//        assertMessage("textChange(5,0,abcde) ");
//    }
//    
//    public void testTextChangeReplacedByOtherTextChange() {
//        collector.textChanged(5, 0, "abc");
//        collector.textChanged(5, 3, "xyz");
//
//        assertMessage("textChange(5,0,xyz) ");
//    }
//    
//    public void testJoinDeletingTextChanges() {
//        collector.textChanged(3, 2, "");
//        collector.textChanged(3, 3, "");
//
//        assertMessage("textChange(3,5,) ");
//    }
//    
//    public void testJoinDeletingTextChangesReversed() {
//        collector.textChanged(5, 3, "");
//        collector.textChanged(3, 2, "");
//
//        assertMessage("textChange(3,5,) ");
//    }
//    
//    public void testJoinIntermingledDeletingTextChanges() {
//        collector.textChanged(5, 2, "");
//        collector.textChanged(4, 3, "");
//
//        assertMessage("textChange(4,5,) ");
//    }
//    
//    public void testJoinDeleteAndInsertedTextChanges() {
//        collector.textChanged(5, 5, "");
//        collector.textChanged(5, 0, "abc");
//
//        assertMessage("textChange(5,5,abc) ");
//    }
//    
//    public void testJoinDeleteAndInsertedTextChangesReversed() {
//        collector.textChanged(5, 0, "abc");
//        collector.textChanged(8, 5, "");
//
//        assertMessage("textChange(5,5,abc) ");
//    }
//    
//    public void testNonJoinableDeleteAndInsertedTextChanges() {
//        collector.textChanged(5, 5, "");
//        collector.textChanged(6, 0, "abc");
//
//        assertMessage("textChange(5,5,) textChange(6,0,abc) ");
//    }
//    
//    public void testJoinOverflowingDeleteOfInsertedTextChange() {
//        collector.textChanged(5, 0, "abc");
//        collector.textChanged(5, 5, "");
//
//        assertMessage("textChange(5,2,) ");
//    }
//    
//    public void testIncrementLaterTextChanges() {
//        collector.textChanged(25, 0, "abc");
//        collector.textChanged( 5, 0, "xyz");
//        collector.textChanged(27, 0, "de");
//        
//        assertMessage("textChange(5,0,xyz) textChange(27,0,de) textChange(30,0,abc) ");
//    }
//    
//    public void testMergeThreeTextChanges() {
//        collector.textChanged( 5, 0, "abc");
//        collector.textChanged( 9, 0, "efg");
//        collector.textChanged( 8, 1, "d");
//        
//        assertMessage("textChange(5,1,abcdefg) ");
//    }
//}
