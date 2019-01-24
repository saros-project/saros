package de.fu_berlin.inf.dpp.server.console;

import de.fu_berlin.inf.dpp.filesystem.IFolder;
import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.filesystem.IWorkspace;
import de.fu_berlin.inf.dpp.server.filesystem.ServerFolderImplV2;
import de.fu_berlin.inf.dpp.server.filesystem.ServerPathImpl;
import de.fu_berlin.inf.dpp.server.filesystem.ServerReferencePointManager;
import de.fu_berlin.inf.dpp.session.IReferencePointManager;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

public class ShareCommand extends ConsoleCommand {
  private static final Logger log = Logger.getLogger(ShareCommand.class);
  private final ISarosSessionManager sessionManager;
  private final IWorkspace workspace;
  private final ServerReferencePointManager serverReferencePointManager;

  public ShareCommand(
      ISarosSessionManager sessionManager,
      IWorkspace workspace,
      ServerReferencePointManager serverReferencePointManager,
      ServerConsole console) {
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
      IReferencePointManager referencePointManager =
          session.getComponent(IReferencePointManager.class);
      for (String path : args) {
        try {
          IFolder sourceFolder =
              new ServerFolderImplV2(this.workspace.getLocation(), ServerPathImpl.fromString(path));
          IReferencePoint referencePoint = ServerReferencePointManager.create(workspace);
          referencePointManager.put(referencePoint, sourceFolder);
          serverReferencePointManager.put(referencePoint, this.workspace.getLocation().toFile());
          referencePoints.put(referencePoint, null);
        } catch (Exception e) {
          log.error(path + " could not be added to the session", e);
        }
      }
      sessionManager.addResourcesToSession(referencePoints);
    } catch (Exception e) {
      log.error("Error sharing resources", e);
    }
  }
}
