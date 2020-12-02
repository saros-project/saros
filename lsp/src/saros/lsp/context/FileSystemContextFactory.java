package saros.lsp.context;

import saros.context.AbstractContextFactory;
import saros.filesystem.IPathFactory;
import saros.filesystem.IWorkspace;
import saros.lsp.filesystem.LspWorkspace;
import saros.lsp.filesystem.PathFactory;
import saros.repackaged.picocontainer.MutablePicoContainer;

/** ContextFactory for components that are filesystem related. */
public class FileSystemContextFactory extends AbstractContextFactory {

  @Override
  public void createComponents(MutablePicoContainer container) {
    container.addComponent(IPathFactory.class, PathFactory.class);
    container.addComponent(IWorkspace.class, LspWorkspace.class);
  }
}
