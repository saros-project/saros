package de.fu_berlin.inf.dpp.ui.actions;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.picocontainer.Disposable;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.util.selection.SelectionUtils;
import de.fu_berlin.inf.dpp.ui.util.selection.retriever.SelectionRetrieverFactory;

/**
 * This action opens a color dialog and checks whether the chosen color is
 * different enough from other colors. If yes, the new color will be sent to the
 * sessionmembers If no, you can change a new color or abort the process
 * 
 * @author cnk and tobi
 */
@Component(module = "action")
public final class ChangeColorAction extends Action implements Disposable {

    private ISelectionListener selectionListener = new ISelectionListener() {
        @Override
        public void selectionChanged(IWorkbenchPart part, ISelection selection) {
            updateEnablement();
        }
    };

    @Inject
    private ISarosSessionManager sessionManager;

    public ChangeColorAction() {
        super(Messages.ChangeColorAction_title);
        SarosPluginContext.initComponent(this);

        setToolTipText(Messages.ChangeColorAction_tooltip);

        setImageDescriptor(ImageManager
            .getImageDescriptor("icons/elcl16/changecolor.png")); //$NON-NLS-1$

        SelectionUtils.getSelectionService().addSelectionListener(
            selectionListener);

        updateEnablement();
    }

    public void updateEnablement() {
        List<User> participants = SelectionRetrieverFactory
            .getSelectionRetriever(User.class).getSelection();

        ISarosSession session = sessionManager.getSarosSession();

        setEnabled(session != null && participants.size() == 1
            && participants.get(0).equals(session.getLocalUser()));

    }

    @Override
    public void run() {

        InputDialog dialog = new InputDialog(EditorAPI.getShell(), "Input",
            "Number", "0", null);

        if (dialog.open() != Window.OK)
            return;

        try {
            int colorID = Integer.parseInt(dialog.getValue());
            ISarosSession session = sessionManager.getSarosSession();

            if (session == null)
                return;

            session.changeColor(colorID);

        } catch (NumberFormatException e) {
            // NOP
        }
    }

    @Override
    public void dispose() {
        SelectionUtils.getSelectionService().removeSelectionListener(
            selectionListener);
    }
}
