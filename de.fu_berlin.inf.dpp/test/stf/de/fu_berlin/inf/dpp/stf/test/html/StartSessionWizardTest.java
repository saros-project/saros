package de.fu_berlin.inf.dpp.stf.test.html;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.ui.View.MAIN_VIEW;
import static de.fu_berlin.inf.dpp.ui.View.SESSION_WIZARD;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfHtmlTestCase;

public class StartSessionWizardTest extends StfHtmlTestCase {

    @BeforeClass
    public static void selectTesters() throws Exception {
        select(ALICE);
    }

    @Test
    public void shouldOpenDialog() throws Exception {
        assertTrue("Main view did not load", ALICE.htmlBot().view(MAIN_VIEW)
            .isOpen());
        assertTrue("No 'Start Session' button", ALICE.htmlBot().view(MAIN_VIEW)
            .hasElementWithId("start-session"));

        ALICE.htmlBot().view(MAIN_VIEW).button("start-session").click();

        assertTrue(ALICE.htmlBot().view(SESSION_WIZARD).isOpen());
        assertTrue(ALICE.htmlBot().view(SESSION_WIZARD).textElement("header")
            .getText().equals("Choose Files"));

        ALICE.htmlBot().view(SESSION_WIZARD).button("next-button").click();
        assertTrue(ALICE.htmlBot().view(SESSION_WIZARD).textElement("header")
            .getText().equals("Choose Contacts"));

    }
}
