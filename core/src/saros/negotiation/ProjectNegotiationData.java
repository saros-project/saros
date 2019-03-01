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
  private final String projectName;

  @XStreamAlias("pid")
  @XStreamAsAttribute
  private final String projectID;

  @XStreamAlias("partial")
  @XStreamAsAttribute
  private final boolean partial;

  @XStreamAlias("filelist")
  private final FileList fileList;

  /**
   * @param projectID Session wide ID of the project. This ID is the same for all users.
   * @param projectName Name of the project on inviter side.
   * @param fileList complete list of all files that are part of the sharing for the given project
   */
  public ProjectNegotiationData(
      String projectID, String projectName, boolean partial, FileList fileList) {

    this.fileList = fileList;
    this.projectName = projectName;
    this.projectID = projectID;
    this.partial = partial;
  }

  public FileList getFileList() {
    return fileList;
  }

  public String getProjectName() {
    return projectName;
  }

  public String getProjectID() {
    return projectID;
  }

  public boolean isPartial() {
    return partial;
  }
}
