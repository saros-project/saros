package de.fu_berlin.inf.dpp.ui.model;

import de.fu_berlin.inf.dpp.ui.model.ProjectTree.Node.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a project. This is an abstraction of the different concepts of what an "project" is in
 * different IDEs. A ProjectTree has {@link Node} which represents the root.
 *
 * <p>This class is used to form an JSON equivalent for the HTML UI.
 */
public class ProjectTree {
  private final Node root;

  /** @param root the node representing the project itself */
  public ProjectTree(Node root) {
    if (root.getType() != Type.FOLDER) {
      throw new IllegalArgumentException("root node needs to be a FOLDER");
    }

    this.root = root;
  }

  public Node getRoot() {
    return root;
  }

  public String getProjectName() {
    return root.getLabel();
  }

  /**
   * This represents a project or a resource (folder, file) inside a project. Several of these nodes
   * make up a tree structure.
   */
  public static class Node {
    private final Type type;
    private final String label;
    private final boolean isSelectedForSharing;
    private final List<Node> members;

    /**
     * An instance's hash depends on the hashes of its members, will be calculated at the end of its
     * initialization.
     */
    private final int hash;

    /**
     * Type of resource a {@link Node} can represent.
     *
     * <p>A {@link Node} can be a parent for other {@link Node}s (i.e. a {@link #PROJECT} or a
     * {@link #FOLDER}, which may have {@link Node#getMembers() members}), or just be a {@link
     * #FILE}.
     */
    public static enum Type {
      FOLDER,
      FILE
    }

    /**
     * Will create a new {@link Node}. Use {@link #fileNode(String, boolean)} for nodes that
     * represent files.
     *
     * @param members A list of members of this node. Can be empty, but must not be <code>null
     *     </code>.
     * @param label the name used for this node to be displayed in the UI
     * @param type the type of this node. Can be either: PROJECT, FOLDER, or FILE.
     * @param isSelectedForSharing determines whether this node is (pre)selected to be shared in a
     *     session.
     */
    public Node(List<Node> members, String label, Type type, boolean isSelectedForSharing) {

      if (members == null) throw new IllegalArgumentException("members cannot be null");

      this.members = members;
      this.label = label;
      this.type = type;
      this.isSelectedForSharing = isSelectedForSharing;

      this.hash = calculateHash();
    }

    /**
     * Create a {@link Node} representing a file.
     *
     * @param label the file name
     * @param isSelectedForSharing determines whether this node is (pre)selected to be shared in a
     *     session.
     * @return a node representing a file (has no members)
     */
    public static Node fileNode(String label, boolean isSelectedForSharing) {
      return new Node(new ArrayList<Node>(), label, Type.FILE, isSelectedForSharing);
    }

    /** @return the list of all members, not <code>null</code>. */
    public List<Node> getMembers() {
      return new ArrayList<Node>(members);
    }

    /** @return the name used for this node to be displayed in the UI */
    public String getLabel() {
      return label;
    }

    /** @return the type of this node. */
    public Type getType() {
      return type;
    }

    /** @return <code>true</code> if this node is selected to be shared in a session */
    public boolean isSelectedForSharing() {
      return isSelectedForSharing;
    }

    @Override
    public String toString() {
      return label + " (" + type + ")";
    }

    /**
     * Two {@link Node}s are considered equals if they have the same {@link #type}, {@link #label},
     * and {@link #members} match. The field {@link #isSelectedForSharing} is not considered.
     */
    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof Node)) {
        return false;
      }

      Node other = (Node) obj;

      if (!this.label.equals(other.label)) return false;

      if (this.type != other.type) return false;

      if (!this.members.containsAll(other.members)) return false;

      if (!other.members.containsAll(this.members)) return false;

      return true;
    }

    /**
     * Two {@link Node}s get the same hash if their {@link #type}, {@link #label}, and the hash of
     * their {@link #members} match. The field {@link #isSelectedForSharing} is not considered.
     */
    @Override
    public int hashCode() {
      return hash;
    }

    private int calculateHash() {
      final int prime = 31;
      int result = prime + label.hashCode();
      result = prime * result + type.hashCode();

      for (Node member : members) {
        result = prime * result + member.hashCode();
      }

      return result;
    }
  }
}
