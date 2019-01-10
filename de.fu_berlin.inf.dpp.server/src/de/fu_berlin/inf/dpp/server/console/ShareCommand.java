package de.fu_berlin.inf.dpp.server.console;

import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.filesystem.IWorkspace;
import de.fu_berlin.inf.dpp.server.filesystem.ServerProjectImpl;
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
