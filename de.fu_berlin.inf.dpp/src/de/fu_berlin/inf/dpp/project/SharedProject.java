package de.fu_berlin.inf.dpp.project;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.subscribers.ISubscriberChangeEvent;
import org.eclipse.team.core.subscribers.ISubscriberChangeListener;
import org.eclipse.team.core.subscribers.Subscriber;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.vcs.VCSAdapter;
import de.fu_berlin.inf.dpp.vcs.VCSAdapterFactory;
import de.fu_berlin.inf.dpp.vcs.VCSResourceInformation;

/**
 * A SharedProject stores the state of a project (and its resources) shared in a
 * Saros session.<br>
 * <br>
 * We only want to send out activities if a value changes. To detect changes, we
 * must compare the current value to the one we previously saw. This class is
 * responsible for storing the project specific values we want to track.<br>
 * <br>
 * TODO Add the ability to track information on every file/folder in the
 * project.<br>
 * TODO Rename to SharedProjectState?
 */
public class SharedProject implements ISubscriberChangeListener {
    /**
     * A value of type E with a convenient update method to check if the value
     * was changed.
     */
    static class UpdatableValue<E> {
        private E value;

        UpdatableValue(E value) {
            this.value = value;
        }

        /** Updates the value, and returns true if the value changed. */
        public boolean update(E newValue) {
            if (newValue == null) {
                if (value == null)
                    return false;
                value = null;
                return true;
            }
            if (newValue.equals(value)) {
                return false;
            }
            value = newValue;
            return true;
        }

        public E value() {
            return value;
        }
    }

    private static final Logger log = Logger.getLogger(SharedProject.class);

    protected final ISarosSession sarosSession;

    protected final IProject project;

    /* Stored state values: */
    protected UpdatableValue<Boolean> projectIsOpen;

    protected UpdatableValue<VCSAdapter> vcs;

    protected UpdatableValue<String> vcsUrl;

    protected UpdatableValue<String> vcsRevision;

    protected UpdatableValue<Boolean> isDriver = new UpdatableValue<Boolean>(
        false);

    protected ISharedProjectListener sharedProjectListener = new AbstractSharedProjectListener() {
        @Override
        public void roleChanged(User user) {
            if (isDriver.update(sarosSession.isDriver()))
                initializeVCSInformation();
        }
    };

    public SharedProject(IProject project, ISarosSession sarosSession) {
        assert sarosSession != null;
        assert project != null;

        this.sarosSession = sarosSession;
        this.project = project;

        projectIsOpen = new UpdatableValue<Boolean>(project.isOpen());
        boolean useVersionControl = sarosSession.useVersionControl();
        if (useVersionControl) {
            sarosSession.addListener(sharedProjectListener);
            boolean isDriver = sarosSession.isDriver();
            this.isDriver.update(isDriver);
            if (isDriver)
                initializeVCSInformation();
        }
    }

    protected void initializeVCSInformation() {
        VCSAdapter vcs = VCSAdapterFactory.getAdapter(project);
        this.vcs = new UpdatableValue<VCSAdapter>(vcs);
        if (vcs == null)
            return;
        RepositoryProvider provider = RepositoryProvider.getProvider(project);
        Subscriber subscriber = provider.getSubscriber();
        if (subscriber != null)
            subscriber.addListener(this);
        else
            log.error("Could not add this SharedProject as an ISubscriberChangeListener.");

        VCSResourceInformation info = vcs.getResourceInformation(project);
        vcsUrl = new UpdatableValue<String>(info.repositoryRoot + info.path);
        vcsRevision = new UpdatableValue<String>(info.revision);
    }

    /** Updates the current VCSAdapter, and returns true if the value changed. */
    public boolean updateVcs(VCSAdapter newValue) {
        return vcs.update(newValue);
    }

    /** Updates the current VCS URL, and returns true if the value changed. */
    public boolean updateVcsUrl(String newValue) {
        return vcsUrl.update(newValue);
    }

    /** Updates the current VCS revision, and returns true if the value changed. */
    public boolean updateRevision(String newValue) {
        return vcsRevision.update(newValue);
    }

    /**
     * Updates if the project is currently open, and returns true if the value
     * changed.
     */
    public boolean updateProjectIsOpen(boolean newValue) {
        return projectIsOpen.update(newValue);
    }

    public VCSAdapter getVCSAdapter() {
        return vcs.value();
    }

    /**
     * Returns true if this SharedProject tracks the state of the project.
     */
    public boolean belongsTo(IProject project) {
        return this.project.equals(project);
    }

    public void subscriberResourceChanged(ISubscriberChangeEvent[] deltas) {
        String s = "subscriberResourceChanged:\n";
        for (ISubscriberChangeEvent delta : deltas) {
            int flags = delta.getFlags();
            if (flags == ISubscriberChangeEvent.NO_CHANGE)
                s += "0";
            if ((flags & ISubscriberChangeEvent.SYNC_CHANGED) != 0)
                s += "S";
            if ((flags & ISubscriberChangeEvent.ROOT_ADDED) != 0)
                s += "+";
            if ((flags & ISubscriberChangeEvent.ROOT_REMOVED) != 0)
                s += "-";
            s += " " + delta.getResource().getFullPath().toPortableString()
                + "\n";
        }
        log.trace(s);
    }
}
