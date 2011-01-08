package de.fu_berlin.inf.dpp.ui.actions;

import java.util.Collection;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.picocontainer.Disposable;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.business.ChangeColorActivity;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.annotations.SarosAnnotation;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.project.internal.ChangeColorManager;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * This action opens a color dialog and checks whether the chosen color is
 * different enough from other colors. If yes, the new color will be sent to the
 * sessionmembers If no, you can change a new color or abort the process
 * 
 * @author cnk and tobi
 */
@Component(module = "action")
public class ChangeColorAction extends SelectionProviderAction implements
    Disposable {

    private static final Logger log = Logger.getLogger(ChangeColorAction.class);

    @Inject
    protected ISarosSessionManager sessionManager;

    @Inject
    protected Saros saros;

    @Inject
    protected EditorManager editorManager;

    public ChangeColorAction(ISelectionProvider provider) {
        super(provider, "Change Color");
        Saros.injectDependenciesOnly(this);

        setToolTipText("changes your session colour");
        setImageDescriptor(SarosUI.getImageDescriptor("icons/table_edit.png"));

        selectionChanged(getStructuredSelection());
    }

    @Override
    public void selectionChanged(IStructuredSelection selection) {
        setEnabled(sessionManager.getSarosSession() != null
            && getSelectedUser() != null);
    }

    /**
     * @review runSafe OK
     */
    @Override
    public void run() {
        Util.runSafeSWTSync(log, new Runnable() {
            public void run() {
                boolean done = false;
                ColorDialog changeColor = new ColorDialog(EditorAPI.getShell());
                changeColor.setText("Please choose your color");
                while (!done) {
                    RGB selectedColor = changeColor.open();
                    if (selectedColor == null) {
                        break;
                    }
                    ISarosSession sarosSession = sessionManager
                        .getSarosSession();
                    User localUser = sarosSession.getLocalUser();

                    Collection<User> listOfUsers = new Vector<User>();
                    listOfUsers.addAll(sarosSession.getParticipants());
                    listOfUsers.remove(localUser);

                    if (ChangeColorManager.checkColor(selectedColor,
                        listOfUsers)) {
                        log.info(selectedColor + " was selected.");
                        SarosAnnotation.setUserColor(localUser, selectedColor);
                        for (User user : listOfUsers) {
                            sarosSession.sendActivity(user,
                                new ChangeColorActivity(localUser, user, 
                                    selectedColor));
                        }
                        editorManager.colorChanged();
                        editorManager.refreshAnnotations();
                        done = true;
                    } else {
                        MessageDialog.openInformation(EditorAPI.getShell(),
                            "Color Information", "Please choose another color");
                    }
                }
            }
        });
    }

    public User getSelectedUser() {
        Object selected = getStructuredSelection().getFirstElement();

        if (!(selected instanceof User))
            return null;

        User selectedUser = (User) selected;

        if (selectedUser.isLocal())
            return selectedUser;
        else
            return null;
    }
}
