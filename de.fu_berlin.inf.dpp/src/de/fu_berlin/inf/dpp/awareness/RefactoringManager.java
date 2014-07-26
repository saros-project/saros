package de.fu_berlin.inf.dpp.awareness;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.history.IRefactoringExecutionListener;
import org.eclipse.ltk.core.refactoring.history.IRefactoringHistoryService;
import org.eclipse.ltk.core.refactoring.history.RefactoringExecutionEvent;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.jivesoftware.smack.XMPPException;
import org.picocontainer.Startable;

import de.fu_berlin.inf.dpp.activities.AbstractActivityReceiver;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.IActivityReceiver;
import de.fu_berlin.inf.dpp.activities.RefactoringActivity;
import de.fu_berlin.inf.dpp.communication.chat.AbstractChat;
import de.fu_berlin.inf.dpp.communication.chat.ChatElement;
import de.fu_berlin.inf.dpp.communication.chat.IChat;
import de.fu_berlin.inf.dpp.communication.chat.IChatServiceListener;
import de.fu_berlin.inf.dpp.communication.chat.muc.MultiUserChat;
import de.fu_berlin.inf.dpp.communication.chat.muc.MultiUserChatService;
import de.fu_berlin.inf.dpp.session.AbstractActivityConsumer;
import de.fu_berlin.inf.dpp.session.AbstractActivityProducer;
import de.fu_berlin.inf.dpp.session.IActivityConsumer;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;

/**
 * This manager is responsible for distributing information about refactoring
 * performed by all session participants.
 * 
 * It listens on the {@link IRefactoringHistoryService} for refactoring which
 * are performed and notifies all attached listeners, when the current user
 * refactored something. Activities are sent around to all session participants,
 * if a session is active.
 * */
