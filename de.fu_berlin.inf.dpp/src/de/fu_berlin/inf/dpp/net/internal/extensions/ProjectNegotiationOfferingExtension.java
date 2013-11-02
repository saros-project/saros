package de.fu_berlin.inf.dpp.net.internal.extensions;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import de.fu_berlin.inf.dpp.activities.ProjectExchangeInfo;

@XStreamAlias(/* ProjectNegotiationOffering */"PNOF")
public class ProjectNegotiationOfferingExtension extends
    ProjectNegotiationExtension {

    public static final Provider PROVIDER = new Provider();

    private List<ProjectExchangeInfo> projectInfos;

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
            super("pnof", ProjectNegotiationOfferingExtension.class);
        }
    }

}
