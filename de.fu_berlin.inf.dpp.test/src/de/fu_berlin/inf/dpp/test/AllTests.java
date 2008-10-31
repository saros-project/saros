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

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

    public static Test suite() {
	TestSuite suite = new TestSuite("Test for de.fu_berlin.inf.dpp.test");
	// $JUnit-BEGIN$
	suite.addTestSuite(ActivitiesExtensionProviderTest.class);
	suite.addTestSuite(SmackFileTransferTest.class);
	suite.addTestSuite(ActivitySequencerTest.class);
	suite.addTestSuite(JIDTest.class);
	suite.addTestSuite(FileListTest.class);
	suite.addTestSuite(SarosTest.class);
	suite.addTestSuite(EditorManagerTest2.class);
	suite.addTestSuite(ContributionAnnotationTest.class);
	suite.addTestSuite(EditorManagerTest.class);
	suite.addTestSuite(EditorAPITest.class);
	suite.addTestSuite(RequestExtensionProviderTest.class);
	// $JUnit-END$
	return suite;
    }
}
