package de.fu_berlin.inf.dpp.activities;

import de.fu_berlin.inf.dpp.FileList;

public class ProjectExchangeInfoDataObject {

    protected String xmlFileList;
    protected String projectName;
    protected String description;
    protected String projectID;
    protected boolean partial;

    /**
     * 
     * @param projectID
     *            Session wide ID of the project. This ID is the same for all
     *            users.
     * @param projectName
     *            Name of the project on inviter side.
     * @param fileList
     *            Complete List of all Files in the project.
     */
    public ProjectExchangeInfoDataObject(String projectID, String description,
        String projectName, boolean partial, String fileList) {

        this.xmlFileList = fileList;
        this.projectName = projectName;
        this.description = description;
        this.projectID = projectID;
        this.partial = partial;
    }

    public String getFileList() {
        return xmlFileList;
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

    public boolean isPartial() {
        return partial;
    }

    public ProjectExchangeInfo toProjectInfo() {
        return new ProjectExchangeInfo(projectID, description, projectName,
            partial, FileList.fromXML(xmlFileList));
    }
}
