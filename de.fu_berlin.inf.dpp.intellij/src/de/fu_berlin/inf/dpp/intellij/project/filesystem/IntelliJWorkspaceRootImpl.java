package de.fu_berlin.inf.dpp.intellij.project.filesystem;

import de.fu_berlin.inf.dpp.filesystem.IFolder;
import de.fu_berlin.inf.dpp.filesystem.IWorkspaceRoot;

/** IntelliJ implementation of {@link IWorkspaceRoot}. */
public class IntelliJWorkspaceRootImpl implements IWorkspaceRoot {

  @Override
  public IFolder[] getReferenceFolders() {
    /*
     *  FIXME Implement! As IWorkspaceRoot might not be a sufficient concept for the core filesystem,
     *  it is used in the HTML-UI to create a list of files in the workspace that can be shared.
     *  This IWorspaceRoot implementation is needed to avoid IntellJ crash when activating the HTML UI.
     *  Until the core filesystem is reworked, this throws an Exception to indicate that the implementation is missing.
     */
    throw new UnsupportedOperationException();
  }
}
