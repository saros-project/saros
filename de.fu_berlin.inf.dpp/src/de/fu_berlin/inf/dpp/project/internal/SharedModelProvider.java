package de.fu_berlin.inf.dpp.project.internal;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.SessionManager;

/**
 * This model provider is responsible for keeping an session observer from
 * modifying the file tree of a shared project on his own.
 * 
 * @author rdjemili
 */
public class SharedModelProvider extends ModelProvider implements ISessionListener {

	private static final String ERROR_TEXT = "Only the driver should edit the resources of this shared project.";

	private static final IStatus ERROR_STATUS = new Status(IStatus.ERROR, "de.fu_berlin.inf.dpp",
		2, ERROR_TEXT, null);

	/** the currently running shared project */
	private ISharedProject sharedProject;

	/**
	 * Validates the resource delta.
	 */
	private class ResourceDeltaVisitor implements IResourceDeltaVisitor {
		private boolean isAllowed = true;

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.resources.IResourceDeltaVisitor
		 */
		public boolean visit(IResourceDelta delta) throws CoreException {
			if (sharedProject == null || sharedProject.isDriver())
				return false;

			IResource resource = delta.getResource();
			if (resource.getProject() == null) // work space root
				return true;

			if (resource.getProject() != sharedProject.getProject())
				return false;

			if (resource instanceof IFile || resource instanceof IFolder) {
				isAllowed = false;
				return false;
			}

			return delta.getKind() > 0;
		}
	}

	@Override
	protected void initialize() {
		SessionManager sm = Saros.getDefault().getSessionManager();

		sm.addSessionListener(this);
		sharedProject = sm.getSharedProject();
	}

	@Override
	public IStatus validateChange(IResourceDelta delta, IProgressMonitor pm) {
		ResourceDeltaVisitor visitor = new ResourceDeltaVisitor();

		try {
			delta.accept(visitor);
		} catch (CoreException e) {
			e.printStackTrace();
		}

		return visitor.isAllowed ? Status.OK_STATUS : ERROR_STATUS;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.project.ISessionListener
	 */
	public void sessionStarted(ISharedProject session) {
		sharedProject = session;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.project.ISessionListener
	 */
	public void sessionEnded(ISharedProject session) {
		sharedProject = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.project.ISessionListener
	 */
	public void invitationReceived(IIncomingInvitationProcess invitation) {
		// ignore
	}
}
