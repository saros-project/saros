package de.fu_berlin.inf.dpp.activities.business;

import java.util.ArrayList;
import java.util.List;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.ProjectExchangeInfo;
import de.fu_berlin.inf.dpp.activities.ProjectExchangeInfoDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.ProjectsAddedActivityDataObject;
import de.fu_berlin.inf.dpp.project.ISarosSession;

/**
 * This Activity is created when a user adds one or more projects to the session
 * It contains the information about the Project see:
 * {@link ProjectExchangeInfo}
 */
public class ProjectsAddedActivity extends AbstractActivity {

    protected List<ProjectExchangeInfo> projectInfos;
    protected String processID;
    protected boolean doStream;

    public ProjectsAddedActivity(User source,
        List<ProjectExchangeInfo> projectInfos, String processID,
        boolean doStream) {
        super(source);
        this.projectInfos = projectInfos;
        this.processID = processID;
        this.doStream = doStream;
    }

    public void dispatch(IActivityReceiver receiver) {
        receiver.receive(this);
    }

    public IActivityDataObject getActivityDataObject(ISarosSession sarosSession) {
        List<ProjectExchangeInfoDataObject> pInfos = new ArrayList<ProjectExchangeInfoDataObject>(
            this.projectInfos.size());
        for (ProjectExchangeInfo fileList : this.projectInfos) {
            pInfos.add(fileList.toProjectInfoDataObject());
        }
        return new ProjectsAddedActivityDataObject(this.source.getJID(),
            pInfos, processID, doStream);
    }

    public List<ProjectExchangeInfo> getProjectInfos() {
        return this.projectInfos;
    }

    public String getProcessID() {
        return processID;
    }

    public boolean doStream() {
        return doStream;
    }
}
