package saros.communication.extensions;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import java.util.List;
import saros.negotiation.FileList;
import saros.negotiation.ResourceNegotiationData;

@XStreamAlias(/* ResourceNegotiationOffering */ "RNOF")
public class ResourceNegotiationOfferingExtension extends ResourceNegotiationExtension {

  public static final Provider PROVIDER = new Provider();

  private List<ResourceNegotiationData> resourceNegotiationData;

  public ResourceNegotiationOfferingExtension(
      String sessionID,
      String negotiationID,
      List<ResourceNegotiationData> resourceNegotiationData) {
    super(sessionID, negotiationID);
    this.resourceNegotiationData = resourceNegotiationData;
  }

  public List<ResourceNegotiationData> getResourceNegotiationData() {
    return resourceNegotiationData;
  }

  public static class Provider
      extends ResourceNegotiationExtension.Provider<ResourceNegotiationOfferingExtension> {

    private Provider() {
      super(
          "rnof",
          ResourceNegotiationOfferingExtension.class,
          ResourceNegotiationData.class,
          FileList.class);
    }
  }
}
