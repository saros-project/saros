package de.fu_berlin.inf.dpp.ui.model;

import de.fu_berlin.inf.dpp.filesystem.IProject;

/**
 * Represents a project. This is an abstraction of the different concepts of
 * what an "project" is in different IDEs. A ProejctTree has a name and
 * {@link ProjectTreeNode} which represents the root.
 * 
 * This class is used to form an JSON equivalent for the HTML UI. This model can
 * be used to create a common {@link IProject} which is needed for the
 * ProjectNegotiation.
 * 
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

    public ProjectTreeNode getRoot() {
        return root;
    }

    public String getProjectName() {
        return displayName;
    }

}
