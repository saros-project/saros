package de.fu_berlin.inf.dpp.ui.widgets.viewer.project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.project.events.BaseResourceSelectionListener;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.project.events.ResourceSelectionChangedEvent;
import de.fu_berlin.inf.dpp.util.ArrayUtils;

/**
 * This {@link Composite} extends {@link ProjectDisplayComposite} and allows to
 * check (via check boxes) {@link IProject}s, {@link IFolder}s and {@link IFile}
 * s.
 * <p>
 * This composite does <strong>NOT</strong> handle setting the layout.
 * 
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>NONE and those supported by {@link StructuredViewer}</dd>
 * <dd>SWT.CHECK is used by default</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * 
 * @author bkahlert
 * @author kheld
 * 
 */
public class BaseResourceSelectionComposite extends ResourceDisplayComposite {

    protected List<BaseResourceSelectionListener> resourceSelectionListeners = new ArrayList<BaseResourceSelectionListener>();
    protected CheckboxTreeViewer checkboxTreeViewer = (CheckboxTreeViewer) this.viewer;
    Logger log = Logger.getLogger(this.getClass());

    protected ICheckStateListener checkStateListener = new ICheckStateListener() {

        public void checkStateChanged(CheckStateChangedEvent event) {
            final Object element = event.getElement();
            boolean isChecked = event.getChecked();
            IResource resource = (IResource) element;
            handleCheckStateChanged(resource, isChecked);
            notifyResourceSelectionChanged(resource, isChecked);
        }
    };

    /**
     * Handles the tree selection behavior. That means:
     * <ul>
     * <li>If folder or project root is selected, check subtree
     * <li>If one (or more) element(s) of project selected, select
     * <b>.calsspath</b> and <b>.project</b> file aswell (if exists)
     * <li>If selected resource is not project root, handle gray status of
     * checkboxes in the higher tree levels
     * </ul>
     * 
     * @param resource
     * @param checked
     */
    protected void handleCheckStateChanged(IResource resource, boolean checked) {
        if (resource instanceof IFile) {
            this.checkboxTreeViewer.setChecked(resource, checked);
            setParentsCheckedORGrayed(resource);
        } else if (resource instanceof IFolder || resource instanceof IProject) {
            this.checkboxTreeViewer.setGrayChecked(resource, false);
            this.checkboxTreeViewer.setChecked(resource, checked);
            setChildrenUngrayed(resource);
            this.checkboxTreeViewer.setSubtreeChecked(resource, checked);
            setParentsCheckedORGrayed(resource);
        }
    }

    /**
     * Traverses the whole subtree of selected resource to reset their gray
     * status. This is necessary to do before elements can get checked.
     * 
     * @param resource
     */
    private void setChildrenUngrayed(IResource resource) {
        if (resource instanceof IProject) {
            if (!((IProject) resource).isOpen())
                return;
        }
        if ((resource instanceof IProject) || (resource instanceof IFolder)) {
            try {
                for (Object elemt : ((IContainer) resource).members()) {
                    this.checkboxTreeViewer.setGrayChecked(elemt, false);
                    setChildrenUngrayed((IResource) elemt);
                }
            } catch (CoreException e) {
                log.debug("Can't determine tree members.", e);
            }
        }
    }

    /**
     * Traverses the upper levels of tree element to:
     * <ul>
     * <li>Check them if all resources are checked
     * <li>Gray them if some resources are checked
     * <li>Ungray or uncheck them if no resources are checked.
     * </ul>
     * 
     * @param resource
     */
    protected void setParentsCheckedORGrayed(IResource resource) {
        IContainer parentResource = resource.getParent();

        if (parentResource != resource.getWorkspace().getRoot()) {
            int checkedChildren = 0;
            int grayedChildren = 0;
            Object[] childs = ((WorkbenchContentProvider) checkboxTreeViewer
                .getContentProvider()).getChildren(parentResource);
            for (int i = 0; i < childs.length; i++) {
                if (this.checkboxTreeViewer.getChecked(childs[i]))
                    checkedChildren++;
                if (this.checkboxTreeViewer.getGrayed(childs[i]))
                    grayedChildren++;
            }
            checkedChildren = checkedChildren - grayedChildren;
            if (childs.length == checkedChildren) {
                checkboxTreeViewer.setGrayed(parentResource, false);
                checkboxTreeViewer.setChecked(parentResource, true);
                setParentsCheckedORGrayed(parentResource);
            } else if ((grayedChildren > 0) || (checkedChildren > 0)) {
                checkboxTreeViewer.setChecked(parentResource, true);
                checkboxTreeViewer.setGrayed(parentResource, true);
                setParentsCheckedORGrayed(parentResource);
            } else {
                checkboxTreeViewer.setGrayChecked(parentResource, false);
                setParentsCheckedORGrayed(parentResource);
            }
        }
        return;
    }

