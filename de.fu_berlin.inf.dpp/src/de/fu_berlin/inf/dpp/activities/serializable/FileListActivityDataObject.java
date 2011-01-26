package de.fu_berlin.inf.dpp.activities.serializable;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.business.FileListActivity;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISarosSession;

public class FileListActivityDataObject extends AbstractActivityDataObject {

    protected FileList fileList;
    protected String description;
    protected User target;
    protected String projectID;

    public FileListActivityDataObject(JID source, FileList fileList,
        String description, String projectID) {
        super(source);
        this.fileList = fileList;
        this.description = description;
        this.projectID = projectID;
    }

    public IActivity getActivity(ISarosSession sarosSession) {
        return new FileListActivity(sarosSession.getUser(source), fileList,
            description, projectID);
    }

    public FileList getFileList() {
        return fileList;
    }
}
