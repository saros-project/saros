package de.fu_berlin.inf.dpp.optional.jdt;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitDocumentProvider;
import org.eclipse.jdt.internal.ui.javaeditor.JavaDocumentSetupParticipant;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.ui.editors.text.ForwardingDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;

import de.fu_berlin.inf.dpp.editor.internal.SharedDocumentProvider;

/**
 * Implementation of IJDTSupport which really uses JDT functionality. Do not
 * refer to this class directly or Saros will crash if a user has JDT not
 * installed.
 * 
 * All access to this code should go through JDTFacade.
 * 
 * @author oezbek
 * 
 */
public class JDTSupport implements IJDTSupport {

    public void installSharedDocumentProvider() {
        // TODO RESTRICTED ACCESS
        CompilationUnitDocumentProvider cuProvider = (CompilationUnitDocumentProvider) JavaPlugin
                .getDefault().getCompilationUnitDocumentProvider();

        SharedDocumentProvider sharedProvider = new SharedDocumentProvider();

        IDocumentSetupParticipant setupParticipant = new JavaDocumentSetupParticipant();
        ForwardingDocumentProvider parentProvider = new ForwardingDocumentProvider(
                IJavaPartitions.JAVA_PARTITIONING, setupParticipant,
                sharedProvider);

        cuProvider.setParentDocumentProvider(parentProvider);
    }

    @SuppressWarnings("restriction")
    public IDocumentProvider getDocumentProvider() {
        // TODO RESTRICTED ACCESS
        return JavaPlugin.getDefault().getCompilationUnitDocumentProvider();
    }

}
