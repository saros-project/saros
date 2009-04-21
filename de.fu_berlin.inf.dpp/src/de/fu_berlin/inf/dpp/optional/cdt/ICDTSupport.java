package de.fu_berlin.inf.dpp.optional.cdt;

import org.eclipse.ui.texteditor.IDocumentProvider;

import de.fu_berlin.inf.dpp.project.SessionManager;

public interface ICDTSupport {

    /**
     * Will retrieve the DocumentProvider from the CDT Plugin and install a
     * SharedDocumentProvider by using a ForwardingDocumentProvider.
     */
    public void installSharedDocumentProvider(SessionManager sessionManager);

    /**
     * Will return the CDocumentProvider from the CDT Plugin by calling
     * CUIPlugin.getDefault().getDocumentProvider();
     */
    public IDocumentProvider getDocumentProvider();

}
