package de.fu_berlin.inf.dpp.communication.extensions;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import de.fu_berlin.inf.dpp.negotiation.FileList;
import de.fu_berlin.inf.dpp.negotiation.ReferencePointNegotiationData;
import de.fu_berlin.inf.dpp.negotiation.TransferType;

@XStreamAlias(/* ProjectNegotiationOffering */"PNOF")
public class ProjectNegotiationOfferingExtension extends
    ProjectNegotiationExtension {

    public static final Provider PROVIDER = new Provider();

    private List<ReferencePointNegotiationData> projectNegotiationData;
    private TransferType transferType;

    public ProjectNegotiationOfferingExtension(String sessionID,
        String negotiationID,
        List<ReferencePointNegotiationData> projectNegotiationData,
        TransferType transferType) {
        super(sessionID, negotiationID);
        this.projectNegotiationData = projectNegotiationData;
        this.transferType = transferType;
    }

    public List<ReferencePointNegotiationData> getProjectNegotiationData() {
        return projectNegotiationData;
    }

    public TransferType getTransferType() {
        return transferType;
    }

    public static class Provider
        extends
        ProjectNegotiationExtension.Provider<ProjectNegotiationOfferingExtension> {

        private Provider() {
            super("pnof", ProjectNegotiationOfferingExtension.class,
                ReferencePointNegotiationData.class, TransferType.class,
                FileList.class);
        }
    }

}
