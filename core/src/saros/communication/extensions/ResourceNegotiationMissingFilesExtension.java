package saros.communication.extensions;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import java.util.List;
import saros.negotiation.FileList;

@XStreamAlias(/* ResourceNegotiationMissingFiles */ "RNMF")
public class ResourceNegotiationMissingFilesExtension extends ResourceNegotiationExtension {

  public static final Provider PROVIDER = new Provider();

  private final List<FileList> fileLists;

  public ResourceNegotiationMissingFilesExtension(
      String sessionID, String negotiationID, List<FileList> fileLists) {
    super(sessionID, negotiationID);
    this.fileLists = fileLists;
  }

  public List<FileList> getFileLists() {
    return fileLists;
  }

  public static class Provider
      extends ResourceNegotiationExtension.Provider<ResourceNegotiationMissingFilesExtension> {

    private Provider() {
      super("rnmf", ResourceNegotiationMissingFilesExtension.class, FileList.class);
    }
  }
}
