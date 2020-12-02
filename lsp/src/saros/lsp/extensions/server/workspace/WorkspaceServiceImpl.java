package saros.lsp.extensions.server.workspace;

import com.google.gson.Gson;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.services.WorkspaceService;
import saros.lsp.configuration.Configuration;
import saros.session.AbstractActivityProducer;

/** Empty implementation of the workspace service. */
public class WorkspaceServiceImpl extends AbstractActivityProducer implements WorkspaceService {

  @Override
  public void didChangeConfiguration(DidChangeConfigurationParams params) {
    String settingsJson = params.getSettings().toString();
    Configuration configuration = new Gson().fromJson(settingsJson, Configuration.class);
    LogManager.getRootLogger().setLevel(Level.toLevel(configuration.saros.log.server));
  }

  @Override
  public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {}
}