public class RefactoringManager extends AbstractActivityProducer implements
    Startable {

    private static final Logger LOG = Logger
        .getLogger(RefactoringManager.class);

    private final List<RefactoringListener> listeners = new CopyOnWriteArrayList<RefactoringListener>();
    private final ISarosSession session;
    private final AwarenessInformationCollector awarenessInformationCollector;
    private MultiUserChatService mucs;
    private IChat sessionChat;

    /**
     * Constructs a new {@link RefactoringManager}.
     * 
     * @param session
     *            The session, in which the action awareness information are
     *            distributed.
     * @param awarenessInformationCollector
     *            This class is used to retrieve the updates from the received
     *            information about performed refactorings.
     * @param mucs
     *            The class which manages the creation and destruction of
     *            {@link MultiUserChat}'s. It is used to display the collected
     *            information about performed refactorings in the multi-user
     *            chat in the Saros view.
     * */
    public RefactoringManager(ISarosSession session,
        AwarenessInformationCollector awarenessInformationCollector,
        MultiUserChatService mucs) {
        this.session = session;
        this.awarenessInformationCollector = awarenessInformationCollector;
        this.mucs = mucs;
        this.mucs.addChatServiceListener(chatServiceListener);
    }

    private final IActivityConsumer consumer = new AbstractActivityConsumer() {
        @Override
        public void exec(IActivity activity) {
            if (Boolean
                .getBoolean("de.fu_berlin.inf.dpp.awareness.SESSION_OVERVIEW")) {
                activity.dispatch(receiverSessionOverview);
            }
            if (Boolean.getBoolean("de.fu_berlin.inf.dpp.awareness.CHAT")) {
                activity.dispatch(receiverChat);
            }
            if (Boolean.getBoolean("de.fu_berlin.inf.dpp.awareness.STATUSLINE")) {
                activity.dispatch(receiverStatusLine);
            }
        }
    };

    private IActivityReceiver receiverSessionOverview = new AbstractActivityReceiver() {
        @Override
        public void receive(RefactoringActivity activity) {

            User user = activity.getSource();
            if (!user.isInSession()) {
                return;
            }

            awarenessInformationCollector.addRefactoring(user,
                activity.getDescription());
            notifyListeners();

            awarenessInformationCollector.removeRefactoring(user);
        }
    };

    // TODO refactor this if this remains in saros after the user test
    // FIXME In some cases, the status line is empty
    private IActivityReceiver receiverStatusLine = new AbstractActivityReceiver() {
        @Override
        public void receive(RefactoringActivity activity) {

            final String description = activity.getDescription();
            if (description == null) {
                return;
            }

            SWTUtils.runSafeSWTAsync(LOG, new Runnable() {

                @Override
                public void run() {
                    IActionBars actionBars = null;
                    try {
                        IWorkbench workbench = PlatformUI.getWorkbench();
                        IWorkbenchPage workbenchPage = workbench
                            .getActiveWorkbenchWindow().getActivePage();
                        IWorkbenchPart part = workbenchPage.getActivePart();
                        IWorkbenchPartSite site = part.getSite();

                        if (site instanceof IViewSite) {
                            actionBars = ((IViewSite) site).getActionBars();
                        } else if (site instanceof IEditorSite) {
                            actionBars = ((IEditorSite) site).getActionBars();
                        }

                        String message = "Refactored: " + description;

                        if (actionBars != null)
                            actionBars.getStatusLineManager().setMessage(
                                message);
                    } catch (Exception e) {
                        // TODO don't handle everything within a try/catch
                        // block
                        LOG.debug(
                            "Could not get action bars for the status line", e);
                        return;
                    }
                }
            });
        }
    };

    // TODO refactor this if this remains in saros after the user test
    private IActivityReceiver receiverChat = new AbstractActivityReceiver() {
        @Override
        public void receive(RefactoringActivity activity) {

            String description = activity.getDescription();
            if (description == null) {
                return;
            }

            String message = "... refactored: " + description;
            sessionChat.addHistoryEntry(new ChatElement(message, activity
                .getSource().getJID(), new Date()));
            ((AbstractChat) sessionChat).notifyJIDMessageReceived(activity
                .getSource().getJID(), message);
        }
    };

    /**
     * Listener for performed refactorings
     * */
    private final IRefactoringExecutionListener refactoringListener = new IRefactoringExecutionListener() {

        @Override
        public void executionNotification(RefactoringExecutionEvent event) {

            if (event.getEventType() == RefactoringExecutionEvent.PERFORMED) {
                String description = event.getDescriptor().getDescription();
                fireActivity(new RefactoringActivity(session.getLocalUser(),
                    description));
            }
        }
    };

    @Override
    public void start() {
        session.addActivityProducer(this);
        session.addActivityConsumer(consumer);

        IRefactoringHistoryService service = RefactoringCore
            .getHistoryService();
        service.addExecutionListener(refactoringListener);
        LOG.debug("Added refactoring listener to the IRefactoringHistoryService");
    }

    @Override
    public void stop() {
        session.removeActivityProducer(this);
        session.removeActivityConsumer(consumer);

        IRefactoringHistoryService service = RefactoringCore
            .getHistoryService();
        service.removeExecutionListener(refactoringListener);
        LOG.debug("Removed refactoring listener from the IRefactoringHistoryService");
    }

    /**
     * This listener is for the lifecyle of an {@link IChat}. Here, the
     * lifecycle of a {@link MultiUserChat MUC} is listened to. If the MUC is
     * created, it stores the reference to it. If the MUC is aborted or
     * destroyed, it sets the reference to it to <code>null</code>.
     * */
    private final IChatServiceListener chatServiceListener = new IChatServiceListener() {
        @Override
        public void chatCreated(IChat chat, boolean createdLocally) {
            if (chat instanceof MultiUserChat) {
                sessionChat = chat;
            }
        }

        @Override
        public void chatDestroyed(IChat chat) {
            if (chat instanceof MultiUserChat) {
                sessionChat = null;
            }
        }

        @Override
        public void chatAborted(IChat chat, XMPPException exception) {
            if (chat instanceof MultiUserChat) {
                sessionChat = null;
            }
        }
    };

    private void notifyListeners() {
        for (RefactoringListener listener : listeners) {
            listener.refactoringActivityChanged();
        }
    }

    /**
     * Adds the given {@link RefactoringListener} to the
     * {@link RefactoringManager}.
     * 
     * @param listener
     *            The given {@link RefactoringListener} to add
     * */
    public void addRefactoringListener(RefactoringListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes the given {@link RefactoringListener} from the
     * {@link RefactoringManager}.
     * 
     * @param listener
     *            The given {@link RefactoringListener} to remove
     * */
    public void removeRefactoringListener(RefactoringListener listener) {
        listeners.remove(listener);
    }
}