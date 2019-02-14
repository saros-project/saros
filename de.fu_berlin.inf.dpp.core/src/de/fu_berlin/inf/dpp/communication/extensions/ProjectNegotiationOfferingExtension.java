package de.fu_berlin.inf.dpp.communication.extensions;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import de.fu_berlin.inf.dpp.negotiation.FileList;
import de.fu_berlin.inf.dpp.negotiation.ProjectNegotiationData;
import java.util.List;

@XStreamAlias(/* ProjectNegotiationOffering */ "PNOF")
public class ProjectNegotiationOfferingExtension extends ProjectNegotiationExtension {

  public static final Provider PROVIDER = new Provider();

  private List<ProjectNegotiationData> projectNegotiationData;

  public ProjectNegotiationOfferingExtension(
      String sessionID, String negotiationID, List<ProjectNegotiationData> projectNegotiationData) {
    super(sessionID, negotiationID);
    this.projectNegotiationData = projectNegotiationData;
  }

  public List<ProjectNegotiationData> getProjectNegotiationData() {
    return projectNegotiationData;
  }

  public static class Provider
      extends ProjectNegotiationExtension.Provider<ProjectNegotiationOfferingExtension> {

    private Provider() {
      super(
          "pnof",
          ProjectNegotiationOfferingExtension.class,
          ProjectNegotiationData.class,
          FileList.class);
    }
  }
}
