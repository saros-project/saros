package saros.negotiation;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * This class contains all the information that the remote user needs during a resource negotiation.
 *
 * <p>It contains the local reference point name, session-wide reference point id, and local file
 * list.
 *
 * @see FileList
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

  /**
   * @param referencePointID Session wide ID of the reference point. This ID is the same for all
   *     users.
   * @param referencePointName Name of the reference point on inviter side.
   * @param fileList complete list of all files that are part of the sharing for the given reference
   *     point
   */
  public ResourceNegotiationData(
      String referencePointID, String referencePointName, FileList fileList) {

    this.fileList = fileList;
    this.referencePointName = referencePointName;
    this.referencePointID = referencePointID;
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
}
