package de.fu_berlin.inf.dpp.ui.model;

import java.util.ArrayList;
import java.util.List;

import de.fu_berlin.inf.dpp.filesystem.IPath;

/**
 * This represents a entry inside a project. It uses a tree structure, where a
 * project entry has a list of his members (childs).
 */
public class ProjectTreeNode {
    private List<ProjectTreeNode> members;
    private String displayName;
    private IPath path;
    private NodeType type;
    private boolean isSelectedForSharing = true;

    public static enum NodeType {
        PROJECT, FOLDER, FILE,
    }

    /**
     * Will create a new {@link ProjectTreeNode}
     * 
     * @param members
     *            a list of members of this node
     * @param displayName
     *            the name used for this node to be displayed in the UI
     * @param path
     *            the path to this node
     * @param type
     *            the type of this node. Can be either: PROJECT, FOLDER or FILE.
     * @param isSelectedForSharing
     *            determinates whether this node is (pre)selected to be shared
     *            in a session. The default is true.
     */
    public ProjectTreeNode(List<ProjectTreeNode> members, String displayName,
        IPath path, NodeType type, boolean isSelectedForSharing) {

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
     * @param path
     *            the path to this node
     * @param type
     *            the type of this node. Can be either: PROJECT, FOLDER or FILE.
     */
    public ProjectTreeNode(IPath path, NodeType type) {
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

    /**
     * @return the path of this node.
     */
    public IPath getPath() {
        return path;
    }

    /**
     * @return the name used for this node to be displayed in the UI
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @return the type of this node. Can be either "PROJECT","FOLDER" or
     *         "FILE".
     */
    public NodeType getType() {
        return this.type;
    }

    /**
     * @param type
     *            the type of this node. Can be either "PROJECT","FOLDER" or
     *            "FILE".
     */
    public void setType(NodeType type) {
        this.type = type;
    }

    /**
     * @return true if this node is selected to be shared in a session
     */
    public boolean isSelectedForSharing() {
        return isSelectedForSharing;
    }

}