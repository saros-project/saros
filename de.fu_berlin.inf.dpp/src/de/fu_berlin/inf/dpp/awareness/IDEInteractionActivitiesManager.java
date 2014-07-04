package de.fu_berlin.inf.dpp.awareness;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.jivesoftware.smack.XMPPException;
import org.picocontainer.Startable;

import de.fu_berlin.inf.dpp.activities.AbstractActivityReceiver;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.IActivityReceiver;
import de.fu_berlin.inf.dpp.activities.IDEInteractionActivity;
import de.fu_berlin.inf.dpp.activities.IDEInteractionActivity.Element;
import de.fu_berlin.inf.dpp.activities.IDEInteractionActivity.Status;
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
 * This manager is responsible for distributing information about dialogs and
 * views being opened or focused by all session participants.
 * 
 * It listens on the workbench state for dialogs and views and notifies all
 * attached listeners, when the current user opened a new dialog or focused a
 * view. Activities are sent around to all session participants, if a session is
 * active.
 * */
public class IDEInteractionActivitiesManager extends AbstractActivityProducer
    implements Startable {

    private static final Logger LOG = Logger
        .getLogger(IDEInteractionActivitiesManager.class);

    private final List<IDEInteractionActivitiesListener> listeners = new CopyOnWriteArrayList<IDEInteractionActivitiesListener>();
    private final ISarosSession session;
    private final AwarenessInformationCollector awarenessInformationCollector;
    private MultiUserChatService mucs;
    private IChat sessionChat;
    private IWorkbench workbench;
    private IWorkbenchPage workbenchPage;
    private WizardDialog wizardDialog;

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

    /**
     * Listener for opening or closing shells, which normally are dialogs
     * <p>
     * TODO Refactor this to a named class
     * */
    private final Listener shellListener = new Listener() {

        @Override
        public void handleEvent(Event event) {

            if (event == null || event.widget == null) {
                // in some situations, this may happen but can be ignored
                return;
            }

            if (event.widget.getClass() != org.eclipse.swt.widgets.Shell.class)
                return;

            Shell shell = (Shell) event.widget;
            if (shell.getParent() == null) {
                // it is the main window, do nothing
                return;
            }

            // if the shell is a wizard dialog, title changes with page change
            if (shell.getData() instanceof WizardDialog) {
                // TODO this works because even when there are two wizard
                // dialogs the same time, the listener is not added to the same
                // wizard dialog twice. Therefore, the listener always listens
                // to the 'correct' wizard dialog
                wizardDialog = (WizardDialog) shell.getData();
                wizardDialog.addPageChangedListener(wizardDialogListener);
            }

            String currentDialogTitle = shell.getText();

            // if a view in a tab folder is disposed, SWT.Dispose is fired, too,
            // with an empty viewpart title
            if (currentDialogTitle.isEmpty())
                return;

            if (event.type == SWT.Activate) {

                // a dialog was opened or activated
                fireActivity(new IDEInteractionActivity(session.getLocalUser(),
                    currentDialogTitle, Element.DIALOG, Status.FOCUS));

            } else if (event.type == SWT.Deactivate) {

                // a dialog was deactivated due to nested dialogs or by
                // unfocusing the workbench
                fireActivity(new IDEInteractionActivity(session.getLocalUser(),
                    currentDialogTitle, Element.DIALOG, Status.UNFOCUS));

            } else if (event.type == SWT.Dispose) {

                // if a wizard dialog is closed, this event type is also fired,
                // because the shell remains the same, thus remove the listener
                if (shell.getData() instanceof WizardDialog
                    && wizardDialog != null) {
                    wizardDialog
                        .removePageChangedListener(wizardDialogListener);
                    wizardDialog = null;
                }

                // a (nested) dialog was closed
                fireActivity(new IDEInteractionActivity(session.getLocalUser(),
                    currentDialogTitle, Element.DIALOG, Status.UNFOCUS));

                // if the dialog's parent is the main window, closing this
                // navigates the user to the last active view
                if (shell.getParent().getParent() == null && workbench != null
                    && workbenchPage != null
                    && workbenchPage.getActivePart() != null) {
                    SWTUtils.runSafeSWTAsync(LOG, new Runnable() {
                        @Override
                        public void run() {
                            if (workbenchPage == null)
                                return;

                            String landingView = null;
                            if (workbenchPage.getActivePart() instanceof IEditorPart) {
                                // TODO find better way than hardcoding this
                                landingView = "Editor";
                            } else {
                                landingView = workbenchPage.getActivePart()
                                    .getTitle();
                            }
                            fireActivity(new IDEInteractionActivity(session
                                .getLocalUser(), landingView, Element.VIEW,
                                Status.FOCUS));
                        }
                    });
                }
            }
        }
    };

    /**
     * Listener for page changed events in wizard dialogs, which also change the
     * title of the dialogs
     * */
    private final IPageChangedListener wizardDialogListener = new IPageChangedListener() {

        @Override
        public void pageChanged(PageChangedEvent event) {

            String oldTitle = awarenessInformationCollector
                .getOpenIDEElementTitle(session.getLocalUser());

            // FIXME use
            // ((IWizardPage)event.getSource()).getControl().getShell();
            // and check if the shell has the focus / is active

            String newTitle = wizardDialog.getShell().getText();

            // if the first dialog of session is opened, there is no old title
            if (oldTitle != null) {
                fireActivity(new IDEInteractionActivity(session.getLocalUser(),
                    oldTitle, Element.DIALOG, Status.UNFOCUS));
            }

            fireActivity(new IDEInteractionActivity(session.getLocalUser(),
                newTitle, Element.DIALOG, Status.FOCUS));
        }
    };

    /**
     * Listener for activating or deactivating views
     * <p>
     * TODO refactor this class to a named class
     * */
    private final IPartListener2 viewListener = new IPartListener2() {

        @Override
        public void partActivated(IWorkbenchPartReference partReference) {
            if (partReference.getPart(false) instanceof IEditorPart) {
                // if editor is activated don't show the file's name
                // TODO find better way than hardcoding this
                fireActivity(new IDEInteractionActivity(session.getLocalUser(),
                    "Editor", Element.VIEW, Status.FOCUS));
            } else if (partReference.getPart(false) instanceof IViewPart) {
                fireActivity(new IDEInteractionActivity(session.getLocalUser(),
                    partReference.getTitle(), Element.VIEW, Status.FOCUS));
            }
        }

        @Override
        public void partDeactivated(IWorkbenchPartReference partReference) {
            if (partReference.getPart(false) instanceof IEditorPart) {
                // if editor is activated don't show the file's name
                // TODO find better way than hardcoding this
                fireActivity(new IDEInteractionActivity(session.getLocalUser(),
                    "Editor", Element.VIEW, Status.UNFOCUS));
            } else if (partReference.getPart(false) instanceof IViewPart) {
                fireActivity(new IDEInteractionActivity(session.getLocalUser(),
                    partReference.getTitle(), Element.VIEW, Status.UNFOCUS));
            }
        }

        @Override
        public void partClosed(IWorkbenchPartReference partReference) {
            // do nothing
        }

        @Override
        public void partOpened(IWorkbenchPartReference partReference) {
            // do nothing
        }

        @Override
        public void partBroughtToTop(IWorkbenchPartReference partReference) {
            // do nothing
        }

        @Override
        public void partHidden(IWorkbenchPartReference partReference) {
            // do nothing
        }

        @Override
        public void partInputChanged(IWorkbenchPartReference partReference) {
            // do nothing
        }

        @Override
        public void partVisible(IWorkbenchPartReference partReference) {
            // do nothing
        }
    };

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

    /**
     * Constructs a new {@link IDEInteractionActivitiesManager}.
     * 
     * @param session
     *            The session, in which the action awareness information are
     *            distributed.
     * @param awarenessInformationCollector
     *            This class is used to retrieve the updates from the received
     *            information about opened/closed dialogs or
     *            activated/deactivated views.
     * @param mucs
     *            The class which manages the creation and destruction of
     *            {@link MultiUserChat}'s. It is used to display the collected
     *            information about opened/closed dialogs and
     *            activated/deactivated views in the multi-user chat in the
     *            Saros view.
     * */
    public IDEInteractionActivitiesManager(ISarosSession session,
        AwarenessInformationCollector awarenessInformationCollector,
        MultiUserChatService mucs) {
        this.awarenessInformationCollector = awarenessInformationCollector;
        this.mucs = mucs;
        this.session = session;
        this.mucs.addChatServiceListener(chatServiceListener);
    }

    @Override
    public void start() {
        session.addActivityProducer(this);
        session.addActivityConsumer(consumer);

        /*
         * TODO use a syncExec with timeout instead, we could miss some
         * activities (not very likely though)
         */
        SWTUtils.runSafeSWTAsync(LOG, new Runnable() {
            @Override
            public void run() {
                // get workbench needed for dialog and view listeners
                try {
                    workbench = PlatformUI.getWorkbench();
                } catch (IllegalStateException e) {
                    LOG.warn(
                        "Workbench not available. IDE interaction information will not be displayed.",
                        e);
                    return;
                }

                // install listener for dialog events
                workbench.getDisplay().addFilter(SWT.Activate, shellListener);
                workbench.getDisplay().addFilter(SWT.Deactivate, shellListener);
                workbench.getDisplay().addFilter(SWT.Dispose, shellListener);

                // get workbench page for retrieving view information
                try {
                    workbenchPage = workbench.getActiveWorkbenchWindow()
                        .getActivePage();
                } catch (Exception e) {
                    LOG.error(
                        "Workbench page not found. Information about views cannot be displayed.",
                        e);
                    return;
                }

                if (workbenchPage == null) {
                    LOG.warn("Workbench page not found. Information about views cannot be displayed.");
                    return;
                }

                // install listener for view events
                IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
                IPartService partService = null;
                for (int i = 0; i < windows.length; i++) {
                    partService = windows[i].getPartService();
                    partService.addPartListener(viewListener);
                }
            }
        });
    }

    @Override
    public void stop() {
        session.removeActivityProducer(this);
        session.removeActivityConsumer(consumer);

        SWTUtils.runSafeSWTAsync(LOG, new Runnable() {
            @Override
            public void run() {
                // remove dialog, wizard dialog and view listenerslisteners
                if (workbench != null) {
                    workbench.getDisplay().removeFilter(SWT.Activate,
                        shellListener);
                    workbench.getDisplay().removeFilter(SWT.Dispose,
                        shellListener);
                    workbench.getDisplay().removeFilter(SWT.Deactivate,
                        shellListener);

                    IWorkbenchWindow[] windows = workbench
                        .getWorkbenchWindows();
                    IPartService partService = null;
                    for (int i = 0; i < windows.length; i++) {
                        partService = windows[i].getPartService();
                        partService.removePartListener(viewListener);
                    }
                }
                if (wizardDialog != null)
                    wizardDialog
                        .removePageChangedListener(wizardDialogListener);

                // ensure garbage collection
                workbench = null;
                workbenchPage = null;
                wizardDialog = null;
            }
        });
    }

    private IActivityReceiver receiverSessionOverview = new AbstractActivityReceiver() {
        @Override
        public void receive(IDEInteractionActivity activity) {
            User user = activity.getSource();

            if (activity.getStatus() == Status.UNFOCUS) {
                awarenessInformationCollector.updateCloseIDEElement(user,
                    activity.getElement());
                // currently, we don't display close information
                // this needs further experiments
                return;
            }

            awarenessInformationCollector.updateOpenIDEElement(user,
                activity.getTitle(), activity.getElement());
            notifyListeners();
        }
    };

    // TODO refactor this if this remains in saros after the user test
    private IActivityReceiver receiverChat = new AbstractActivityReceiver() {
        @Override
        public void receive(IDEInteractionActivity activity) {

            String message;
            if (activity.getStatus() == Status.FOCUS) {
                if (activity.getElement() == Element.DIALOG) {
                    message = "...opened the dialog '" + activity.getTitle()
                        + "'.";
                } else {
                    message = "...activated the view '" + activity.getTitle()
                        + "'.";
                }
                sessionChat.addHistoryEntry(new ChatElement(message, activity
                    .getSource().getJID(), new Date()));
                ((AbstractChat) sessionChat).notifyJIDMessageReceived(activity
                    .getSource().getJID(), message);
            }
        }
    };

    // TODO refactor this if this remains in saros after the user test
    // FIXME In some cases, the status line is empty
    private IActivityReceiver receiverStatusLine = new AbstractActivityReceiver() {
        @Override
        public void receive(IDEInteractionActivity activity) {

            final User user = activity.getSource();
            final Status currentStatus = activity.getStatus();
            final Element currentElement = activity.getElement();
            final String currentTitle = activity.getTitle();

            SWTUtils.runSafeSWTAsync(LOG, new Runnable() {

                @Override
                public void run() {
                    IActionBars actionBars = null;
                    try {
                        IWorkbenchPart part = workbenchPage.getActivePart();
                        IWorkbenchPartSite site = part.getSite();

                        if (site instanceof IViewSite) {
                            actionBars = ((IViewSite) site).getActionBars();
                        } else if (site instanceof IEditorSite) {
                            actionBars = ((IEditorSite) site).getActionBars();
                        }

                        String message;
                        if (currentStatus == Status.FOCUS) {
                            if (currentElement == Element.DIALOG) {
                                message = user.getNickname()
                                    + " has opened the dialog '" + currentTitle
                                    + "'.";
                            } else {
                                message = user.getNickname()
                                    + " has activated the view '"
                                    + currentTitle + "'.";
                            }
                            if (actionBars != null)
                                actionBars.getStatusLineManager().setMessage(
                                    message);
                        }
                    } catch (Exception e) {
                        // TODO don't handle everything within a try/catch block
                        LOG.debug(
                            "Could not get action bars for the status line", e);
                        return;
                    }
                }
            });
        }
    };

    private void notifyListeners() {
        for (IDEInteractionActivitiesListener listener : listeners) {
            listener.dialogOrViewInteractionChanged();
        }
    }

    /**
     * Adds the given {@link IDEInteractionActivitiesListener} to the
     * {@link IDEInteractionActivitiesManager}.
     * 
     * @param listener
     *            The given {@link IDEInteractionActivitiesListener} to add
     * */
    public void addIDEInteractionActivityListener(
        IDEInteractionActivitiesListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes the given {@link IDEInteractionActivitiesListener} from the
     * {@link IDEInteractionActivitiesManager}.
     * 
     * @param listener
     *            The given {@link IDEInteractionActivitiesListener} to remove
     * */
    public void removeIDEInteractionActivityListener(
        IDEInteractionActivitiesListener listener) {
        listeners.remove(listener);
    }
}