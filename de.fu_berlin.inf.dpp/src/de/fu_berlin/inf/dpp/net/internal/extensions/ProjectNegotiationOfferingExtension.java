package de.fu_berlin.inf.dpp.net.internal.extensions;

import java.util.List;

import de.fu_berlin.inf.dpp.activities.ProjectExchangeInfo;

public class ProjectNegotiationOfferingExtension extends
    ProjectNegotiationExtension {

    private List<ProjectExchangeInfo> projectInfos;

    public static final Provider PROVIDER = new Provider();

    public ProjectNegotiationOfferingExtension(String sessionID,
        List<ProjectExchangeInfo> projectInfos, String negotiationID) {
        super(sessionID, negotiationID);
        this.projectInfos = projectInfos;
    }

    /**
     * @return The projectExchangeInformations
     */
    public List<ProjectExchangeInfo> getProjectInfos() {
        return projectInfos;
    }

    public static class Provider
        extends
        ProjectNegotiationExtension.Provider<ProjectNegotiationOfferingExtension> {

        private Provider() {
            super("ProjectNegotiationOffering",
                ProjectNegotiationOfferingExtension.class);
        }
    }

}
