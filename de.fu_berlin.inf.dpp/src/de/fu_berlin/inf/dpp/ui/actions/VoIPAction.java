package de.fu_berlin.inf.dpp.ui.actions;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.communication.audio.AudioServiceManager;
import de.fu_berlin.inf.dpp.net.internal.StreamServiceManager;
import de.fu_berlin.inf.dpp.net.internal.StreamSession;
import de.fu_berlin.inf.dpp.observables.VoIPSessionObservable;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.dialogs.ErrorMessageDialog;
import de.fu_berlin.inf.dpp.ui.dialogs.WarningMessageDialog;
import de.fu_berlin.inf.dpp.ui.views.SarosView;
import de.fu_berlin.inf.dpp.ui.util.selection.SelectionUtils;
import de.fu_berlin.inf.dpp.ui.util.selection.retriever.SelectionRetrieverFactory;
import de.fu_berlin.inf.dpp.util.ValueChangeListener;

/**
 * The {@link VoIPAction} manages the user interface interaction in the
 * {@link SarosView} toolbar.
 * 
 * @author ologa
 * @author bkahlert
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

    protected ISelectionListener selectionListener = new ISelectionListener() {
        public void selectionChanged(IWorkbenchPart part, ISelection selection) {
            updateEnablement();
        }
    };

    @Inject
    protected SarosSessionManager sessionManager;

    protected StreamServiceManager streamServiceManager;

    @Inject
    protected AudioServiceManager audioServiceManager;

    public VoIPAction() {
        super("Start VoIP Session");
        SarosPluginContext.initComponent(this);
        changeButton();
        setId(ACTION_ID);

        obs.add(valueChangeListener);
        SelectionUtils.getSelectionService().addSelectionListener(
            selectionListener);
        updateEnablement();
    }

    protected void updateEnablement() {
        try {
            List<User> participants = SelectionRetrieverFactory
                .getSelectionRetriever(User.class).getSelection();
            setEnabled(sessionManager.getSarosSession() != null
                && participants.size() == 1 && !participants.get(0).isLocal());
        } catch (NullPointerException e) {
            this.setEnabled(false);
        } catch (Exception e) {
            if (!PlatformUI.getWorkbench().isClosing())
                log.error("Unexcepted error while updating enablement", e);
        }
    }

    /**
     * Check if record & playback device is installed and start the invitation
     * of the selected user
     */
    @Override
    public void run() {
        final List<User> participants = SelectionRetrieverFactory
            .getSelectionRetriever(User.class).getSelection();
        if (participants.size() == 1) {
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
                            "Your record device is not properly configured. Please check the VoIP Settings at Window > Preferences > Saros > Communication. The VoIP session will be started, but it could be pointless if the other buddy has also no record device.");
                    audioServiceManager.setRecordDeviceOk(false);
                }

                Job voipCreate = new Job("Creating VoIP Session") {
                    @Override
                    protected IStatus run(IProgressMonitor monitor) {
                        log.info("Trying to invite " + participants.get(0)
                            + " to a new VoIP Session");
                        return audioServiceManager.invite(participants.get(0),
                            SubMonitor.convert(monitor));
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
            updateEnablement();
        } else {
            log.warn("More than one participant selected.");
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
            setImageDescriptor(ImageManager
                .getImageDescriptor("icons/elcl16/stopvoip.png"));
            setToolTipText("Stop VoIP Session...");
            break;
        case STOPPED:
            setImageDescriptor(ImageManager
                .getImageDescriptor("icons/elcl16/startvoip.png"));
            setToolTipText("Start a VoIP Session...");
            break;
        case STOPPING:
            setImageDescriptor(ImageManager
                .getImageDescriptor("icons/elcl16/stoppingvoip.png"));
            setToolTipText("Stop VoIP Session...");
            break;
        default:
            setImageDescriptor(ImageManager
                .getImageDescriptor("icons/elcl16/startvoip.png"));
            setToolTipText("Start a VoIP Session...");
            break;
        }
    }

    public void dispose() {
        SelectionUtils.getSelectionService().removeSelectionListener(
            selectionListener);
        obs.remove(valueChangeListener);
    }

}