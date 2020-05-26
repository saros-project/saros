package saros.negotiation;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import java.util.Collections;
import java.util.Map;

/**
 * This class contains all the information that the remote user needs during a project negotiation.
 *
 * <p>It contains the local project name, session-wide project id, and local file list.
 *
 * <p>Furthermore, it contains a map of additional options for the project. These parameters can be
 * used to provide additional, potentially IDE specific information about the project that are
 * needed when creating a local representation of the project as part of the project negotiation.
 *
 * @see FileList
 * @see AdditionalResourceDataFactory
 */
@XStreamAlias("RNDATA")
public class ResourceNegotiationData {

  @XStreamAlias("name")
  @XStreamAsAttribute
  private final String projectName;

  @XStreamAlias("rpid")
  @XStreamAsAttribute
  private final String projectID;

  @XStreamAlias("filelist")
  private final FileList fileList;

  @XStreamAlias("ard")
  private final Map<String, String> additionalProjectData;

  /**
   * @param projectID Session wide ID of the project. This ID is the same for all users.
   * @param projectName Name of the project on inviter side.
   * @param fileList complete list of all files that are part of the sharing for the given project
   * @param additionalProjectData a map of additional project options
   */
  public ResourceNegotiationData(
      String projectID,
      String projectName,
      FileList fileList,
      Map<String, String> additionalProjectData) {

    this.fileList = fileList;
    this.projectName = projectName;
    this.projectID = projectID;
    this.additionalProjectData = additionalProjectData;
  }

  public FileList getFileList() {
    return fileList;
  }

  public String getReferencePointName() {
    return projectName;
  }

  public String getReferencePointID() {
    return projectID;
  }

  /**
   * Returns an unmodifiable view of the map of additional project options.
   *
   * <p>It is possible that the host does not provide all entries (or any entries at all), so the
   * results when accessing the mapping should be checked against <code>null</code> before usage.
   *
   * @return an unmodifiable view of the map of additional project options
   */
  public Map<String, String> getAdditionalResourceData() {
    if (additionalProjectData == null) return Collections.emptyMap();

    return Collections.unmodifiableMap(additionalProjectData);
  }
}
