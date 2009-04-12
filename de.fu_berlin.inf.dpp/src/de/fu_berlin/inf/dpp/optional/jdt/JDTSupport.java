package de.fu_berlin.inf.dpp.optional.jdt;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitDocumentProvider;
import org.eclipse.jdt.internal.ui.javaeditor.JavaDocumentSetupParticipant;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.ui.editors.text.ForwardingDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;

import de.fu_berlin.inf.dpp.editor.internal.SharedDocumentProvider;
import de.fu_berlin.inf.dpp.preferences.IPreferenceManipulator;

/**
 * Implementation of IJDTSupport which really uses JDT functionality.
 * 
 * All access to this code should go through JDTFacade.
 * 
 * @author oezbek
 * 
 * @noinstantiate Do not refer to this class directly or Saros will crash if a
 *                user has JDT not installed. This class is created using
 *                reflection from JDTFacade if and only if the JDT Plugin is
 *                available
 */
@SuppressWarnings("restriction")
class JDTSupport implements IJDTSupport {

    public void installSharedDocumentProvider() {
        // TODO RESTRICTED ACCESS
        CompilationUnitDocumentProvider cuProvider = (CompilationUnitDocumentProvider) JavaPlugin
            .getDefault().getCompilationUnitDocumentProvider();

        SharedDocumentProvider sharedProvider = new SharedDocumentProvider();

        IDocumentSetupParticipant setupParticipant = new JavaDocumentSetupParticipant();
        ForwardingDocumentProvider parentProvider = new ForwardingDocumentProvider(
            IJavaPartitions.JAVA_PARTITIONING, setupParticipant, sharedProvider);

        cuProvider.setParentDocumentProvider(parentProvider);
    }

    public IDocumentProvider getDocumentProvider() {
        // TODO RESTRICTED ACCESS
        return JavaPlugin.getDefault().getCompilationUnitDocumentProvider();
    }

    public List<IPreferenceManipulator> getPreferenceManipulators() {
        // TODO RESTRICTED ACCESS

        List<IPreferenceManipulator> result = new ArrayList<IPreferenceManipulator>();

        result.add(new SaveActionConfigurator());

        return result;
    }

}
