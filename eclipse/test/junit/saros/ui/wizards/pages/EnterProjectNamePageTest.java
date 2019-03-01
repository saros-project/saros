package saros.ui.wizards.pages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.HashSet;
import java.util.Set;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class EnterProjectNamePageTest {
  private static final String OTHER = "other";
  private static final String MYPROJECT = "myproject";

  private static Set<String> localProjects;
  private static EnterProjectNamePage page;

  @BeforeClass
  public static void setupBeforeClass() {
    page =
        EasyMock.createMockBuilder(EnterProjectNamePage.class)
            .addMockedMethod("projectNameIsUnique")
            .createMock();

    // EasyMock does not support varargs, therefore we just assume two
    // String parameters
    page.projectNameIsUnique(EasyMock.isA(String.class), EasyMock.isA(String.class));
    EasyMock.expectLastCall()
        .andStubAnswer(
            new IAnswer<Boolean>() {
              @Override
              public Boolean answer() throws Throwable {
                String in = (String) EasyMock.getCurrentArguments()[0];
                String res = (String) EasyMock.getCurrentArguments()[1];

                Set<String> local = new HashSet<String>(localProjects);
                local.add(res);

                return !local.contains(in);
              }
            });
    EasyMock.replay(page);
  }

  @Before
  public void setup() {
    localProjects = new HashSet<String>();
  }

  @Test
  public void equalWithoutConflict() {
    String proposal = page.findProjectNameProposal(MYPROJECT, OTHER);
    assertEquals(MYPROJECT, proposal);
  }

  @Test
  public void differentOnWorkspaceConflict() {
    localProjects.add(MYPROJECT);
    String proposal = page.findProjectNameProposal(MYPROJECT, OTHER);

    assertFalse(MYPROJECT.equals(proposal));
    localProjects.add(OTHER);
    assertFalse(localProjects.contains(proposal));
  }

  @Test
  public void differentOnMultipleWorkspaceConflict() {
    localProjects.add(MYPROJECT);
    String oldProposal = page.findProjectNameProposal(MYPROJECT, OTHER);
    localProjects.add(oldProposal);
    String newProposal = page.findProjectNameProposal(MYPROJECT, OTHER);

    assertFalse(oldProposal.equals(newProposal));

    localProjects.add(OTHER);
    assertFalse(localProjects.contains(newProposal));
  }

  @Test
  public void differentOnReservedNamesConflict() {
    String proposal = page.findProjectNameProposal(MYPROJECT, MYPROJECT);
    assertFalse(MYPROJECT.equals(proposal));
  }

  @Test
  public void differentOnCombinedConflict() {
    String oldProposal = page.findProjectNameProposal(MYPROJECT, MYPROJECT);
    localProjects.add(oldProposal);
    String newProposal = page.findProjectNameProposal(MYPROJECT, MYPROJECT);

    assertFalse(oldProposal.equals(newProposal));

    localProjects.add(OTHER);
    assertFalse(localProjects.contains(newProposal));
  }
}
