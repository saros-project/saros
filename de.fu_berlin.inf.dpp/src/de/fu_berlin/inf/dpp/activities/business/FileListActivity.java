package de.fu_berlin.inf.dpp.activities.business;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.serializable.FileListActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.project.ISarosSession;

public class FileListActivity extends AbstractActivity {

    protected FileList fileList;
    protected String description;
    protected String projectID;

    public FileListActivity(User source, FileList fileList, String description,
        String projectID) {
        super(source);
        this.fileList = fileList;
        this.description = description;
        this.projectID = projectID;
    }

    public void dispatch(IActivityReceiver receiver) {
        receiver.receive(this);
    }

    public IActivityDataObject getActivityDataObject(ISarosSession sarosSession) {
        return new FileListActivityDataObject(this.source.getJID(),
            this.fileList, description, projectID);
    }

    public FileList getFileList() {
        return this.fileList;
    }

    public String getDescription() {
        return description;
    }

    public String getProjectID() {
        return projectID;
    }
}
