package de.fu_berlin.inf.dpp.optional.jdt;

import java.util.List;

import org.eclipse.ui.texteditor.IDocumentProvider;

import de.fu_berlin.inf.dpp.preferences.IPreferenceManipulator;

public interface IJDTSupport {

    /**
     * Will retrieve the DocumentProvider from the JDT plug-in and install a
     * SharedDocumentProvider by using a ForwardingDocumentProvider.
     */
    public void installSharedDocumentProvider();

    /**
     * Will return the IDocumentProvider from the JDT plug-in by calling
     * JavaPlugin.getDefault().getCompilationUnitDocumentProvider();
     */
    public IDocumentProvider getDocumentProvider();

    /**
     * Will return all {@link IPreferenceManipulator}s which are available for
     * the JDT plug-in.
     */
    public List<IPreferenceManipulator> getPreferenceManipulators();

}