    public BaseResourceSelectionComposite(Composite parent, int style) {
        super(parent, style | SWT.CHECK);

        ((CheckboxTreeViewer) this.viewer)
            .addCheckStateListener(checkStateListener);

        addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                if (viewer != null)
                    ((CheckboxTreeViewer) viewer)
                        .removeCheckStateListener(checkStateListener);
            }
        });

        SarosPluginContext.initComponent(this);
        viewer.addFilter(sharedProjectsFilter);
    }

    @Inject
    protected SarosSessionManager sessionManager;

    /**
     * Filter for already shared resources.
     */
    protected ViewerFilter sharedProjectsFilter = new ViewerFilter() {
        @Override
        public boolean select(Viewer viewer, Object parentElement,
            Object element) {
            ISarosSession sarosSession = sessionManager.getSarosSession();
            if (sarosSession != null) {
                if (element instanceof IFile || element instanceof IFolder) {
                    return !sarosSession.isShared((IResource) element);
                } else if (element instanceof IProject) {
                    return !(sarosSession
                        .isCompletelyShared((IProject) element));
                }
            }
            return true;
        }
    };

    @Override
    public void createViewer(int style) {
        this.viewer = new CheckboxTreeViewer(new Tree(this, style));
    }

    /**
     * Sets the currently selected {@link IResource}s.
     * 
     * @param resources
     */
    public void setSelectedResources(List<IResource> resources) {
        CheckboxTreeViewer checkboxTreeViewer = (CheckboxTreeViewer) this.viewer;
        IStructuredContentProvider structuredContentProvider = (IStructuredContentProvider) checkboxTreeViewer
            .getContentProvider();

        Object[] allElements = structuredContentProvider
            .getElements(checkboxTreeViewer.getInput());
        Object[] checkedElements = checkboxTreeViewer.getCheckedElements();

        List<IResource> allResources = ArrayUtils.getAdaptableObjects(
            allElements, IResource.class);
        List<IResource> checkedResources = ArrayUtils.getAdaptableObjects(
            checkedElements, IResource.class);

        Map<IResource, Boolean> checkStatesChanges = calculateCheckStateDiff(
            allResources, checkedResources, resources);

        /*
         * Does not fire events...
         */
        checkboxTreeViewer.setCheckedElements(resources.toArray());

        for (int i = 0; i < resources.size(); i++) {
            handleCheckStateChanged(resources.get(i), true);
        }

        /*
         * ... therefore we have to fire them.
         */
        for (Map.Entry<IResource, Boolean> entry : checkStatesChanges
            .entrySet())
            notifyResourceSelectionChanged(entry.getKey(), entry.getValue());
    }

    /**
     * Calculates from a given set of {@link IResource}s which {@link IResource}
     * s change their check state.
     * 
     * @param allResources
     * @param checkedResources
     *            {@link IResource}s which are already checked
     * @param resourcesToCheck
     *            {@link IResource}s which have to be exclusively checked
     * @return {@link Map} of {@link IResource} that must change their check
     *         state
     */
    protected static Map<IResource, Boolean> calculateCheckStateDiff(
        List<IResource> allResources, List<IResource> checkedResources,
        List<IResource> resourcesToCheck) {

        Map<IResource, Boolean> checkStatesChanges = new HashMap<IResource, Boolean>();
        for (IResource resource : allResources) {
            if (resourcesToCheck.contains(resource)
                && !checkedResources.contains(resource)) {
                checkStatesChanges.put(resource, true);
            } else if (!resourcesToCheck.contains(resource)
                && checkedResources.contains(resource)) {
                checkStatesChanges.put(resource, false);
            }
        }

        return checkStatesChanges;
    }

    /**
     * Returns the currently selected {@link IResource}s.
     * 
     * @return
     */
    public List<IResource> getSelectedResources() {
        CheckboxTreeViewer checkboxTreeViewer = ((CheckboxTreeViewer) this.viewer);

        List<IResource> resources = ArrayUtils.getAdaptableObjects(
            checkboxTreeViewer.getCheckedElements(), IResource.class);
        resources.removeAll(ArrayUtils.getAdaptableObjects(
            checkboxTreeViewer.getGrayedElements(), IResource.class));
        return resources;
    }

    /**
     * Adds a {@link BaseResourceSelectionListener}
     * 
     * @param resourceSelectionListener
     */
    public void addResourceSelectionListener(
        BaseResourceSelectionListener resourceSelectionListener) {
        this.resourceSelectionListeners.add(resourceSelectionListener);
    }

    /**
     * Removes a {@link BaseResourceSelectionListener}
     * 
     * @param resourceSelectionListener
     */
    public void removeResourceSelectionListener(
        BaseResourceSelectionListener resourceSelectionListener) {
        this.resourceSelectionListeners.remove(resourceSelectionListener);
    }

    /**
     * Notify all {@link BaseResourceSelectionListener}s about a changed
     * selection.
     * 
     * @param resource
     *            {@link IResource} who's selection changed
     * 
     * @param isSelected
     *            new selection state
     */
    public void notifyResourceSelectionChanged(IResource resource,
        boolean isSelected) {
        ResourceSelectionChangedEvent event = new ResourceSelectionChangedEvent(
            resource, isSelected);
        for (BaseResourceSelectionListener resourceSelectionListener : this.resourceSelectionListeners) {
            resourceSelectionListener.resourceSelectionChanged(event);
        }
    }

    @Override
    public void setLayout(Layout layout) {
        // ignore
    }
}
