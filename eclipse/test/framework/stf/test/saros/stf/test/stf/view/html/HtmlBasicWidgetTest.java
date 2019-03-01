package saros.stf.test.stf.view.html;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static saros.stf.client.tester.SarosTester.ALICE;
import static saros.ui.View.BASIC_WIDGET_TEST;

import java.util.Arrays;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import saros.stf.client.StfHtmlTestCase;
import saros.stf.server.rmi.htmlbot.widget.IRemoteHTMLView;

public class HtmlBasicWidgetTest extends StfHtmlTestCase {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE);
  }

  @Before
  public void setUp() throws Exception {
    ALICE.htmlBot().view(BASIC_WIDGET_TEST).open();
    assertTrue(ALICE.htmlBot().view(BASIC_WIDGET_TEST).isOpen());
  }

  @Test
  public void shouldTestInputFields() throws Exception {
    IRemoteHTMLView view = ALICE.htmlBot().view(BASIC_WIDGET_TEST);

    // text
    assertTrue(view.hasElementWithName("text"));
    view.inputField("text").enter("Some random text");
    assertTrue(view.inputField("text").getValue().equals("Some random text"));
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
    assertTrue(view.inputField("password").getValue().equals("randomPassword123"));
    view.inputField("password").clear();
    assertTrue(view.inputField("password").getValue().equals(""));
  }

  @Test
  public void shouldTestCheckboxes() throws Exception {
    IRemoteHTMLView view = ALICE.htmlBot().view(BASIC_WIDGET_TEST);

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
    IRemoteHTMLView view = ALICE.htmlBot().view(BASIC_WIDGET_TEST);

    assertTrue(view.hasElementWithName("radioGroup"));

    view.radioGroup("radioGroup").select("2");
    assertTrue(view.radioGroup("radioGroup").getSelected().equals("2"));

    view.radioGroup("radioGroup").select("1");
    assertTrue(view.radioGroup("radioGroup").getSelected().equals("1"));

    view.radioGroup("radioGroup").select("3");
    assertTrue(view.radioGroup("radioGroup").getSelected().equals("3"));

    assertTrue(view.radioGroup("radioGroup").values().containsAll(Arrays.asList("1", "2", "3")));
    assertTrue(view.radioGroup("radioGroup").size() == 3);
  }

  @Test
  public void shouldTestSelect() throws Exception {
    IRemoteHTMLView view = ALICE.htmlBot().view(BASIC_WIDGET_TEST);

    assertTrue(view.hasElementWithName("select"));

    view.select("select").select("option2");
    assertTrue(view.select("select").getSelection().equals("option2"));
    view.select("select").select("option3");
    assertTrue(view.select("select").getSelection().equals("option3"));

    assertTrue(
        view.select("select")
            .options()
            .containsAll(Arrays.asList("option1", "option2", "option3")));
    assertTrue(view.select("select").size() == 3);
  }

  @Test
  public void shouldTestMultiSelect() throws Exception {
    IRemoteHTMLView view = ALICE.htmlBot().view(BASIC_WIDGET_TEST);

    assertTrue(view.hasElementWithName("multiSelect"));

    view.multiSelect("multiSelect").select(Arrays.asList("option3"));
    assertTrue(
        view.multiSelect("multiSelect").getSelection().containsAll(Arrays.asList("option3")));
    assertTrue(view.multiSelect("multiSelect").getSelection().size() == 1);

    view.multiSelect("multiSelect").select(Arrays.asList("option1", "option2", "option3"));
    assertTrue(
        view.multiSelect("multiSelect")
            .getSelection()
            .containsAll(Arrays.asList("option1", "option2", "option3")));
    assertTrue(view.multiSelect("multiSelect").getSelection().size() == 3);

    view.multiSelect("multiSelect").select(Arrays.asList("option1", "option3"));
    assertTrue(
        view.multiSelect("multiSelect")
            .getSelection()
            .containsAll(Arrays.asList("option1", "option3")));
    assertTrue(view.multiSelect("multiSelect").getSelection().size() == 2);

    assertTrue(view.multiSelect("multiSelect").size() == 3);
    assertTrue(
        view.multiSelect("multiSelect")
            .options()
            .containsAll(Arrays.asList("option1", "option2", "option2")));
  }

  @Test
  public void shouldTestProgressBar() throws Exception {
    IRemoteHTMLView view = ALICE.htmlBot().view(BASIC_WIDGET_TEST);

    assertTrue(view.hasElementWithName("progressBar"));

    view.progressBar("progressBar").setValue(100);
    assertTrue(view.progressBar("progressBar").getValue() == 100);

    view.progressBar("progressBar").setValue(0);
    assertTrue(view.progressBar("progressBar").getValue() == 0);

    view.progressBar("progressBar").setValue(42);
    assertTrue(view.progressBar("progressBar").getValue() == 42);
  }

  @Test
  public void shouldTestTextElement() throws Exception {
    IRemoteHTMLView view = ALICE.htmlBot().view(BASIC_WIDGET_TEST);

    assertTrue(view.hasElementWithId("button-display-text"));
    assertTrue(view.textElement("button-display-text").getText().equals("none"));

    view.textElement("button-display-text").setText("Some other content");
    assertTrue(view.textElement("button-display-text").getText().equals("Some other content"));
  }

  @Test
  public void shouldTestButton() throws Exception {
    IRemoteHTMLView view = ALICE.htmlBot().view(BASIC_WIDGET_TEST);

    assertTrue(view.hasElementWithId("button"));
    view.button("button").click();
    assertTrue(view.textElement("button-display-text").getText().equals("button"));

    assertTrue(view.hasElementWithId("button-dropdown-1"));
    view.button("button-dropdown-1").click();
    assertTrue(view.textElement("button-display-text").getText().equals("key-dropdown-1"));

    assertTrue(view.hasElementWithId("button-dropdown-2"));
    view.button("button-dropdown-2").click();
    assertTrue(view.textElement("button-display-text").getText().equals("key-dropdown-2"));

    assertTrue(view.hasElementWithId("button-dropdown-3"));
    view.button("button-dropdown-3").click();
    assertTrue(view.textElement("button-display-text").getText().equals("key-dropdown-3"));

    assertTrue(view.hasElementWithId("split-button-1"));
    view.button("split-button-1").click();
    assertTrue(view.textElement("button-display-text").getText().equals("key-split-1"));

    assertTrue(view.hasElementWithId("split-button-2"));
    view.button("split-button-2").click();
    assertTrue(view.textElement("button-display-text").getText().equals("key-split-2"));

    assertTrue(view.hasElementWithId("split-button-3"));
    view.button("split-button-3").click();
    assertTrue(view.textElement("button-display-text").getText().equals("key-split-3"));

    assertTrue(view.button("button").text().equals("Button"));
  }
}
