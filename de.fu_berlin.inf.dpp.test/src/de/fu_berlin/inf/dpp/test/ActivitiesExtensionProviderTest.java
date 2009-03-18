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
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.runtime.Path;
import org.xmlpull.mxp1.MXParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import de.fu_berlin.inf.dpp.activities.EditorActivity;
import de.fu_berlin.inf.dpp.activities.FileActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.TextEditActivity;
import de.fu_berlin.inf.dpp.activities.TextSelectionActivity;
import de.fu_berlin.inf.dpp.activities.EditorActivity.Type;
import de.fu_berlin.inf.dpp.net.TimedActivity;
import de.fu_berlin.inf.dpp.net.internal.ActivitiesPacketExtension;
import de.fu_berlin.inf.dpp.net.internal.ActivitiesProvider;

public class ActivitiesExtensionProviderTest extends TestCase {
	
	private ArrayList<IActivity> activities;
	private MXParser parser;

	@Override
	protected void setUp() throws Exception {
		activities = new ArrayList<IActivity>();
	}

	public void testParsingStopsOnActivitiesEndTag()
			throws XmlPullParserException, IOException {
		activities.add(new TextEditActivity(5, "abc", "d", new Path(
				"hello/world.file"), SarosTest.User1));
		assertCreateAndParseActivities();

		assertTrue(parser.next() == XmlPullParser.END_DOCUMENT);
	}

	public void testCreateAndParseSingleTextEditActivity()
			throws XmlPullParserException, IOException {
		activities.add(new TextEditActivity(5, "abc", "d", new Path(
				"hello/world.file"), SarosTest.User1));
		assertCreateAndParseActivities();
	}

	public void testCreateAndParseMultipleTextEditActivity()
			throws XmlPullParserException, IOException {
		activities.add(new TextEditActivity(5, "abc", "d", new Path(
				"hello/world.file"), SarosTest.User1));
		activities.add(new TextEditActivity(15, "", "", new Path("world.file"),
				SarosTest.User2));
		activities.add(new TextEditActivity(50, "xy", "hello world", new Path(
				"hello.file"), SarosTest.User1));

		assertCreateAndParseActivities();
	}

	public void testCreateAndParsePreservesNewLines()
			throws XmlPullParserException, IOException {
		activities.add(new TextEditActivity(5, "\n  ab   \n \n\nc\n", "\n\n\n \n", new Path("world.file"),
				SarosTest.User2));

		assertCreateAndParseActivities();
	}

	public void testCDATA()
			throws XmlPullParserException, IOException {
		activities.add(new TextEditActivity(5, "lorem]]>ipsum", "fox]]>jumps", new Path("world.file"),
				SarosTest.User2));

		assertCreateAndParseActivities();
	}

	public void testAngleBrackets()
			throws XmlPullParserException, IOException {
		activities.add(new TextEditActivity(5, "<<<>>>", "<<<>>>>", new Path("world.file"),
				SarosTest.User2));

		assertCreateAndParseActivities();
	}

	public void testSingleEditorActivatedActivity()
			throws XmlPullParserException, IOException {

		activities.add(new EditorActivity(Type.Activated, new Path(
				"/foo/text.txt")));

		assertCreateAndParseActivities();
	}

	public void testSingleTextSelectionActivity()
			throws XmlPullParserException, IOException {
		activities.add(new TextSelectionActivity(2, 23, new Path(
				"/foo/text.txt")));

		assertCreateAndParseActivities();
	}

	// public void testResourceAddActivity()
	// throws XmlPullParserException, IOException {
	//        
	// activities.add(new ResourceAddActivity(new Path("/saros/unittest.txt")));
	//
	// assertCreateAndParseActivities();
	// }

	// public void testResourceAddActivityWithContent()
	// throws XmlPullParserException, IOException {
	//        
	// activities.add(new ResourceAddActivity(
	// new Path("/saros/unittest.txt"), "test content"));
	//
	// assertCreateAndParseActivities();
	// }
	//    
	// public void testResourceAddActivityWithContentAndAngledBrackets()
	// throws XmlPullParserException, IOException {
	//
	// activities.add(new ResourceAddActivity(
	// new Path("/saros/unittest.txt"), "test<<<>>>content"));
	//
	//
	// assertCreateAndParseActivities();
	// }
	//    
	// public void testResourceAddActivityWithContentAndPreserveNewLines()
	// throws XmlPullParserException, IOException {
	//    
	// activities.add(new ResourceAddActivity(
	// new Path("/saros/unittest.txt"), "test\ncontent"));
	//    
	// assertCreateAndParseActivities();
	// }
	//    
	// public void
	// testResourceAddActivityWithContentAndPreserveCarriageReturns()
	// throws XmlPullParserException, IOException {
	//
	// activities.add(new ResourceAddActivity(
	// new Path("/saros/unittest.txt"), "test\rcontent"));
	//
	// assertCreateAndParseActivities();
	// }

	public void testFileAddedActivity() throws XmlPullParserException,
			IOException {

		activities.add(new FileActivity(FileActivity.Type.Created, new Path(
				"/saros/unittest.txt")));

		assertCreateAndParseActivities();
	}

	/**
	 * Create a ActivitiesExtension from given parameter, convert it to XML,
	 * process it through the ActivitiesProvider back into an
	 * ActivitiesExtension and check if its activities equal the param given.
	 * 
	 * @throws IOException
	 */
	private void assertCreateAndParseActivities()
			throws XmlPullParserException, IOException {

		List<TimedActivity> timedActivities = new ArrayList<TimedActivity>();
		int time = 0;
		for (IActivity activity : activities) {
			timedActivities.add(new TimedActivity(activity, time++));
		}

		ActivitiesPacketExtension activitiesPacket = new ActivitiesPacketExtension(
				"1", timedActivities);

		ActivitiesProvider provider = new ActivitiesProvider();

		parser = new MXParser();
		parser.setInput(new StringReader(activitiesPacket.toXML()));
		ActivitiesPacketExtension activitiesExtension = provider
				.parseExtension(parser);

		assertEquals(timedActivities, activitiesExtension.getActivities());
	}
}
