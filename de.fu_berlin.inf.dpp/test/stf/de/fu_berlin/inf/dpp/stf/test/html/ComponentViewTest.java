package de.fu_berlin.inf.dpp.stf.test.html;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfHtmlTestCase;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteHTMLView.View;

public class ComponentViewTest extends StfHtmlTestCase {

    @BeforeClass
    public static void selectTesters() throws Exception {
        select(ALICE);
    }

    @Test
    public void shouldOpenComponentTestViewInBrowser() throws Exception {
        ALICE.htmlBot().view(View.COMPONENT_TEST).open();
        assertTrue(ALICE.htmlBot().view(View.COMPONENT_TEST).isOpen());
    }

}
