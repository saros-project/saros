package de.fu_berlin.inf.dpp.ui.actions;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.picocontainer.Disposable;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.AbstractSharedEditorListener;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.ISharedEditorListener;
import de.fu_berlin.inf.dpp.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.util.selection.SelectionUtils;
import de.fu_berlin.inf.dpp.ui.util.selection.retriever.SelectionRetrieverFactory;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * This follow mode action is used to select the person to follow.
 * 
 * @author Christopher Oezbek
 * @author Edna Rosen
 */
@Component(module = "action")
public class FollowThisPersonAction extends Action implements Disposable {

    public static final String ACTION_ID = FollowThisPersonAction.class
        .getName();

    private static final Logger log = Logger
        .getLogger(FollowThisPersonAction.class.getName());

    protected ISarosSessionListener sessionListener = new AbstractSarosSessionListener() {
        @Override
        public void sessionStarted(ISarosSession newSarosSession) {
            updateEnablement();
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession) {
            updateEnablement();
        }
    };

    protected ISharedEditorListener editorListener = new AbstractSharedEditorListener() {
        @Override
        public void followModeChanged(User user, boolean isFollowed) {
            updateEnablement();
        }
    };

    protected ISelectionListener selectionListener = new ISelectionListener() {
        public void selectionChanged(IWorkbenchPart part, ISelection selection) {
            updateEnablement();
        }
    };

    @Inject
    protected ISarosSessionManager sessionManager;

    @Inject
    protected EditorManager editorManager;

    public FollowThisPersonAction() {
        super(Messages.FollowThisPersonAction_follow_title);

        SarosPluginContext.initComponent(this);

        setImageDescriptor(new ImageDescriptor() {
            @Override
            public ImageData getImageData() {
                return ImageManager.ICON_BUDDY_SAROS_FOLLOWMODE.getImageData();
            }
        });

        setToolTipText(Messages.FollowThisPersonAction_follow_tooltip);
        setId(ACTION_ID);

        sessionManager.addSarosSessionListener(sessionListener);
        editorManager.addSharedEditorListener(editorListener);
        SelectionUtils.getSelectionService().addSelectionListener(
            selectionListener);
        updateEnablement();
    }

    /**
     * @review runSafe OK
     */
    @Override
    public void run() {
        Utils.runSafeSync(log, new Runnable() {
            public void run() {
                List<User> participants = SelectionRetrieverFactory
                    .getSelectionRetriever(User.class).getSelection();

                if (participants.size() == 1) {
                    User toFollow;
                    if (editorManager.getFollowedUser() == participants.get(0)) {
                        toFollow = null;
                    } else {
                        toFollow = participants.get(0);
                    }
                    log.info("Following: " + toFollow); //$NON-NLS-1$
                    editorManager.setFollowing(toFollow);
                } else {
                    log.warn("More than one participant selected."); //$NON-NLS-1$
                }

            }
        });
    }

    protected void updateEnablement() {
        try {
            ISarosSession sarosSession = sessionManager.getSarosSession();
            List<User> participants = SelectionRetrieverFactory
                .getSelectionRetriever(User.class).getSelection();

            if (sarosSession != null && participants.size() == 1
                && !participants.get(0).isLocal()) {
                if (editorManager.getFollowedUser() == participants.get(0)) {
                    setText(Messages.FollowThisPersonAction_stop_follow_title);
                    setToolTipText(Messages.FollowThisPersonAction_stop_follow_tooltip);
                } else {
                    setText(Messages.FollowThisPersonAction_follow_title);
                    setToolTipText(Messages.FollowThisPersonAction_follow_tooltip);
                }
                setEnabled(true);
            } else {
                setText(Messages.FollowThisPersonAction_follow_title);
                setToolTipText(Messages.FollowThisPersonAction_follow_tooltip);
                setEnabled(false);
            }
        } catch (NullPointerException e) {
            this.setEnabled(false);
        } catch (Exception e) {
            if (!PlatformUI.getWorkbench().isClosing())
                log.error("Unexcepted error while updating enablement", e); //$NON-NLS-1$
        }
    }

    public void dispose() {
        SelectionUtils.getSelectionService().removeSelectionListener(
            selectionListener);
        sessionManager.removeSarosSessionListener(sessionListener);
        editorManager.removeSharedEditorListener(editorListener);
    }
}
