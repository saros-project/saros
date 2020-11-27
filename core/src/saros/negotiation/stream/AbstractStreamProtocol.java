package saros.negotiation.stream;

import saros.filesystem.IFile;
import saros.monitoring.IProgressMonitor;

/**
 * Stream consists of infinite Stream entries. Stream end is signaled by empty
 * <em>referencePointID</em>.
 *
 * <p><b>Stream entry</b>
 *
 * <table>
 * <tr>
 * <th>byte count</th>
 * <th>content</th>
 * </tr>
 * <tr>
 * <td>varying</td>
 * <td>{@code String} of <em>referencePointID</em> encoded via
 * {@link java.io.DataOutputStream#writeUTF(String)}. If {@code String} is
 * empty, signals stream end.</td>
 * </tr>
 * <tr>
 * <td>varying</td>
 * <td>{@code String} of <em>fileName</em> encoded via
 * {@link java.io.DataOutputStream#writeUTF(String)}.</td>
 * </tr>
 * <tr>
 * <td>{@code long}</td>
 * <td>{@code long} of <em>fileSize</em></td>
 * </tr>
 * <tr>
 * <td>defined by <em>fileSize</em></td>
 * <td>{@code bytestream} of <em>fileContent</em></td>
 * </tr>
 * </table>
 *
 * <b>Handle of Character Encoding</b>
 *
 * <p>The used Character Encoding for a file is an IDE/Editor handled setting. Eclipse is using
 * <em>.settings/org.eclipse.core.resources.prefs</em> for this. That's why it should be one of the
 * first files transmitted in reference point sharing.
 *
 * <p>Additionally, Eclipse handle rules like all <em>.properties</em> files till Java 9 use
 * <em>ISO-8859-1</em> Encoding per default (see <a href=
 * "https://docs.oracle.com/javase/9/intl/internationalization-enhancements-jdk-9.htm"
 * >Internationalization Enhancements in JDK 9</a>), which by setting it explicitly would change a
 * convention setting to a useless permanent configuration setting, with probably side effects.<br>
 * In another case it leads to the Situation, that when one tries to set this explicit, it has to be
 * set in the <em>pref</em> file and gets overwritten by the incoming <em>pref</em> file, if send
 * later.
 *
 * <p>Not forgetting to mention, this behavior is field tested in archive transfer mode.
 */
// TODO rework section on encoding as it is outdated and the suggested approach no longer matches
//  the used sharing model
abstract class AbstractStreamProtocol {

  IProgressMonitor monitor;

  public AbstractStreamProtocol(IProgressMonitor monitor) {
    this.monitor = monitor;
  }

  /**
   * generates reference point and filename combination for user visualization
   *
   * @param file
   * @return String of local reference point name and filename
   */
  String displayName(IFile file) {
    String referencePointName = file.getReferencePoint().getName();
    String fileName = file.getReferencePointRelativePath().toString();

    return referencePointName + ": " + fileName;
  }
}
