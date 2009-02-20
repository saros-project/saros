package de.fu_berlin.inf.dpp.optional.jdt;

import org.eclipse.ui.texteditor.IDocumentProvider;

public interface IJDTSupport {

    /**
     * Will retrieve the DocumentProvider from the JDT Plugin and install a
     * SharedDocumentProvider by using a ForwardingDocumentProvider.
     */
    public void installSharedDocumentProvider();

    /**
     * Will return the IDocumentProvider from the JDT Plugin by calling
     * JavaPlugin.getDefault().getCompilationUnitDocumentProvider();
     */
    public IDocumentProvider getDocumentProvider();

}
