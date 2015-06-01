package de.fu_berlin.inf.dpp.ui.model;

import java.util.ArrayList;
import java.util.List;

import de.fu_berlin.inf.dpp.filesystem.IPath;

/**
 * <p>
 * This represents a entry inside a project. It uses a tree structure, where a
 * project entry has a list of his members (childs) and a reference to his
 * parent. This class has 4 additional properties:
 * </p>
 * <ul>
 * <li><b>displayName</b> the name used for this entry to display in the UI</li>.
 * <li><b>path</b> the path of this entry.</li>
 * <li><b>nodeType</b> can be either "PROJECT","FOLDER" or "FILE".</li>
 * <li><b>isSelectedForSharing</b> determinate whether this entry is
 * (pre)selected to be shared a session.</li>
 * </ul>
 */
public class ProjectTreeNode {
    // TODO: This class does not guarantee any order for the members, provide
    // method to order the members entries or guarantee ordered insert of
    // entries
    private ProjectTreeNode parent;
    private List<ProjectTreeNode> members;
    private String displayName;
    private IPath path;
    private NodeType type;
    private boolean isSelectedForSharing = true;

    protected static enum NodeType {
        PROJECT, FOLDER, FILE,
    }

    /**
     * Will create a new {@link ProjectTreeNode}
     * 
     * @param parent
     *            the parent of this node
     * @param members
     *            a list of members of this node
     * @param displayName
     *            the name used for this entry to display in the UI
     * @param path
     *            the path to the this node
     * @param type
     *            the type of this node. Can be either: PROJECT, FOLDER or FILE.
     * @param isSelectedForSharing
     *            determinates whether this entry is (pre)selected to be shared
     *            in session. The default is true.
     */
    public ProjectTreeNode(ProjectTreeNode parent,
        List<ProjectTreeNode> members, String displayName, IPath path,
        NodeType type, boolean isSelectedForSharing) {
        this.parent = parent;
        this.members = members;
        this.displayName = displayName;
        this.type = type;
        this.path = path;
        this.isSelectedForSharing = isSelectedForSharing;
    }

    /**
     * Will create a new {@link ProjectTreeNode} with <br>
     * <b>isSelcetedForSharing</b> = true </br> <b>displayName</b> =
     * <b>path</b>.lastSegement() and </br> <b>members</b> = new empty ArrayList
     * 
     * @param parent
     *            the parent of this node
     * @param path
     *            the path to the this node
     * @param type
     *            the type of this node. Can be either: PROJECT, FOLDER or FILE.
     */
    public ProjectTreeNode(ProjectTreeNode parent, IPath path, NodeType type) {
        this.parent = parent;
        this.members = new ArrayList<ProjectTreeNode>();
        this.type = type;
        this.displayName = path.lastSegment();
        this.path = path;
    }

    /**
     * @return the list of all members. Please note that there is no designated
     *         order provided.
     */
    public List<ProjectTreeNode> getMembers() {
        return members;
    }

    public IPath getPath() {
        return path;
    }

    public ProjectTreeNode getParent() {
        return parent;
    }

    public String getDisplayName() {
        return displayName;
    }

    public NodeType getType() {
        return this.type;

    }

    public boolean getIsSelected() {
        return isSelectedForSharing;
    }

}