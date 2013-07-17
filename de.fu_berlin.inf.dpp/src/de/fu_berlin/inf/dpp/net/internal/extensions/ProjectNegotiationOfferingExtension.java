package de.fu_berlin.inf.dpp.net.internal.extensions;

import java.util.List;

import de.fu_berlin.inf.dpp.activities.ProjectExchangeInfo;

public class ProjectNegotiationOfferingExtension extends
    SarosSessionPacketExtension {

    private List<ProjectExchangeInfo> projectInfos;
    private String processID;

    public static final Provider PROVIDER = new Provider();

    public ProjectNegotiationOfferingExtension(String sessionID,
        List<ProjectExchangeInfo> projectInfos, String processID) {
        super(sessionID);
        this.projectInfos = projectInfos;
        this.processID = processID;
    }

    /**
     * @return The projectExchangeInformations
     */
    public List<ProjectExchangeInfo> getProjectInfos() {
        return projectInfos;
    }

    /**
     * 
     * @return The ProcessID of this NegotiationProcess
     */
    public String getProcessID() {
        return processID;
    }

    public static class Provider
        extends
        SarosSessionPacketExtension.Provider<ProjectNegotiationOfferingExtension> {

        private Provider() {
            super("ProjectNegotiationOffering",
                ProjectNegotiationOfferingExtension.class);
        }
    }

}
