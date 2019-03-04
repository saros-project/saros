package saros.negotiation;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import java.util.Collections;
import java.util.Map;

/**
 * This class contains all the information that the remote user needs during a project negotiation.
 *
 * <p>It contains the local project name, session-wide project id, local file list, and whether the
 * project is completely or partially shared.
 *
 * <p>Furthermore, it contains a map of additional options for the project. These parameters can be
 * used to provide additional, potentially IDE specific information about the project that are
 * needed when creating a local representation of the project as part of the project negotiation.
 *
 * @see FileList
 * @see AdditionalProjectDataFactory
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

  @XStreamAlias("additionalProjectOptions")
  private final Map<String, String> additionalProjectData;

  /**
   * @param referencePointID Session wide ID of the reference point. This ID is the same for all
   *     users.
   * @param projectName Name of the project on inviter side.
   * @param fileList complete list of all files that are part of the sharing for the given project
   * @param additionalProjectData a map of additional project options
   */
  public ProjectNegotiationData(
      String referencePointID,
      String projectName,
      boolean partial,
      FileList fileList,
      Map<String, String> additionalProjectData) {

    this.fileList = fileList;
    this.referencePointName = projectName;
    this.referencePointID = referencePointID;
    this.partial = partial;
    this.additionalProjectData = additionalProjectData;
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

  /**
   * Returns an unmodifiable view of the map of additional project options.
   *
   * <p>It is possible that the host does not provide all entries (or any entries at all), so the
   * results when accessing the mapping should be checked against <code>null</code> before usage.
   *
   * @return an unmodifiable view of the map of additional project options
   */
  public Map<String, String> getAdditionalProjectData() {
    if (additionalProjectData == null) return Collections.emptyMap();

    return Collections.unmodifiableMap(additionalProjectData);
  }
}
