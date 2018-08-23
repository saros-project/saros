package de.fu_berlin.inf.dpp.stf.test.html;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfHtmlTestCase;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteHTMLView;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteHTMLView.View;

public class ComponentViewTest extends StfHtmlTestCase {

    @BeforeClass
    public static void selectTesters() throws Exception {
        select(ALICE);
    }

    @Before
    public void setUp() throws Exception {
        ALICE.htmlBot().view(View.COMPONENT_TEST).open();
        assertTrue(ALICE.htmlBot().view(View.COMPONENT_TEST).isOpen());
    }

    @Test
    public void shouldTestInputFields() throws Exception {
        IRemoteHTMLView view = ALICE.htmlBot().view(View.COMPONENT_TEST);

        // text
        assertTrue(view.hasElementWithName("text"));
        view.inputField("text").enter("Some random text");
        assertTrue(view.inputField("text").getValue()
            .equals("Some random text"));
        view.inputField("text").clear();
        assertTrue(view.inputField("text").getValue().equals(""));

        // email
        assertTrue(view.hasElementWithName("email"));
        view.inputField("email").enter("test@mail.com");
        assertTrue(view.inputField("email").getValue().equals("test@mail.com"));
        view.inputField("email").clear();
        assertTrue(view.inputField("email").getValue().equals(""));

        // password
        assertTrue(view.hasElementWithName("password"));
        view.inputField("password").enter("randomPassword123");
        assertTrue(view.inputField("password").getValue()
            .equals("randomPassword123"));
        view.inputField("password").clear();
        assertTrue(view.inputField("password").getValue().equals(""));
    }

    @Test
    public void shouldTestCheckboxes() throws Exception {
        IRemoteHTMLView view = ALICE.htmlBot().view(View.COMPONENT_TEST);

        assertTrue(view.hasElementWithName("checkbox1"));
        assertTrue(view.hasElementWithName("checkbox2"));
        assertTrue(view.hasElementWithName("checkbox3"));

        view.checkbox("checkbox1").check();
        view.checkbox("checkbox2").check();
        view.checkbox("checkbox2").uncheck();
        view.checkbox("checkbox3").check();

        assertTrue(view.checkbox("checkbox1").isChecked());
        assertFalse(view.checkbox("checkbox2").isChecked());
        assertTrue(view.checkbox("checkbox3").isChecked());
    }

    @Test
    public void shouldTestRadioGroup() throws Exception {
        IRemoteHTMLView view = ALICE.htmlBot().view(View.COMPONENT_TEST);

        assertTrue(view.hasElementWithName("radioGroup"));

        view.radioGroup("radioGroup").select("2");
        assertTrue(view.radioGroup("radioGroup").getSelected().equals("2"));

        view.radioGroup("radioGroup").select("1");
        assertTrue(view.radioGroup("radioGroup").getSelected().equals("1"));

        view.radioGroup("radioGroup").select("3");
        assertTrue(view.radioGroup("radioGroup").getSelected().equals("3"));

        assertTrue(view.radioGroup("radioGroup").values()
            .containsAll(Arrays.asList("1", "2", "3")));
        assertTrue(view.radioGroup("radioGroup").size() == 3);
    }
}
