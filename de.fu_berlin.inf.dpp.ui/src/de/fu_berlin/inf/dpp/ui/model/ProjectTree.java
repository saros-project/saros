package de.fu_berlin.inf.dpp.ui.model;

import java.util.List;

/**
 * Represents a project. This class is used to form an JSON equivalent.
 * 
 * A project has a name and a {@link ProjectTreeNode ProjectRoot}.
 */
public class ProjectTree {

    private ProjectTreeNode root;

    private String displayName;

    /**
     * @param root
     *            the root entry of the project
     * @param displayName
     *            the name of this project.
     */
    public ProjectTree(ProjectTreeNode root, String displayName) {
        this.root = root;
        this.displayName = displayName;
    }

    public List<ProjectTreeNode> getMembers() {
        return root.getMembers();

    }

    public String getProjectName() {
        return displayName;
    }

}
