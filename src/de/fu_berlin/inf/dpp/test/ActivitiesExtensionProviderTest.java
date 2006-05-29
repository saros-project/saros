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

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.eclipse.core.runtime.Path;
import org.xmlpull.mxp1.MXParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import de.fu_berlin.inf.dpp.activities.CursorLineActivity;
import de.fu_berlin.inf.dpp.activities.CursorOffsetActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.ResourceAddActivity;
import de.fu_berlin.inf.dpp.activities.ResourceRemoveActivity;
import de.fu_berlin.inf.dpp.activities.TextEditActivity;
import de.fu_berlin.inf.dpp.activities.TextLoadActivity;
import de.fu_berlin.inf.dpp.xmpp.ActivitiesPacketExtension;
import de.fu_berlin.inf.dpp.xmpp.ActivitiesProvider;

public class ActivitiesExtensionProviderTest extends TestCase {
    private ArrayList<IActivity> activities;
    private MXParser             parser;

    @Override
    protected void setUp() throws Exception {
        activities = new ArrayList<IActivity>();
    }
    
    public void testParsingStopsOnActivitiesEndTag() 
            throws XmlPullParserException, IOException {
        activities.add(new TextEditActivity(5, "abc", 1));
        assertCreateAndParseActivities();
        
        assertTrue(parser.next() == XmlPullParser.END_DOCUMENT);
    }
    
    public void testCreateAndParseSingleTextEditActivity()
            throws XmlPullParserException, IOException {
        activities.add(new TextEditActivity(5, "abc", 1));
        
        assertCreateAndParseActivities();
    }
    
    public void testCreateAndParseMultipleTextEditActivity()
            throws XmlPullParserException, IOException {
        activities.add(new TextEditActivity(5, "abc", 1));
        activities.add(new TextEditActivity(15, "", 0));
        activities.add(new TextEditActivity(50, "xy", 15));
        
        assertCreateAndParseActivities();
    }
    
    public void testCreateAndParsePreservesNewLines()
            throws XmlPullParserException, IOException {
        activities.add(new TextEditActivity(5, "\nab\n\n\nc\n", 1));
        
        assertCreateAndParseActivities();
    }
    
    // TODO
    public void testCreateAndParseHandlesAngleBrackets()
            throws XmlPullParserException, IOException {
        activities.add(new TextEditActivity(5, "<<<>>>", 1));

        assertCreateAndParseActivities();
    }
//    
//    // TODO
//    public void testCreateAndParsePreservesNewLinesAndCarriageReturns()
//            throws XmlPullParserException, IOException {
//        activities.add(new TextEditActivity(5, "abc\r\n", 1));
//
//        assertCreateAndParseActivities();
//    }
    
    public void testSingleTextLoadActivity()
            throws XmlPullParserException, IOException {
        activities.add(new TextLoadActivity("/foo/text.txt"));

        assertCreateAndParseActivities();
    }
    
    public void testSingleCursorLineActivity() 
            throws XmlPullParserException, IOException {
        activities.add(new CursorLineActivity(5, 37));

        assertCreateAndParseActivities();
    }
    
    public void testSingleCursorOffsetActivity() 
            throws XmlPullParserException, IOException {
        activities.add(new CursorOffsetActivity(2, 23));

        assertCreateAndParseActivities();
    }
    
//    public void testResourceAddActivity() 
//            throws XmlPullParserException, IOException {
//        
//        activities.add(new ResourceAddActivity(new Path("/saros/unittest.txt")));
//
//        assertCreateAndParseActivities();
//    }
    
//    public void testResourceAddActivityWithContent() 
//            throws XmlPullParserException, IOException {
//        
//        activities.add(new ResourceAddActivity(
//            new Path("/saros/unittest.txt"), "test content"));
//
//        assertCreateAndParseActivities();
//    }
//    
//    public void testResourceAddActivityWithContentAndAngledBrackets()
//        throws XmlPullParserException, IOException {
//
//        activities.add(new ResourceAddActivity(
//            new Path("/saros/unittest.txt"), "test<<<>>>content"));
//
//
//        assertCreateAndParseActivities();
//    }
//    
//    public void testResourceAddActivityWithContentAndPreserveNewLines() 
//        throws XmlPullParserException, IOException {
//    
//        activities.add(new ResourceAddActivity(
//            new Path("/saros/unittest.txt"), "test\ncontent"));
//    
//        assertCreateAndParseActivities();
//    }
//    
//    public void testResourceAddActivityWithContentAndPreserveCarriageReturns() 
//        throws XmlPullParserException, IOException {
//
//        activities.add(new ResourceAddActivity(
//            new Path("/saros/unittest.txt"), "test\rcontent"));
//
//        assertCreateAndParseActivities();
//    }
    
    public void testResourceRemoveActivity() 
            throws XmlPullParserException, IOException {
        
        activities.add(new ResourceRemoveActivity(new Path("/saros/unittest.txt")));

        assertCreateAndParseActivities();
    }
    
    /**
     * Create a ActivitiesExtension from given parameter, convert it to XML,
     * process it through the ActivitiesProvider back into an
     * ActivitiesExtension and check if its activities equal the param given.
     * @throws IOException 
     */
    private void assertCreateAndParseActivities() 
            throws XmlPullParserException, IOException {
        
        ActivitiesPacketExtension activitiesPacket = new ActivitiesPacketExtension(
            activities, 0);
        
        ActivitiesProvider provider = new ActivitiesProvider();
        
        parser = new MXParser();
        parser.setInput(new StringReader(activitiesPacket.toXML()));
        ActivitiesPacketExtension activitiesExtension = provider.parseExtension(parser);
        
        assertEquals(activities, activitiesExtension.getActivities());
    }
}
