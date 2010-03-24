package de.fu_berlin.inf.dpp.ui.actions;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.communication.audio.AudioServiceManager;
import de.fu_berlin.inf.dpp.net.internal.StreamServiceManager;
import de.fu_berlin.inf.dpp.net.internal.StreamSession;
import de.fu_berlin.inf.dpp.observables.VoIPSessionObservable;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.ui.ErrorMessageDialog;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.ui.SessionViewToolBar;
import de.fu_berlin.inf.dpp.ui.WarningMessageDialog;
import de.fu_berlin.inf.dpp.ui.SessionView.SessionViewTableViewer;
import de.fu_berlin.inf.dpp.util.ValueChangeListener;

/**
 * The {@link VoIPAction} manages the user interface interaction in the
 * {@link SessionViewToolBar}
 * 
 * @author ologa
 */
public class VoIPAction extends Action {

    private static final Logger log = Logger.getLogger(VoIPAction.class);

    protected static final String ACTION_ID = VoIPAction.class.getName();

    // TODO move to StreamSessionListener start()
    @Inject
    protected VoIPSessionObservable obs;

    protected ValueChangeListener<StreamSession> valueChangeListener = new ValueChangeListener<StreamSession>() {

        public void setValue(StreamSession newValue) {
            changeButton();
        }
    };

    @Inject
    protected SessionManager sessionManager;

    protected SessionViewTableViewer viewer;

    protected StreamServiceManager streamServiceManager;

    @Inject
    protected AudioServiceManager audioServiceManager;

    protected User selectedUser;

    public VoIPAction(SessionViewTableViewer viewer) {
        super();
        Saros.reinject(this);
        changeButton();
        setId(ACTION_ID);
        setEnabled(false);
        this.viewer = viewer;
        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                ISelection selection = event.getSelection();

                if (selection instanceof StructuredSelection) {
                    StructuredSelection users = (StructuredSelection) selection;
                    if (users.size() == 1)
                        selectedUser = (User) users.getFirstElement();
                    else
                        selectedUser = null;
                    setEnabled(shouldEnable());
                }
            }
        });
        obs.add(valueChangeListener);
    }

    protected boolean shouldEnable() {
        if (selectedUser == null)
            return false;

        ISharedProject project = sessionManager.getSharedProject();
        if (project == null)
            return false;

        return !selectedUser.isLocal();
    }

    /**
     * Check if record & playback device is installed and start the invitation
     * of the selected user
     */
    @Override
    public void run() {
        switch (audioServiceManager.getStatus()) {
        case STOPPED:
            if (!audioServiceManager.isPlaybackConfigured()) {
                ErrorMessageDialog
                    .showErrorMessage("Your playback device is not properly configured. Please check the VoIP Settings at Window > Preferences > Saros > Communication. The VoIP session will NOT be started!");
                audioServiceManager.setPlaybackDeviceOk(false);
                break;
            }

            if (!audioServiceManager.isRecordConfigured()) {
                WarningMessageDialog
                    .showWarningMessage(
                        "No Record device",
                        "Your record device is not properly configured. Please check the VoIP Settings at Window > Preferences > Saros > Communication. The VoIP session will be started, but it could be pointless if the other user has also no record device.");
                audioServiceManager.setRecordDeviceOk(false);
            }

            Job voipCreate = new Job("Creating VoIP Session") {
                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    log.info("Trying to invite " + selectedUser
                        + " to a new VoIP Session");
                    return audioServiceManager.invite(selectedUser, SubMonitor
                        .convert(monitor));
                }
            };
            voipCreate.schedule();
            break;
        case RUNNING:
            audioServiceManager.stopSession();
            break;
        case STOPPING:
            break;
        default:
            log.error("unknown voip session status");
            break;
        }
    }

    /**
     * 
     * Change the start / stop button if required
     * 
     */
    protected void changeButton() {
        switch (audioServiceManager.getStatus()) {
        case RUNNING:
            setImageDescriptor(SarosUI
                .getImageDescriptor("icons/telephone_stop.png"));
            setToolTipText("Stop VoIP Session...");
            break;
        case STOPPED:
            setImageDescriptor(SarosUI
                .getImageDescriptor("icons/telephone.png"));
            setToolTipText("Start a VoIP Session...");
            break;
        case STOPPING:
            setImageDescriptor(SarosUI
                .getImageDescriptor("icons/telephone_stop.png"));
            setToolTipText("Stop VoIP Session...");
            break;
        default:
            setImageDescriptor(SarosUI
                .getImageDescriptor("icons/telephone.png"));
            setToolTipText("Start a VoIP Session...");
            break;
        }
    }

}