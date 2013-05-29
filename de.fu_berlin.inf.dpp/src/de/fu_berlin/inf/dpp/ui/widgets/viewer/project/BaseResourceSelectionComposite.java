package de.fu_berlin.inf.dpp.ui.widgets.viewer.project;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
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
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.project.events.BaseResourceSelectionListener;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.project.events.ResourceSelectionChangedEvent;
import de.fu_berlin.inf.dpp.util.ArrayUtils;

/**
 * This {@link Composite} extends {@link ResourceDisplayComposite} and allows to
 * check (via check boxes) {@link IProject}s, {@link IFolder}s and {@link IFile}
 * s.
 * 
 * This component remembers all selections and has methods to undo/redo the
 * selection in the viewer.
 * 
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
 * @author waldmann
 * 
 */
public abstract class BaseResourceSelectionComposite extends
    ResourceDisplayComposite {

    private static final String SAROS_RESOURCE_SELECTION_PRESET_NAMES = "Saros.resource_selection.preset_names";
    protected List<BaseResourceSelectionListener> resourceSelectionListeners = new ArrayList<BaseResourceSelectionListener>();
    protected CheckboxTreeViewer checkboxTreeViewer = (CheckboxTreeViewer) this.viewer;
    Logger log = Logger.getLogger(this.getClass());

    /*
     * Stacks used for saving previous selections in the tree view, enabling
     * undo/redo functionality
     */
    Stack<Object[]> lastChecked = new Stack<Object[]>();
    Stack<Object[]> lastGrayed = new Stack<Object[]>();
    Stack<Object[]> prevChecked = new Stack<Object[]>();
    Stack<Object[]> prevGrayed = new Stack<Object[]>();

    protected final static String SERIALIZATION_SEPARATOR = "**#**";
    protected final static String SERIALIZATION_SEPARATOR_REGEX = "\\*\\*#\\*\\*";

    protected ICheckStateListener checkStateListener = new ICheckStateListener() {
        @Override
        public void checkStateChanged(CheckStateChangedEvent event) {
            final Object element = event.getElement();
            boolean isChecked = event.getChecked();

            IResource resource = (IResource) element;

            handleCheckStateChanged(resource, isChecked);
            notifyResourceSelectionChanged(resource, isChecked);

            rememberSelection();
        }
    };

    /**
     * Handles the tree selection behavior. That means:
     * <ul>
     * <li>If folder or project root is selected, check subtree
     * <li>If one (or more) element(s) of project selected, select
     * <b>.classpath</b> and <b>.project</b> file as well (if exists)
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
        } else if (resource instanceof IFolder) {
            this.checkboxTreeViewer.setChecked(resource, checked);
            this.checkboxTreeViewer.setSubtreeChecked(resource, checked);
            setParentsCheckedORGrayed(resource);
        } else if (resource instanceof IProject) {
            this.checkboxTreeViewer.setGrayChecked(resource, false);
            this.checkboxTreeViewer.setSubtreeChecked(resource, checked);
        }
    }

    /**
     * Remembers the current selection for undo/redo functionality.
     * 
     * Any click activity while "redo" is possible (which means something was
     * undone) will clear the whole redo-stack.
     * 
     */
    public void rememberSelection() {
        if (isRedoEnabled()) {
            prevChecked.clear();
            prevGrayed.clear();
        }

        lastGrayed.push(((CheckboxTreeViewer) viewer).getGrayedElements());
        lastChecked.push(((CheckboxTreeViewer) viewer).getCheckedElements());
        log.debug("Remembered checked/grayed items: "
            + lastChecked.lastElement().length + "/"
            + lastGrayed.lastElement().length + " Saved " + lastChecked.size()
            + " snapshots");

        // Need to update the controls (if there are any)
        updateRedoUndoControls();
    }

    /**
     * Returns a list of all names of the saved selection presets
     */
    public List<String> getSavedSelectionNames() {
        List<String> namesList = new ArrayList<String>();

        String namesString = saros.getPreferenceStore().getString(
            SAROS_RESOURCE_SELECTION_PRESET_NAMES);

        String[] names = namesString.split(SERIALIZATION_SEPARATOR_REGEX);
        for (String name : names) {
            if (!name.isEmpty()) {
                namesList.add(name);
            }
        }
        log.debug("I got the names from the list: " + namesList.size());
        return namesList;
    }

    /**
     * Adds the given name to the list of saved selection preset names
     * 
     * @param name
     */
    protected void rememberSelectionName(String name) {
        log.debug("Remembering new selection name: " + name);
        /*
         * Check if name already exists in list (it's more a set than a list),
         * do nothing in that case..
         */
        if (getSavedSelectionNames().contains(name)) {
            log.debug("Not adding name to list because already exists: " + name);
            return;
        }

        StringBuilder namesString = new StringBuilder(saros
            .getPreferenceStore().getString(
                SAROS_RESOURCE_SELECTION_PRESET_NAMES));

        namesString.append(name).append(SERIALIZATION_SEPARATOR);

        log.debug("Storing namesString: " + namesString.toString());
        saros.getPreferenceStore().setValue(
            SAROS_RESOURCE_SELECTION_PRESET_NAMES, namesString.toString());
    }

    /**
     * Adds the given name to the list of saved selection preset names
     * 
     * @param name
     */
    protected void removeStoredSelection(String name) {
        log.debug("Removing stored selection with name: " + name);
        List<String> savedSelectionNames = getSavedSelectionNames();
        if (!savedSelectionNames.contains(name)) {
            log.debug("cannot remove, name not present in list: " + name);
            return;
        }
        // remove
        savedSelectionNames.remove(name);

        // rebuild string
        StringBuilder namesString = new StringBuilder();
        for (String theName : savedSelectionNames) {
            namesString.append(theName).append(SERIALIZATION_SEPARATOR);
        }
        // save string
        log.debug("Storing namesString: " + namesString.toString());
        saros.getPreferenceStore().setValue(
            SAROS_RESOURCE_SELECTION_PRESET_NAMES, namesString.toString());

        // delete the preference
        String checkedSelectionName = "Saros.resource_selection.checked."
            + name;
        saros.getPreferenceStore().setValue(checkedSelectionName, "");
        // TODO: find out if there is some sort of delete method..
    }

    /**
     * Save the current selection as a selection preset with the given name
     * using the preferences api, serializing all URIs of the selected
     * iResources.
     * 
     * Overwrites any existing selections with the same name without warning.
     * Use hasSelectionWithName(String name) to find out if the user would
     * overwrite an existing selection preset!
     * 
     * Adds the name to a set of saved preset names in the preferences too. Use
     * getSavedSelectionNames() to retrieve a list of all names
     * 
     * @param name
     */
    public void saveSelectionWithName(String name) {
        /*
         * Load all checked elements, remove the ones that are grayed and store
         * the rest as selection preset. SetSelection() automatically handles
         * setting the grayed status AND fires the changelisteners which is
         * important for the wizards next button to activate
         */
        Object[] checked = ((CheckboxTreeViewer) viewer).getCheckedElements();

        StringBuilder checkedString = new StringBuilder();

        for (Object resource : checked) {
            if (((CheckboxTreeViewer) viewer).getGrayed(resource) == false) {
                checkedString.append(((IResource) resource).getFullPath())
                    .append(SERIALIZATION_SEPARATOR);
            }
        }

        String checkedSelectionName = "Saros.resource_selection.checked."
            + name;

        log.debug("Storing checked elements (not grayed): "
            + checkedString.toString());

        saros.getPreferenceStore().setValue(checkedSelectionName,
            checkedString.toString());

        rememberSelectionName(name);
    }

    /**
     * Removes any existing selection and applies the selection preset that was
     * saved with the given name (if it exists). Returns false If no selection
     * was made (e.g. there is no such preset) or true if the selection was
     * applied..
     * 
     * @param name
     *            The name of the preset that should be used
     */
    public boolean restoreSelectionWithName(String name) {
        String checkedSelectionName = "Saros.resource_selection.checked."
            + name;

        String checked = saros.getPreferenceStore().getString(
            checkedSelectionName);

        if (checked.isEmpty()) {
            log.debug("checked string is empty");
            /*
             * No empty selections can be saved, so this means that there is no
             * selection preset with the given name
             */
            return false;
        }

        List<IResource> checkedList = new ArrayList<IResource>();

        String[] uris;

        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

        uris = checked.split(SERIALIZATION_SEPARATOR_REGEX);
        for (String uri : uris) {
            IResource resource = root.findMember(uri);
            if (resource != null) {
                checkedList.add(resource);
            } else {
                log.error("Did not find resource with uri in workspace root to apply selection: "
                    + uri);
            }
        }

        setSelectedResources(checkedList);

        return true;
    }

    /**
     * This needs to take care of enabling redo/undo controls
     */
    public abstract void updateRedoUndoControls();

    /**
     * Undo the last user action (this enables the redo button if not already
     * enabled).
     */
    protected void undoSelection() {
        if (!lastChecked.isEmpty()) {
            /*
             * This holds the CURRENT selection (rememberSelection is called
             * after each selection event in the tree, so when the user triggers
             * an undo, the first element in the lastXYZ stacks represent the
             * selection which the user wants to undo) which we need to push
             * onto the redo-stacks.
             */
            Object[] checked = lastChecked.pop();
            Object[] grayed = lastGrayed.pop();

            prevChecked.push(checked);
            prevGrayed.push(grayed);
        }
        /*
         * Do not combine the two ifs! lastChecked can be empty now because of
         * the modifications...
         */
        if (!lastChecked.isEmpty()) {
            /*
             * Not using the checked/grayed variable as they contain the CURRENT
             * selection (which we want to undo). Using them would not undo
             * anything.
             */
            ((CheckboxTreeViewer) viewer).setGrayedElements(lastGrayed
                .lastElement());
            ((CheckboxTreeViewer) viewer).setCheckedElements(lastChecked
                .lastElement());

        } else {
            /*
             * No previous selection available, so unset all selections (set to
             * initial state)
             */
            ((CheckboxTreeViewer) viewer).setCheckedElements(new Object[0]);
            ((CheckboxTreeViewer) viewer).setGrayedElements(new Object[0]);
        }

        // Need to update the controls (if there are any)
        updateRedoUndoControls();
    }

    /**
     * Restores the last selection if there is one because of a previous undo
     * action. Does nothing if there was no undo operation before the call to
     * redoSelection()
     */
    protected void redoSelection() {
        if (!prevChecked.isEmpty()) {
            Object[] checkedElements = prevChecked.pop();
            Object[] grayedElements = prevGrayed.pop();

            lastChecked.push(checkedElements);
            lastGrayed.push(grayedElements);

            ((CheckboxTreeViewer) viewer).setGrayedElements(grayedElements);
            ((CheckboxTreeViewer) viewer).setCheckedElements(checkedElements);

            // Disable redo button if no redo possible anymore

        } else {
            log.debug("Cannot redo, no more snapshots!");
        }
        // Need to update the controls (if there are any)
        updateRedoUndoControls();
    }

    /**
     * @return true if redoing the last "undone" selection is possible..
     */
    protected boolean isRedoEnabled() {
        return !prevChecked.isEmpty();
    }

    /**
     * @return true if redoing the last "undone" selection is possible..
     */
    protected boolean isUndoEnabled() {
        return !lastChecked.isEmpty();
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
                if (this.checkboxTreeViewer.getChecked(childs[i])) {
                    checkedChildren++;
                }
                if (this.checkboxTreeViewer.getGrayed(childs[i])) {
                    grayedChildren++;
                }
            }
            checkedChildren = checkedChildren - grayedChildren;
            if (childs.length == checkedChildren) {
                checkboxTreeViewer.setGrayed(parentResource, false);
                checkboxTreeViewer.setChecked(parentResource, true);
            } else if ((grayedChildren > 0) || (checkedChildren > 0)) {
                checkboxTreeViewer.setChecked(parentResource, true);
                checkboxTreeViewer.setGrayed(parentResource, true);
            } else {
                checkboxTreeViewer.setGrayChecked(parentResource, false);
            }
            setParentsCheckedORGrayed(parentResource);
        }
        return;
    }

    public BaseResourceSelectionComposite(Composite parent, int style) {
        super(parent, style | SWT.CHECK);

        ((CheckboxTreeViewer) this.viewer)
            .addCheckStateListener(checkStateListener);

        addDisposeListener(new DisposeListener() {
            @Override
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
    protected ISarosSessionManager sessionManager;

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

        List<IResource> checkedResourcesBeforeUpdate = ArrayUtils
            .getAdaptableObjects(checkboxTreeViewer.getCheckedElements(),
                IResource.class, Platform.getAdapterManager());

        /*
         * Does not fire events...
         */
        checkboxTreeViewer.setCheckedElements(resources.toArray());

        for (int i = 0; i < resources.size(); i++)
            handleCheckStateChanged(resources.get(i), true);

        /*
         * ... therefore we have to fire them.
         */

        List<IResource> checkedResourcesAfterUpdate = ArrayUtils
            .getAdaptableObjects(checkboxTreeViewer.getCheckedElements(),
                IResource.class, Platform.getAdapterManager());

        Set<IResource> checkedResources = new HashSet<IResource>(
            checkedResourcesAfterUpdate);

        checkedResources.removeAll(checkedResourcesBeforeUpdate);

        Set<IResource> uncheckedResources = new HashSet<IResource>(
            checkedResourcesBeforeUpdate);

        uncheckedResources.removeAll(checkedResourcesAfterUpdate);

        for (IResource resource : checkedResources)
            notifyResourceSelectionChanged(resource, true);

        for (IResource resource : uncheckedResources)
            notifyResourceSelectionChanged(resource, false);
    }

    /**
     * Returns the currently selected {@link IResource}s. If you only want to
     * know if at least one resource is selected, use hasSelectedResources()
     * 
     * @return
     */
    public List<IResource> getSelectedResources() {
        CheckboxTreeViewer checkboxTreeViewer = ((CheckboxTreeViewer) this.viewer);

        List<IResource> resources = ArrayUtils.getAdaptableObjects(
            checkboxTreeViewer.getCheckedElements(), IResource.class,
            Platform.getAdapterManager());
        resources.removeAll(ArrayUtils.getAdaptableObjects(
            checkboxTreeViewer.getGrayedElements(), IResource.class,
            Platform.getAdapterManager()));
        return resources;
    }

    /**
     * Returns true if at least one resource is selected
     */
    public boolean hasSelectedResources() {
        CheckboxTreeViewer checkboxTreeViewer = ((CheckboxTreeViewer) this.viewer);
        return checkboxTreeViewer.getCheckedElements().length > 0;
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
