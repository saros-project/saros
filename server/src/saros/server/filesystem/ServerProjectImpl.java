package saros.server.filesystem;

import static saros.filesystem.IResource.Type.PROJECT;

import java.io.IOException;
import java.nio.file.Files;
import saros.filesystem.IProject;
import saros.filesystem.IWorkspace;

/** Server implementation of the {@link IProject} interface. */
public class ServerProjectImpl extends ServerContainerImpl implements IProject {

  private static final String DEFAULT_CHARSET = "UTF-8";

  /**
   * Creates a ServerProjectImpl.
   *
   * @param workspace the containing workspace
   * @param name the project's name
   */
  public ServerProjectImpl(IWorkspace workspace, String name) {
    super(workspace, ServerPathImpl.fromString(name));
  }

  @Override
  public Type getType() {
    return PROJECT;
  }

  @Override
  public String getDefaultCharset() {
    // TODO: Read default character set from the project metadata files.
    return DEFAULT_CHARSET;
  }

  /**
   * Creates the underlying folder structure for the project
   *
   * @throws IOException
   */
  public void create() throws IOException {
    Files.createDirectory(toNioPath());
  }
}
