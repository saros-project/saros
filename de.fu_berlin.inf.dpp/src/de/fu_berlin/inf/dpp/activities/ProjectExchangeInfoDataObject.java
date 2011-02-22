package de.fu_berlin.inf.dpp.activities;

import de.fu_berlin.inf.dpp.FileList;

public class ProjectExchangeInfoDataObject {

    protected String xmlFileList;
    protected String projectName;
    protected String description;
    protected String projectID;

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
    public ProjectExchangeInfoDataObject(String projectID, String description,
        String projectName, String fileList) {
        this.xmlFileList = fileList;
        this.projectName = projectName;
        this.description = description;
        this.projectID = projectID;
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

    public ProjectExchangeInfo toProjectInfo() {
        return new ProjectExchangeInfo(projectID, description, projectName,
            FileList.fromXML(xmlFileList));
    }

}
