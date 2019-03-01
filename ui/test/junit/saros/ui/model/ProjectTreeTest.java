package saros.ui.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.junit.Test;
import saros.ui.model.ProjectTree.Node;
import saros.ui.model.ProjectTree.Node.Type;

public class ProjectTreeTest {

  @Test
  public void hashCodeAndEquals() {
    HashSet<Node> set = new HashSet<Node>();

    Node base = new Node(new ArrayList<Node>(), "root", Type.PROJECT, true);
    set.add(base);

    Node same = new Node(new ArrayList<Node>(), "root", Type.PROJECT, true);

    assertTrue("should recognize new object with same properties as same", set.contains(same));

    Node notSelected = new Node(new ArrayList<Node>(), "root", Type.PROJECT, false);

    assertTrue(
        "should recognize new object with irrelevant property changes as same",
        set.contains(notSelected));

    Node differentType = new Node(new ArrayList<Node>(), "root", Type.FOLDER, true);

    assertFalse(
        "should detect new object with relevant property changes as different",
        set.contains(differentType));
  }

  @Test
  public void equalsDeepTree() {
    Node node1 = createSimpleTree();
    Node node2 = createSimpleTree();

    assertFalse(node1 == node2);
    assertTrue(node1.equals(node2));
  }

  private Node createSimpleTree() {
    Node grandchildA = Node.fileNode("a", true);
    Node grandchildB = Node.fileNode("b", true);

    List<Node> submembers = new ArrayList<Node>();
    submembers.add(grandchildA);
    submembers.add(grandchildB);

    Node childA = new Node(submembers, "a", Type.FOLDER, true);
    Node childB = Node.fileNode("b", true);

    List<Node> members = new ArrayList<Node>();
    members.add(childA);
    members.add(childB);
    return new Node(members, "root", Type.PROJECT, true);
  }

  @Test(expected = IllegalArgumentException.class)
  public void membersNull() {
    new Node(null, "foo", Type.PROJECT, true);
  }

  @Test
  public void getters() {
    List<Node> members = new ArrayList<Node>();
    Node file = Node.fileNode("file.txt", false);
    members.add(file);
    Node project = new Node(members, "root", Type.PROJECT, true);

    ProjectTree tree = new ProjectTree(project);

    assertEquals("root", tree.getProjectName());
    assertEquals(project, tree.getRoot());

    assertEquals("root", project.getLabel());
    assertEquals(Type.PROJECT, project.getType());
    assertTrue(project.isSelectedForSharing());
    List<Node> projectMembers = project.getMembers();
    assertEquals(1, projectMembers.size());

    Node member = projectMembers.get(0);
    assertEquals("file.txt", member.getLabel());
    assertEquals(Type.FILE, member.getType());
    assertTrue(member.getMembers().isEmpty());
    assertFalse(member.isSelectedForSharing());
  }
}
