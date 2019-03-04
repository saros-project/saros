package saros.negotiation;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * This class contains all the information that the remote user needs during a project negotiation.
 * The {@link FileList} of the whole project, the project name and the session wide project id.
 */
@XStreamAlias("PJNGDATA")
public class ProjectNegotiationData {

  @XStreamAlias("name")
  @XStreamAsAttribute
  private final String referencePointName;

  @XStreamAlias("pid")
  @XStreamAsAttribute
  private final String referencePointID;

  @XStreamAlias("partial")
  @XStreamAsAttribute
  private final boolean partial;

  @XStreamAlias("filelist")
  private final FileList fileList;

  /**
   * @param referencePointID Session wide ID of the reference point. This ID is the same for all
   *     users.
   * @param projectName Name of the project on inviter side.
   * @param fileList complete list of all files that are part of the sharing for the given project
   */
  public ProjectNegotiationData(
      String referencePointID, String projectName, boolean partial, FileList fileList) {

    this.fileList = fileList;
    this.referencePointName = projectName;
    this.referencePointID = referencePointID;
    this.partial = partial;
  }

  public FileList getFileList() {
    return fileList;
  }

  public String getProjectName() {
    return referencePointName;
  }

  public String getReferencePointID() {
    return referencePointID;
  }

  public boolean isPartial() {
    return partial;
  }
}
