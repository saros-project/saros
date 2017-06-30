package de.fu_berlin.inf.dpp.stf.test.html;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteHTMLView.View.ADD_CONTACT;
import static de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteHTMLView.View.MAIN_VIEW;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfHtmlTestCase;

public class AddContactTest extends StfHtmlTestCase {
    @BeforeClass
    public static void selectTesters() throws Exception {
        select(ALICE);
    }

    @Test
    public void openAddContactForm() throws Exception {
        ALICE.htmlBot().view(MAIN_VIEW).button("add-contact").click();

        assertTrue("Form for adding a contact did not open", ALICE.htmlBot()
            .view(ADD_CONTACT).isOpen());
    }
}
