package de.fu_berlin.inf.dpp.awareness;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.picocontainer.Startable;

import de.fu_berlin.inf.dpp.activities.AbstractActivityReceiver;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.IActivityReceiver;
import de.fu_berlin.inf.dpp.activities.IDEInteractionActivity;
import de.fu_berlin.inf.dpp.activities.IDEInteractionActivity.Element;
import de.fu_berlin.inf.dpp.session.AbstractActivityConsumer;
import de.fu_berlin.inf.dpp.session.AbstractActivityProducer;
import de.fu_berlin.inf.dpp.session.IActivityConsumer;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;

/**
 * This manager is responsible for distributing information about dialogs and
 * views being opened or focused by all session participants.
 * 
 * It listens on the workbench state for dialogs and views and stores the
 * collected information in the {@link AwarenessInformationCollector}, when the
 * current user opened a new dialog or focused a view. Activities are sent
 * around to all session participants, if a session is active.
 * */
public class IDEInteractionActivitiesManager extends AbstractActivityProducer
    implements Startable {

    private static final Logger LOG = Logger
        .getLogger(IDEInteractionActivitiesManager.class);

    private final ISarosSession session;
    private final AwarenessInformationCollector awarenessInformationCollector;
    private IWorkbench workbench;
    private IWorkbenchPage workbenchPage;
    private WizardDialog wizardDialog;

    private final IActivityConsumer consumer = new AbstractActivityConsumer() {
        @Override
        public void exec(IActivity activity) {
            activity.dispatch(receiver);
        }
    };

    /**
     * Listener for opening or closing shells, which normally are dialogs
     * */
    private final Listener shellListener = new Listener() {

        @Override
        public void handleEvent(Event event) {

            if (event == null || event.widget == null) {
                // in some situations, this may happen but can be ignored
                return;
            }

            if (event.widget.getClass() != Shell.class)
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
                    currentDialogTitle, Element.DIALOG));

            } else if (event.type == SWT.Dispose) {

                // if a wizard dialog is closed, this event type is also fired,
                // because the shell remains the same, thus remove the listener
                if (shell.getData() instanceof WizardDialog
                    && wizardDialog != null) {
                    wizardDialog
                        .removePageChangedListener(wizardDialogListener);
                    wizardDialog = null;
                }

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
                                .getLocalUser(), landingView, Element.VIEW));
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
            // FIXME use
            // ((IWizardPage)event.getSource()).getControl().getShell();
            // and check if the shell has the focus / is active

            String newTitle = wizardDialog.getShell().getText();
            fireActivity(new IDEInteractionActivity(session.getLocalUser(),
                newTitle, Element.DIALOG));
        }
    };

    /**
     * Listener for activating or deactivating views
     * */
    private final IPartListener2 viewListener = new IPartListener2() {

        @Override
        public void partActivated(IWorkbenchPartReference partReference) {
            if (partReference.getPart(false) instanceof IEditorPart) {
                // if editor is activated don't show the file's name
                // TODO find better way than hardcoding this
                fireActivity(new IDEInteractionActivity(session.getLocalUser(),
                    "Editor", Element.VIEW));
            } else if (partReference.getPart(false) instanceof IViewPart) {
                fireActivity(new IDEInteractionActivity(session.getLocalUser(),
                    partReference.getTitle(), Element.VIEW));
            }
        }

        @Override
        public void partDeactivated(IWorkbenchPartReference partReference) {
            // do nothing
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
     * Constructs a new {@link IDEInteractionActivitiesManager}.
     * 
     * @param session
     *            The session, in which the action awareness information are
     *            distributed.
     * @param awarenessInformationCollector
     *            This class is used to retrieve the updates from the received
     *            information about opened/closed dialogs or
     *            activated/deactivated views.
     * */
    public IDEInteractionActivitiesManager(ISarosSession session,
        AwarenessInformationCollector awarenessInformationCollector) {
        this.awarenessInformationCollector = awarenessInformationCollector;
        this.session = session;
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

    private IActivityReceiver receiver = new AbstractActivityReceiver() {
        @Override
        public void receive(IDEInteractionActivity activity) {
            awarenessInformationCollector.addOpenIDEElement(
                activity.getSource(), activity.getTitle(),
                activity.getElement());
        }
    };
}