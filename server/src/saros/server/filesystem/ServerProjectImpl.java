package saros.server.filesystem;

import static saros.filesystem.IResource.Type.REFERENCE_POINT;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
  public boolean isNested(IReferencePoint otherReferencePoint) {
    Path p1 = Paths.get(getLocation().toString());

    ServerProjectImpl s2 = (ServerProjectImpl) otherReferencePoint;
    Path p2 = Paths.get(s2.getLocation().toString());

    return p1.equals(p2) || p1.startsWith(p2) || p2.startsWith(p1);
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
