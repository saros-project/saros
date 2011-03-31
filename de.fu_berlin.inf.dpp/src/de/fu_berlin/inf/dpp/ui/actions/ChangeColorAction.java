package de.fu_berlin.inf.dpp.ui.actions;

import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.picocontainer.Disposable;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.business.ChangeColorActivity;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.annotations.SarosAnnotation;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.project.internal.ChangeColorManager;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.util.selection.SelectionUtils;
import de.fu_berlin.inf.dpp.ui.util.selection.retriever.SelectionRetrieverFactory;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * This action opens a color dialog and checks whether the chosen color is
 * different enough from other colors. If yes, the new color will be sent to the
 * sessionmembers If no, you can change a new color or abort the process
 * 
 * @author cnk and tobi
 */
@Component(module = "action")
public class ChangeColorAction extends Action implements Disposable {

    private static final Logger log = Logger.getLogger(ChangeColorAction.class);

    protected ISelectionListener selectionListener = new ISelectionListener() {
        public void selectionChanged(IWorkbenchPart part, ISelection selection) {
            updateEnablement();
        }
    };

    @Inject
    protected ISarosSessionManager sessionManager;

    @Inject
    protected Saros saros;

    @Inject
    protected EditorManager editorManager;

    public ChangeColorAction() {
        super("Change Color");
        SarosPluginContext.initComponent(this);

        setToolTipText("changes your session colour");
        setImageDescriptor(ImageManager
            .getImageDescriptor("icons/elcl16/changecolor.png"));

        SelectionUtils.getSelectionService().addSelectionListener(
            selectionListener);
        updateEnablement();
    }

    public void updateEnablement() {
        try {
            List<User> participants = SelectionRetrieverFactory
                .getSelectionRetriever(User.class).getSelection();
            setEnabled(sessionManager.getSarosSession() != null
                && participants.size() == 1
                && participants.get(0).equals(
                    sessionManager.getSarosSession().getLocalUser()));
        } catch (NullPointerException e) {
            this.setEnabled(false);
        } catch (Exception e) {
            if (!PlatformUI.getWorkbench().isClosing())
                log.error("Unexcepted error while updating enablement", e);
        }
    }

    /**
     * @review runSafe OK
     */
    @Override
    public void run() {
        Utils.runSafeSWTSync(log, new Runnable() {
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

    public void dispose() {
        SelectionUtils.getSelectionService().removeSelectionListener(
            selectionListener);
    }
}
