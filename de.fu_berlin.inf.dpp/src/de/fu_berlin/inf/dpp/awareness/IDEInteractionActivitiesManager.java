package de.fu_berlin.inf.dpp.awareness;

import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.ui.IWorkbenchPart;
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
 * 
 * The following state machine represents the behavior of how the action
 * awareness information are retrieved:
 * 
 * <pre>
 * <code>
 *                                 [#openDialog == 1] closeDialog                [#openDialog > 1] closeDialog
 *                         +------------------------------------------------+   +------------+                   
 *                         |                                                |   |            |                   
 *                         v                                                |   |            |                   
 *                                                                          |   |            |                   
 *            +-------------------+                                 +-------+---+-----+ <----+                   
 *            |                   |         openDialog              |                 |                          
 *  start +-> |    active view    | +---------------------------->  |   open dialog   |                          
 *            |                   |                                 |                 | <+                       
 *   +------+ +-------------------+                                 +-------+---+-----+  |                       
 *   |                                                                      |   |        |                       
 *   |            ^      ^                                                  |   |        |                       
 *   |            |      |                                                  |   +--------+                       
 *   +---+--------+      +---------------------+----------------------------+         openDialog                 
 *   activateView                        deactivateDialog                                                        
 *                
 * </code>
 * </pre>
 * */
public class IDEInteractionActivitiesManager extends AbstractActivityProducer
    implements Startable {

    private static final Logger LOG = Logger
        .getLogger(IDEInteractionActivitiesManager.class);

    private final ISarosSession session;
    private final AwarenessInformationCollector awarenessInformationCollector;
    private IWorkbench workbench;
    private IWorkbenchPage workbenchPage;
    private String lastShellTitle;

    /*
     * Since it can happen that there can are two or more modal wizard dialogs
     * open, an {@link IPageChangedListener} must be added to the correct {@link
     * WizardDialog}. Therefore this list stores all open modal and non-modal
     * {@link WizardDialog}'s, so that a listener can be added to the correct
     * dialog.
     */
    private final List<WizardDialog> wizardDialogList = new ArrayList<WizardDialog>();

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

        boolean lastShellWasDeactivated = false;

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
                WizardDialog wizardDialog = (WizardDialog) shell.getData();
                wizardDialog.addPageChangedListener(wizardDialogListener);
                if (!wizardDialogList.contains(wizardDialog)) {
                    wizardDialogList.add(wizardDialog);
                }
            }

            String currentShellTitle = shell.getText();

            // if a view in a tab folder is disposed, SWT.Dispose is fired, too,
            // with an empty viewpart title
            if (currentShellTitle.isEmpty())
                return;

            if (event.type == SWT.Activate) {

                // a window was opened or activated
                fireActivity(new IDEInteractionActivity(session.getLocalUser(),
                    currentShellTitle, Element.DIALOG));
                lastShellTitle = currentShellTitle;
                lastShellWasDeactivated = false;

            } else if (event.type == SWT.Deactivate) {

                if (SWTUtils.getDisplay().getActiveShell() == null) {
                    return;
                }

                sendLandingView(shell);
                lastShellWasDeactivated = true;

            } else if (event.type == SWT.Dispose) {
                /*
                 * Note: Sometimes, dialogs are activated which cannot be seen,
                 * e.g. the dialog 'Progress information' during a session
                 * start. This was the reason, why e.g. 'In view Saros' was
                 * printed twice at the beginning of a session. To avoid this,
                 * we check, if a dialog which is going to be closed is not a
                 * known dialog. If so we do not send the event, to avoid
                 * sending it twice.
                 */
                if (lastShellTitle != null
                    && !lastShellTitle.equals(currentShellTitle)) {
                    return;
                }

                // if a wizard dialog is closed, this event type is also fired,
                // because the shell remains the same, thus remove the listener
                if (shell.getData() instanceof WizardDialog) {
                    int position = wizardDialogList.indexOf(shell.getData());
                    if (position != -1) {
                        WizardDialog wizardDialog = wizardDialogList
                            .get(position);
                        wizardDialog
                            .removePageChangedListener(wizardDialogListener);
                        wizardDialogList.remove(wizardDialog);
                    }
                }

                /*
                 * If the dialog's parent is the main window, closing this
                 * navigates the user to the last active view.
                 * 
                 * Note: In a case of a modal window following can happen: A
                 * modal window like the Find/Replace, is open. Then the user
                 * clicks into a view, like the editor, which is now displayed
                 * as the active view. Then the user closes the Find/Replace
                 * dialog without activating it (just clicking the x). In such a
                 * case, the landing view would be computed and displayed again.
                 * To avoid this, it is remembered wether the last shell was
                 * deactivated to avoid displaying the active view twice.
                 */
                if (!lastShellWasDeactivated) {
                    sendLandingView(shell);
                }
                lastShellWasDeactivated = false;
            }
        }
    };

    private void sendLandingView(Shell shell) {
        if (shell.getParent().getParent() == null && workbench != null
            && workbenchPage != null && workbenchPage.getActivePart() != null) {

            IWorkbenchPart part = workbenchPage.getActivePart();
            if (part instanceof IEditorPart) {
                // TODO find better way than hardcoding this
                fireActivity(new IDEInteractionActivity(session.getLocalUser(),
                    "Editor", Element.VIEW));
            } else if (part instanceof IViewPart) {
                fireActivity(new IDEInteractionActivity(session.getLocalUser(),
                    part.getTitle(), Element.VIEW));
            }
        }
    }

    /**
     * Listener for page changed events in wizard dialogs, which also change the
     * title of the dialogs
     * */
    private final IPageChangedListener wizardDialogListener = new IPageChangedListener() {

        @Override
        public void pageChanged(PageChangedEvent event) {
            WizardDialog wizardDialog = (WizardDialog) event.getSource();
            Shell shell = wizardDialog.getCurrentPage().getControl().getShell();
            Shell currentActiveShell = SWTUtils.getDisplay().getActiveShell();
            if (currentActiveShell != null && shell.equals(currentActiveShell)) {
                // wizard dialog has focus / is active
                String newTitle = shell.getText();
                fireActivity(new IDEInteractionActivity(session.getLocalUser(),
                    newTitle, Element.DIALOG));
                lastShellTitle = newTitle;
            }
        }
    };

    /**
     * Listener for activating or deactivating views
     * */
    private final IPartListener2 viewListener = new IPartListener2() {

        boolean lastViewWasEditor = false;

        @Override
        public void partActivated(IWorkbenchPartReference partReference) {
            if (partReference.getPart(false) instanceof IEditorPart) {
                // if editor is activated don't show the file's name
                // TODO find better way than hardcoding this

                if (!lastViewWasEditor)
                    fireActivity(new IDEInteractionActivity(
                        session.getLocalUser(), "Editor", Element.VIEW));
                lastViewWasEditor = true;

            } else if (partReference.getPart(false) instanceof IViewPart) {

                fireActivity(new IDEInteractionActivity(session.getLocalUser(),
                    partReference.getPartName(), Element.VIEW));
                lastViewWasEditor = false;
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

                for (WizardDialog dialog : wizardDialogList)
                    dialog.removePageChangedListener(wizardDialogListener);

                wizardDialogList.clear();

                // ensure garbage collection
                workbench = null;
                workbenchPage = null;
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