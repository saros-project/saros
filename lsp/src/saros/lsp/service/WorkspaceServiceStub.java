package saros.lsp.service;

import org.apache.log4j.Logger;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.services.WorkspaceService;
import saros.lsp.SarosLauncher;

/** Empty implementation of the workspace service. */
public class WorkspaceServiceStub implements WorkspaceService {

  private static final Logger log = Logger.getLogger(SarosLauncher.class);

  @Override
  public void didChangeConfiguration(DidChangeConfigurationParams params) {
    log.info("didChangeConfiguration");
  }

  @Override
  public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
    log.info("didChangeWatchedFiles");
  }
}
