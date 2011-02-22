package de.fu_berlin.inf.dpp.activities.serializable;

import java.util.ArrayList;
import java.util.List;

import de.fu_berlin.inf.dpp.activities.ProjectExchangeInfo;
import de.fu_berlin.inf.dpp.activities.ProjectExchangeInfoDataObject;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.ProjectsAddedActivity;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISarosSession;

/**
 * This ActivityDO is created when a user adds one or more projects to the
 * session It contains the information about the Project see:
 * {@link ProjectExchangeInfoDataObject}
 */
public class ProjectsAddedActivityDataObject extends AbstractActivityDataObject {

    protected List<ProjectExchangeInfoDataObject> projectInfos;
    protected String processID;
    protected boolean doStream;

    public ProjectsAddedActivityDataObject(JID source,
        List<ProjectExchangeInfoDataObject> projectInfos, String processID,
        boolean doStream) {
        super(source);
        this.projectInfos = projectInfos;
        this.processID = processID;
        this.doStream = doStream;
    }

    public IActivity getActivity(ISarosSession sarosSession) {
        List<ProjectExchangeInfo> pInfos = new ArrayList<ProjectExchangeInfo>(
            this.projectInfos.size());
        for (ProjectExchangeInfoDataObject projectInfo : this.projectInfos) {
            pInfos.add(projectInfo.toProjectInfo());
        }
        return new ProjectsAddedActivity(sarosSession.getUser(source), pInfos,
            processID, doStream);
    }

    public List<ProjectExchangeInfoDataObject> getProjectInfos() {
        return this.projectInfos;
    }

}
