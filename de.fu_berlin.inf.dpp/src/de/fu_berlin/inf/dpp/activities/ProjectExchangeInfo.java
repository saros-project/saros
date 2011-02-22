package de.fu_berlin.inf.dpp.activities;

import de.fu_berlin.inf.dpp.FileList;

/**
 * This class contains all the information that an invited user needs. The
 * {@link FileList} of the whole project, the projectName and the session wide
 * projectID.
 */
public class ProjectExchangeInfo {
    protected FileList fileList;
    protected String projectName;
    protected String projectID;

    // The description is not used yet, but there was a description field all
    // the time and I didn't want to delete it. This field could be useful
    // sometime...
    protected String description;

    /**
     * 
     * @param projectID
     *            session wide ID of the project. This ID is the same on all
     *            clients/buddies
     * @param projectName
     *            Name of the project on inviter side.
     * @param fileList
     *            Complete List of all Files in the project.
     */
    public ProjectExchangeInfo(String projectID, String description,
        String projectName, FileList fileList) {
        this.fileList = fileList;
        this.projectName = projectName;
        this.description = description;
        this.projectID = projectID;
    }

    public FileList getFileList() {
        return fileList;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getDescription() {
        return description;
    }

    public String getProjectID() {
        return projectID;
    }

    public ProjectExchangeInfoDataObject toProjectInfoDataObject() {
        return new ProjectExchangeInfoDataObject(projectID, description,
            projectName, fileList.toXML());
    }
}