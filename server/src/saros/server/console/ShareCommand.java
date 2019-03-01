package saros.server.console;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import saros.filesystem.IProject;
import saros.filesystem.IResource;
import saros.filesystem.IWorkspace;
import saros.server.filesystem.ServerProjectImpl;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;

public class ShareCommand extends ConsoleCommand {
  private static final Logger log = Logger.getLogger(ShareCommand.class);
  private final ISarosSessionManager sessionManager;
  private final IWorkspace workspace;

  public ShareCommand(
      ISarosSessionManager sessionManager, IWorkspace workspace, ServerConsole console) {
    this.sessionManager = sessionManager;
    this.workspace = workspace;
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
      Map<IProject, List<IResource>> projects = new HashMap<>();
      for (String path : args) {
        try {
          IProject project = new ServerProjectImpl(this.workspace, path);
          projects.put(project, null);
        } catch (Exception e) {
          log.error(path + " could not be added to the session", e);
        }
      }
      sessionManager.addResourcesToSession(projects);
    } catch (Exception e) {
      log.error("Error sharing resources", e);
    }
  }
}
