package de.fu_berlin.inf.dpp.awareness;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;
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
import de.fu_berlin.inf.dpp.activities.FileActivity;
import de.fu_berlin.inf.dpp.activities.FileActivity.Type;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.IActivityReceiver;
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
 * This manager is responsible for distributing information about created files
 * by all session participants.
 * 
 * It listens for {@link FileActivity}'s for created elements and notifies all
 * attached listeners, when the current user created a new file, independent of
 * if it is a class, an interface or anything else. Activities are sent around
 * to all session participants, if a session is active.
 * */
public class FileActivityManager extends AbstractActivityProducer implements
    Startable {

    private static final Logger LOG = Logger
        .getLogger(FileActivityManager.class);

    private final List<FileCreationListener> listeners = new CopyOnWriteArrayList<FileCreationListener>();
    private final ISarosSession session;
    private final AwarenessInformationCollector awarenessInformationCollector;
    private MultiUserChatService mucs;
    private IChat sessionChat;

    /**
     * Constructs a new {@link FileActivityManager}.
     * 
     * @param session
     *            The session, in which the action awareness information are
     *            distributed.
     * @param awarenessInformationCollector
     *            This class is used to retrieve the updates from the received
     *            information about created files.
     * @param mucs
     *            The class which manages the creation and destruction of
     *            {@link MultiUserChat}'s. It is used to display the collected
     *            information about performed refactorings in the multi-user
     *            chat in the Saros view.
     * */
    public FileActivityManager(ISarosSession session,
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
        public void receive(FileActivity activity) {

            User user = activity.getSource();
            if (!user.isInSession()) {
                return;
            }

            if (activity.getType() == Type.CREATED) {
                String file = activity.getPath().getFile().getName();
                awarenessInformationCollector.addCreatedFileName(user, activity
                    .getPath().getFile().getName());
                notifyListeners();

                awarenessInformationCollector.removeCreatedFileName(user);
            }
        }
    };

    // TODO refactor this if this remains in saros after the user test
    // FIXME In some cases, the status line is empty
    private IActivityReceiver receiverStatusLine = new AbstractActivityReceiver() {
        @Override
        public void receive(FileActivity activity) {

            if (activity.getType() != Type.CREATED) {
                return;
            }

            final String fileName = activity.getPath().getFile().getName();
            if (fileName == null) {
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

                        String message = "Created: " + fileName;

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
        public void receive(FileActivity activity) {

            if (activity.getType() != Type.CREATED) {
                return;
            }

            String fileName = activity.getPath().getFile().getName();
            if (fileName == null) {
                return;
            }

            String message = "...created the file " + fileName;
            sessionChat.addHistoryEntry(new ChatElement(message, activity
                .getSource().getJID(), new Date()));
            ((AbstractChat) sessionChat).notifyJIDMessageReceived(activity
                .getSource().getJID(), message);
        }
    };

    @Override
    public void start() {
        session.addActivityProducer(this);
        session.addActivityConsumer(consumer);
    }

    @Override
    public void stop() {
        session.removeActivityProducer(this);
        session.removeActivityConsumer(consumer);
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
        for (FileCreationListener listener : listeners) {
            listener.newFileCreated();
        }
    }

    /**
     * Adds the given {@link FileCreationListener} to the
     * {@link FileActivityManager}.
     * 
     * @param listener
     *            The given {@link FileCreationListener} to add
     * */
    public void addFileCreationListener(FileCreationListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes the given {@link FileCreationListener} from the
     * {@link FileActivityManager}.
     * 
     * @param listener
     *            The given {@link FileCreationListener} to remove
     * */
    public void removeFileCreationListener(FileCreationListener listener) {
        listeners.remove(listener);
    }
}
