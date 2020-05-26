package saros.negotiation;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import java.util.Collections;
import java.util.Map;

/**
 * This class contains all the information that the remote user needs during a resource negotiation.
 *
 * <p>It contains the local reference point name, session-wide reference point id, and local file
 * list.
 *
 * <p>Furthermore, it contains a map of additional options for the reference point. These parameters
 * can be used to provide additional, potentially IDE specific information about the reference point
 * that are needed when creating a local representation of the reference point as part of the
 * resource negotiation.
 *
 * @see FileList
 * @see AdditionalResourceDataFactory
 */
@XStreamAlias("RNDATA")
public class ResourceNegotiationData {

  @XStreamAlias("name")
  @XStreamAsAttribute
  private final String referencePointName;

  @XStreamAlias("rpid")
  @XStreamAsAttribute
  private final String referencePointID;

  @XStreamAlias("filelist")
  private final FileList fileList;

  @XStreamAlias("ard")
  private final Map<String, String> additionalResourceData;

  /**
   * @param referencePointID Session wide ID of the reference point. This ID is the same for all
   *     users.
   * @param referencePointName Name of the reference point on inviter side.
   * @param fileList complete list of all files that are part of the sharing for the given reference
   *     point
   * @param additionalResourceData a map of additional resource data
   */
  public ResourceNegotiationData(
      String referencePointID,
      String referencePointName,
      FileList fileList,
      Map<String, String> additionalResourceData) {

    this.fileList = fileList;
    this.referencePointName = referencePointName;
    this.referencePointID = referencePointID;
    this.additionalResourceData = additionalResourceData;
  }

  public FileList getFileList() {
    return fileList;
  }

  public String getReferencePointName() {
    return referencePointName;
  }

  public String getReferencePointID() {
    return referencePointID;
  }

  /**
   * Returns an unmodifiable view of the map of additional resource data.
   *
   * <p>It is possible that the host does not provide all entries (or any entries at all), so the
   * results when accessing the mapping should be checked against <code>null</code> before usage.
   *
   * @return an unmodifiable view of the map of additional resource data
   */
  public Map<String, String> getAdditionalResourceData() {
    if (additionalResourceData == null) return Collections.emptyMap();

    return Collections.unmodifiableMap(additionalResourceData);
  }
}
