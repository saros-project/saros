package saros.server.console;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import saros.filesystem.IPath;
import saros.filesystem.IProject;
import saros.filesystem.IReferencePoint;
import saros.filesystem.IReferencePointManager;
import saros.filesystem.IResource;
import saros.filesystem.IWorkspace;
import saros.server.filesystem.ServerProjectImpl;
import saros.server.filesystem.ServerReferencePointManager;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;

public class ShareCommand extends ConsoleCommand {
  private static final Logger log = Logger.getLogger(ShareCommand.class);
  private final ISarosSessionManager sessionManager;
  private final IWorkspace workspace;
  private final ServerReferencePointManager serverReferencePointManager;

  public ShareCommand(
      ISarosSessionManager sessionManager,
      IWorkspace workspace,
      ServerConsole console,
      ServerReferencePointManager serverReferencePointManager) {
    this.sessionManager = sessionManager;
    this.workspace = workspace;
    this.serverReferencePointManager = serverReferencePointManager;
    console.registerCommand(this);
  }

  @Override
  public String identifier() {
    return "share";
  }

  @Override
  public String help() {
    return "share <PATH>... - Share projects relative to the workspace with session participants";
  }

  @Override
  public void execute(List<String> args, PrintStream out) {
    ISarosSession session = sessionManager.getSession();

    if (session == null) {
      log.error("No Session running, cannot add any resources");
      return;
    }

    try {
      Map<IReferencePoint, List<IResource>> referencePoints = new HashMap<>();
      for (String path : args) {
        try {
          IProject project = new ServerProjectImpl(this.workspace, path);
          fillCoreReferencePointManager(session, project);
          fillServerReferencePointManager(workspace.getLocation(), path);
          referencePoints.put(project.getReferencePoint(), null);
        } catch (Exception e) {
          log.error(path + " could not be added to the session", e);
        }
      }
      sessionManager.addReferencePointResources(referencePoints);
    } catch (Exception e) {
      log.error("Error sharing resources", e);
    }
  }

  private void fillCoreReferencePointManager(ISarosSession session, IProject project) {
    IReferencePointManager referencePointManager =
        session.getComponent(IReferencePointManager.class);
    referencePointManager.putIfAbsent(project.getReferencePoint(), project);
  }

  private void fillServerReferencePointManager(IPath rootPath, String relPathToDirectory) {
    Path path = Paths.get(rootPath.toString(), relPathToDirectory);

    serverReferencePointManager.putIfAbsent(path);
  }
}
