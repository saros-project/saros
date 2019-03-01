package saros.stf.test.stf.contextmenu;

import static saros.stf.client.tester.SarosTester.ALICE;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import saros.stf.client.StfTestCase;
import saros.stf.server.rmi.superbot.component.contextmenu.peview.IContextMenusInPEView;
import saros.stf.server.rmi.superbot.component.contextmenu.peview.submenu.IRunAsContextMenu;

public class ContextMenuRunAsTest extends StfTestCase {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE);
  }

  @Before
  public void beforeEachTest() throws Exception {
    clearWorkspaces();
  }

  @Test
  public void runAsJavaApplicationTest() throws Exception {

    ALICE.superBot().internal().createJavaProject("foo");
    ALICE.superBot().internal().createFile("foo", "src/foo/Foo.java", createJavaClassContent());

    IContextMenusInPEView javaClass =
        ALICE.superBot().views().packageExplorerView().selectFile("foo", "src", "foo", "Foo.java");

    IRunAsContextMenu runAs = javaClass.runAs();
    runAs.javaApplication();

    ALICE.superBot().views().consoleView().waitUntilCurrentConsoleContainsText("Hello World 4");

    /*
     * it does not matter if clear workspace may fail here, the STF base
     * class will it retry several time because this is the only test
     */
  }

  private String createJavaClassContent() {
    StringBuilder builder = new StringBuilder();
    builder.append("package foo;").append("\n");
    builder.append("public class Foo{");
    builder.append("public static void main(String [] args){");
    builder.append("for(int i = 0; i < 20; System.out.println(\"Hello World \" + (i++)));");
    builder.append("}}");

    return builder.toString();
  }
}
