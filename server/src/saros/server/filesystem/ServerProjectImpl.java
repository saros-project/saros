package saros.server.filesystem;

import static saros.filesystem.IResource.Type.REFERENCE_POINT;

import java.io.IOException;
import java.nio.file.Files;
import saros.filesystem.IReferencePoint;

/** Server implementation of the {@link IReferencePoint} interface. */
public class ServerProjectImpl extends ServerContainerImpl implements IReferencePoint {
  /**
   * Creates a ServerProjectImpl.
   *
   * @param workspace the containing workspace
   * @param name the project's name
   */
  public ServerProjectImpl(ServerWorkspaceImpl workspace, String name) {
    super(workspace, ServerPathImpl.fromString(name));
  }

  @Override
  public Type getType() {
    return REFERENCE_POINT;
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
