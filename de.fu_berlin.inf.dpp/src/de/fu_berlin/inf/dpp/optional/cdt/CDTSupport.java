package de.fu_berlin.inf.dpp.optional.cdt;

import org.eclipse.cdt.internal.ui.editor.CDocumentSetupParticipant;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.ICPartitions;
import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.ui.editors.text.ForwardingDocumentProvider;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;

import de.fu_berlin.inf.dpp.editor.internal.SharedDocumentProvider;

/**
 * Implementation of ICDTSupport which really uses CDT functionality. Do not
 * refer to this class directly or Saros will crash if a user has CDT not
 * installed.
 * 
 * All access to this code should go through CDTFacade.
 * 
 * @author oezbek
 * 
 */
public class CDTSupport implements ICDTSupport {

    public void installSharedDocumentProvider() {
        // TODO RESTRICTED ACCESS
        TextFileDocumentProvider docProvider = CUIPlugin.getDefault()
                .getDocumentProvider();

        SharedDocumentProvider sharedProvider = new SharedDocumentProvider();

        IDocumentSetupParticipant setupParticipant = new CDocumentSetupParticipant();
        ForwardingDocumentProvider parentProvider = new ForwardingDocumentProvider(
                ICPartitions.C_PARTITIONING, setupParticipant, sharedProvider);
        docProvider.setParentDocumentProvider(parentProvider);
    }

    public IDocumentProvider getDocumentProvider() {
        return CUIPlugin.getDefault().getDocumentProvider();
    }

}
