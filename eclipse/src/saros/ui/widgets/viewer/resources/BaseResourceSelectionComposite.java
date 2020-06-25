package saros.ui.widgets.viewer.resources;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import saros.Saros;
import saros.SarosPluginContext;
import saros.filesystem.IReferencePoint;
import saros.filesystem.ResourceConverter;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.ui.util.LayoutUtils;
import saros.ui.widgets.viewer.ViewerComposite;
import saros.ui.widgets.viewer.resources.events.BaseResourceSelectionListener;
import saros.ui.widgets.viewer.resources.events.ResourceSelectionChangedEvent;

/**
 * Base UI allowing the user to select (complete) resource trees in the current workspace.
 *
 * <p>This component remembers all selections and has methods to undo/redo the selection in the
 * viewer.
 *
 * <p>This composite does <strong>NOT</strong> handle setting the layout.
 *
 * <dl>
 *   <dt><b>Styles:</b>
 *   <dd>NONE and those supported by {@link StructuredViewer}
 *   <dd>SWT.CHECK is used by default
 *   <dt><b>Events:</b>
 *   <dd>(none)
 * </dl>
 */
public abstract class BaseResourceSelectionComposite extends ViewerComposite<CheckboxTreeViewer> {
  private static final Logger log = Logger.getLogger(BaseResourceSelectionComposite.class);

  private static final String SAROS_RESOURCE_SELECTION_PRESET_NAMES =
      "Saros.resource_selection.preset_names";
  private static final String SAROS_RESOURCE_SELECTION_SETTINGS_KEY_PREFIX =
      "Saros.resource_selection.selected.";

  private static final String SERIALIZATION_SEPARATOR = "**#**";
  private static final String SERIALIZATION_SEPARATOR_REGEX = "\\*\\*#\\*\\*";

  private final CheckboxTreeViewer checkboxTreeViewer;

  protected final List<BaseResourceSelectionListener> resourceSelectionListeners;

  /** List of base containers selected by the user. */
  private List<IContainer> selectedBaseContainers;

  /*
   * Stacks used for saving previous selections in the tree view, enabling
   * undo/redo functionality
   */
  private final Deque<List<IContainer>> lastSelected;
  private final Deque<List<IContainer>> prevSelected;

  @Inject private Saros saros;

  @Inject private ISarosSessionManager sessionManager;

  private final ICheckStateListener checkStateListener =
      new ICheckStateListener() {
        @Override
        public void checkStateChanged(CheckStateChangedEvent event) {
          Object element = event.getElement();
          boolean isSelected = event.getChecked();

          IResource resource = (IResource) element;

          if (isInvalidStateChange(resource, isSelected)) {

            checkboxTreeViewer.setChecked(resource, !isSelected);

            // TODO inform user about reverted change

            return;
          }

          IContainer containerToSelect;

          if (resource.getType() != IResource.FILE) {
            containerToSelect = (IContainer) resource;

          } else {
            IContainer parentContainer = resource.getParent();

            if (parentContainer == null) {
              log.error("Could not determine parent resource of selected file " + resource);

              checkboxTreeViewer.setChecked(resource, !isSelected);

              return;
            }

            containerToSelect = parentContainer;
          }

          checkboxTreeViewer.setSubtreeChecked(containerToSelect, isSelected);
          updateSelectedBaseResources(containerToSelect, isSelected);
          notifyResourceSelectionChanged(containerToSelect, isSelected);

          rememberSelection();
        }
      };

  /**
   * Filter for resources that are already shared or can't be shared with the current reference
   * point setup. This includes:
   *
   * <ul>
   *   <li>Already shared resources
   *   <li>Parents of already shared resources that don't contain any other sharable child
   *       containers
   *   <li>Files contained in parents of already shared resources
   * </ul>
   */
  @SuppressWarnings("FieldCanBeLocal")
  private final ViewerFilter sharedResourcesFilter =
      new ViewerFilter() {
        @Override
        public boolean select(Viewer viewer, Object parentElement, Object element) {
          ISarosSession sarosSession = sessionManager.getSession();

          if (sarosSession == null || !(element instanceof IResource)) {
            return true;
          }

          Set<IReferencePoint> referencePoints = sarosSession.getReferencePoints();
          Set<IContainer> referencePointParents = getReferencePointParents(referencePoints);

          if (element instanceof IContainer) {
            Set<IContainer> containerWithoutSharableChildren =
                getIgnoredReferencePointParents(referencePoints, referencePointParents);

            if (containerWithoutSharableChildren.contains(element)) {
              // TODO inform user about filtered resource

              return false;
            }

          } else if (element instanceof IFile) {
            IContainer parent = ((IFile) element).getParent();

            if (referencePointParents.contains(parent)) {
              return false;
            }
          }

          Set<IReferencePoint> sharedReferencePoints = sarosSession.getReferencePoints();
          saros.filesystem.IResource wrappedResource =
              ResourceConverter.convertToResource(sharedReferencePoints, (IResource) element);

          return !sarosSession.isShared(wrappedResource);
        }
      };

  /**
   * Returns all parent resources of the given set of reference points.
   *
   * @param referencePoints the reference points
   * @return all parent resources of the given set of reference points
   */
  private Set<IContainer> getReferencePointParents(Set<IReferencePoint> referencePoints) {

    Set<IContainer> referencePointParents = new HashSet<>();

    for (IReferencePoint referencePoint : referencePoints) {
      IContainer referencePointDelegate = ResourceConverter.getDelegate(referencePoint);

      IContainer parent = referencePointDelegate.getParent();

      while (parent != null && parent.getType() != IResource.ROOT) {
        referencePointParents.add(parent);

        parent = parent.getParent();
      }
    }

    return referencePointParents;
  }

  /**
   * Returns a list of all referent point parent resources that don't have any sharable child
   * resources.
   *
   * <p>Sharable resources in this context are containers that are valid candidates for new
   * reference points that can be added to the session. To be sharable, a container must not have
   * any child resources that are already shared.
   *
   * @param referencePoints the currently shared reference points
   * @return a list of all referent point parent resources that don't have any sharable child
   *     resources
   */
  private Set<IContainer> getIgnoredReferencePointParents(
      Set<IReferencePoint> referencePoints, Set<IContainer> referencePointParents) {
    Set<IContainer> referencePointDelegates =
        referencePoints.stream().map(ResourceConverter::getDelegate).collect(Collectors.toSet());

    List<IContainer> sortedReferencePointParents = new ArrayList<>(referencePointParents);
    sortedReferencePointParents.sort(
        Comparator.comparingInt((IContainer c) -> c.getFullPath().segmentCount()).reversed());

    Set<IContainer> containersWithoutSharableChildren = new HashSet<>();
    Set<IContainer> containersWithSharableChildren = new HashSet<>();

    for (IContainer referencePointParent : sortedReferencePointParents) {
      IResource[] children;

      try {
        children = referencePointParent.members();

      } catch (CoreException e) {
        log.error("Could not obtain members of container " + referencePointParent, e);

        continue;
      }

      boolean hasSharableChildren = false;

      for (IResource child : children) {
        if (!(child instanceof IContainer)) {
          continue;
        }

        if (containersWithSharableChildren.contains(child)
            || (!referencePointParents.contains(child)
                && !referencePointDelegates.contains(child))) {
          hasSharableChildren = true;

          break;
        }
      }

      if (hasSharableChildren) {
        containersWithSharableChildren.add(referencePointParent);
      } else {
        containersWithoutSharableChildren.add(referencePointParent);
      }
    }

    return containersWithoutSharableChildren;
  }

  public BaseResourceSelectionComposite(Composite parent, int style) {
    super(parent, style | SWT.CHECK);

    SarosPluginContext.initComponent(this);

    super.setLayout(LayoutUtils.createGridLayout());

    this.checkboxTreeViewer = getViewer();

    this.checkboxTreeViewer.getControl().setLayoutData(LayoutUtils.createFillGridData());

    this.checkboxTreeViewer.setInput(ResourcesPlugin.getWorkspace().getRoot());
    this.checkboxTreeViewer.addCheckStateListener(checkStateListener);

    addDisposeListener(e -> checkboxTreeViewer.removeCheckStateListener(checkStateListener));

    this.checkboxTreeViewer.addFilter(sharedResourcesFilter);

    this.resourceSelectionListeners = new ArrayList<>();

    this.selectedBaseContainers = new ArrayList<>();

    this.lastSelected = new LinkedList<>();
    this.prevSelected = new LinkedList<>();
  }

  /**
   * Returns whether the change made by the user was invalid.
   *
   * <p>Invalid changes are:
   *
   * <ul>
   *   <li>A resource being deselected while its parent resource is still selected.
   *   <li>A parent resource of an already shared resource is selected.
   * </ul>
   *
   * @param resource the resource element whose state changed
   * @param selected the new state of the resource element
   * @return whether the change is invalid
   */
  private boolean isInvalidStateChange(IResource resource, boolean selected) {
    ISarosSession sarosSession = sessionManager.getSession();

    if (sarosSession != null && resource instanceof IContainer) {
      Set<IContainer> referencePointParents =
          getReferencePointParents(sarosSession.getReferencePoints());

      if (referencePointParents.contains(resource)) {
        return true;
      }
    }

    IResource parentResource = resource.getParent();

    boolean parentSelected = checkboxTreeViewer.getChecked(parentResource);

    return parentSelected && !selected;
  }

  /**
   * Updates the list of base resources selected by the user.
   *
   * <p>If <code>selected=false</code>, the given resource is removed from the list of selected base
   * resources.
   *
   * <p>If <code>selected=true</code>, the given resource is added to the list of selected base
   * resources. Furthermore, all child resources of the given resources are removed from the list as
   * they are now represented by the new base resource.
   *
   * @param container the base resource entry changed by the user
   * @param selected the new state of the resource
   */
  private void updateSelectedBaseResources(IContainer container, boolean selected) {
    if (!selected) {
      selectedBaseContainers.remove(container);

      return;
    }

    selectedBaseContainers.removeIf(
        selectedBaseContainer -> isChildResource(container, selectedBaseContainer));

    selectedBaseContainers.add(container);
  }

  /**
   * Returns whether the given resource is a child resource of the given base resource.
   *
   * @param base the base resource to use for the check
   * @param other the potential child resource
   * @return whether the given resource is a child resource of the given base resource
   */
  private boolean isChildResource(IResource base, IResource other) {
    IPath basePath = base.getFullPath();
    IPath otherPath = other.getFullPath();

    return basePath.isPrefixOf(otherPath);
  }

  /**
   * Remembers the current selection for undo/redo functionality.
   *
   * <p>Any click activity while "redo" is possible (which means something was undone) will clear
   * the whole redo-stack.
   */
  public void rememberSelection() {
    if (isRedoEnabled()) {
      prevSelected.clear();
    }

    lastSelected.push(new ArrayList<>(selectedBaseContainers));
    log.debug(
        "Remembered selected items: "
            + lastSelected.peek().size()
            + " Saved "
            + lastSelected.size()
            + " snapshots");

    // Need to update the controls (if there are any)
    updateRedoUndoControls();
  }

  /** Returns a list of all names of the saved selection presets */
  public List<String> getSavedSelectionNames() {
    List<String> namesList = new ArrayList<>();

    String namesString =
        saros.getPreferenceStore().getString(SAROS_RESOURCE_SELECTION_PRESET_NAMES);

    String[] names = namesString.split(SERIALIZATION_SEPARATOR_REGEX);
    for (String name : names) {
      if (!name.isEmpty()) {
        namesList.add(name);
      }
    }

    log.debug("Read saved selection names: " + namesList.size());

    return namesList;
  }

  /**
   * Adds the given name to the list of saved selection preset names
   *
   * @param name the name to use for the preset
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

    StringBuilder namesString =
        new StringBuilder(
            saros.getPreferenceStore().getString(SAROS_RESOURCE_SELECTION_PRESET_NAMES));

    namesString.append(name).append(SERIALIZATION_SEPARATOR);

    log.debug("Storing namesString: " + namesString.toString());
    saros
        .getPreferenceStore()
        .setValue(SAROS_RESOURCE_SELECTION_PRESET_NAMES, namesString.toString());
  }

  /**
   * Adds the given name to the list of saved selection preset names
   *
   * @param name the name of the preset
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
    saros
        .getPreferenceStore()
        .setValue(SAROS_RESOURCE_SELECTION_PRESET_NAMES, namesString.toString());

    // delete the preference
    String selectionName = SAROS_RESOURCE_SELECTION_SETTINGS_KEY_PREFIX + name;
    saros.getPreferenceStore().setValue(selectionName, "");
    // TODO: find out if there is some sort of delete method..
  }

  /**
   * Save the current base resource selection as a selection preset with the given name using the
   * preferences api, serializing all URIs of the selected resources.
   *
   * <p>Overwrites any existing selections with the same name without warning. Use
   * hasSelectionWithName(String name) to find out if the user would overwrite an existing selection
   * preset!
   *
   * <p>Adds the name to a set of saved preset names in the preferences too. Use
   * getSavedSelectionNames() to retrieve a list of all names
   *
   * @param name the name under which to save the selection
   */
  public void saveSelectionWithName(String name) {
    StringBuilder selectedString = new StringBuilder();

    for (IContainer selectedBaseContainer : selectedBaseContainers) {
      selectedString.append(selectedBaseContainer.getFullPath()).append(SERIALIZATION_SEPARATOR);
    }

    String selectionName = SAROS_RESOURCE_SELECTION_SETTINGS_KEY_PREFIX + name;

    log.debug("Storing selected elements: " + selectedString.toString());

    saros.getPreferenceStore().setValue(selectionName, selectedString.toString());

    rememberSelectionName(name);
  }

  /**
   * Removes any existing selection and applies the selection preset that was saved with the given
   * name (if it exists). Returns false If no selection was made (e.g. there is no such preset) or
   * true if the selection was applied..
   *
   * @param name The name of the preset that should be used
   */
  public boolean restoreSelectionWithName(String name) {
    String selectionName = SAROS_RESOURCE_SELECTION_SETTINGS_KEY_PREFIX + name;

    String selected = saros.getPreferenceStore().getString(selectionName);

    if (selected.isEmpty()) {
      log.debug("selection string is empty");
      /*
       * No empty selections can be saved, so this means that there is no
       * selection preset with the given name
       */
      return false;
    }

    List<IResource> selectedList = new ArrayList<>();

    String[] uris;

    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

    uris = selected.split(SERIALIZATION_SEPARATOR_REGEX);
    for (String uri : uris) {
      IResource resource = root.findMember(uri);
      if (resource != null && resource.getType() != IResource.FILE) {
        selectedList.add(resource);

      } else if (resource != null && resource.getType() == IResource.FILE) {
        log.error("Resource for saved uri " + uri + "is not a container: " + resource);

      } else {
        log.error("Did not find resource with uri in workspace root to apply selection: " + uri);
      }
    }

    /*
     * use "external" selection method to filter out selections that are illegal with the current
     * session state
     */
    setSelectedResources(selectedList);

    return true;
  }

  /** This needs to take care of enabling redo/undo controls */
  protected abstract void updateRedoUndoControls();

  /** Undo the last user action (this enables the redo button if not already enabled). */
  protected void undoSelection() {
    if (!lastSelected.isEmpty()) {
      /*
       * This holds the CURRENT selection (rememberSelection is called
       * after each selection event in the tree, so when the user triggers
       * an undo, the first element in the lastXYZ stacks represent the
       * selection which the user wants to undo) which we need to push
       * onto the redo-stacks.
       */
      List<IContainer> selected = lastSelected.pop();

      prevSelected.push(selected);
    }

    List<IContainer> newSelectedContainers;
    /*
     * Do not combine the two ifs! lastSelected can be empty now because of
     * the modifications...
     */
    if (!lastSelected.isEmpty()) {
      /*
       * Not using the selected variable as it contains the CURRENT
       * selection (which we want to undo). Using it would not undo
       * anything.
       */
      newSelectedContainers = lastSelected.peek();

    } else {
      /*
       * No previous selection available, so unset all selections (set to
       * initial state)
       */
      newSelectedContainers = new ArrayList<>();
    }

    setSelectedResourcesInternal(newSelectedContainers);

    // Need to update the controls (if there are any)
    updateRedoUndoControls();
  }

  /**
   * Restores the last selection if there is one because of a previous undo action. Does nothing if
   * there was no undo operation before the call to redoSelection()
   */
  protected void redoSelection() {
    if (!prevSelected.isEmpty()) {
      List<IContainer> newSelectedContainers = prevSelected.pop();

      lastSelected.push(newSelectedContainers);

      setSelectedResourcesInternal(newSelectedContainers);

    } else {
      log.debug("Cannot redo, no more snapshots!");
    }
    // Need to update the controls (if there are any)
    updateRedoUndoControls();
  }

  /** @return true if redoing the last "undone" selection is possible.. */
  protected boolean isRedoEnabled() {
    return !prevSelected.isEmpty();
  }

  /** @return true if redoing the last "undone" selection is possible.. */
  protected boolean isUndoEnabled() {
    return !lastSelected.isEmpty();
  }

  @Override
  protected CheckboxTreeViewer createViewer(int style) {
    return new CheckboxTreeViewer(new Tree(this, style));
  }

  @Override
  protected void configureViewer(CheckboxTreeViewer viewer) {
    viewer.setContentProvider(new WorkbenchContentProvider());
    viewer.setLabelProvider(new WorkbenchLabelProvider());
    viewer.setUseHashlookup(true);
    viewer.setSorter(new WorkbenchItemsSorter());
  }

  /**
   * Sets selects the given resources. This selection is handled as though it were made by the user,
   * meaning all necessary listeners and internal handlers are called.
   *
   * @param resources the resources to select
   * @see #notifyResourceSelectionChanged(IResource, boolean)
   */
  public void setSelectedResources(List<IResource> resources) {
    Set<IContainer> sanitizedResources = new HashSet<>();

    for (IResource resource : resources) {
      if (resource.getType() != IResource.FILE) {
        sanitizedResources.add((IContainer) resource);

        continue;
      }

      IContainer parentContainer = resource.getParent();

      if (parentContainer == null) {
        log.error("Could not determine parent resource of selected file " + resource);

        continue;
      }

      sanitizedResources.add(parentContainer);
    }

    ISarosSession sarosSession = sessionManager.getSession();

    if (sarosSession != null) {
      Set<IReferencePoint> referencePoints = sarosSession.getReferencePoints();

      Set<IContainer> referencePointParents = getReferencePointParents(referencePoints);

      sanitizedResources.removeIf(
          container ->
              referencePointParents.contains(container)
                  || sarosSession.isShared(
                      ResourceConverter.convertToResource(referencePoints, container)));
    }

    List<IContainer> sanitizedBaseContainers =
        determineBaseContainers(new ArrayList<>(sanitizedResources));

    setSelectedResourcesInternal(sanitizedBaseContainers);
  }

  /**
   * Returns the base containers contained in the given list of containers.
   *
   * <p>In the returned list, it is guaranteed that none of the containers contained in the set is a
   * child resource of another container in the set.
   *
   * @param containers the list of containers
   * @return the base containers contained in the given list of containers
   */
  private List<IContainer> determineBaseContainers(List<IContainer> containers) {
    List<IContainer> baseContainers = new ArrayList<>();

    containers.sort(
        (r1, r2) -> {
          IPath path1 = r1.getFullPath();
          IPath path2 = r2.getFullPath();

          // sort in ascending order
          return Integer.compare(path1.segmentCount(), path2.segmentCount());
        });

    for (IContainer container : containers) {
      boolean isChild =
          baseContainers
              .stream()
              .anyMatch((baseContainer) -> isChildResource(baseContainer, container));

      if (!isChild) {
        baseContainers.add(container);
      }
    }

    return baseContainers;
  }

  /**
   * Sets the given containers and its children as selected. This selection is handled as though it
   * were made by the user, meaning all necessary listeners and internal handlers are called.
   *
   * <p><b>NOTE:</b> This method expects that the list of given containers only contains base
   * resources, i.e. that none of the contained resources is a child resource of another contained
   * resource.
   *
   * @param containers the containers to select
   * @see #notifyResourceSelectionChanged(IResource, boolean)
   */
  private void setSelectedResourcesInternal(List<IContainer> containers) {
    List<IContainer> selectedResourcesBeforeUpdate = selectedBaseContainers;
    selectedBaseContainers = new ArrayList<>(containers);

    /*
     * Does not fire events...
     */
    checkboxTreeViewer.setCheckedElements(containers.toArray());
    applyAdditionalSelections(containers);

    /*
     * ... therefore we have to fire them.
     */

    Set<IContainer> newSelectedResources = new HashSet<>(containers);
    newSelectedResources.removeAll(selectedResourcesBeforeUpdate);

    Set<IContainer> newDeselectedResources = new HashSet<>(selectedResourcesBeforeUpdate);
    newDeselectedResources.removeAll(containers);

    for (IContainer resource : newSelectedResources) {
      notifyResourceSelectionChanged(resource, true);
    }

    for (IContainer resource : newDeselectedResources) {
      notifyResourceSelectionChanged(resource, false);
    }
  }

  /**
   * Sets the subtree for every given selected base container as selected. Subsequently expands the
   * tree to show every selected base container.
   *
   * @param selectedBaseContainers the new selected base containers
   */
  private void applyAdditionalSelections(List<IContainer> selectedBaseContainers) {
    for (IContainer selectedBaseResource : selectedBaseContainers) {
      checkboxTreeViewer.setSubtreeChecked(selectedBaseResource, true);
      checkboxTreeViewer.expandToLevel(selectedBaseResource, 0);
    }
  }

  /**
   * Returns the base containers selected by the user.
   *
   * @return the base containers selected by the user
   * @see #hasSelectedResources()
   */
  public List<IContainer> getSelectedBaseContainers() {
    return new ArrayList<>(selectedBaseContainers);
  }

  /** Returns true if at least one base resource is selected. */
  public boolean hasSelectedResources() {
    return !selectedBaseContainers.isEmpty();
  }

  /**
   * Adds a {@link BaseResourceSelectionListener}.
   *
   * @param resourceSelectionListener the resource listener to add
   */
  public void addResourceSelectionListener(
      BaseResourceSelectionListener resourceSelectionListener) {
    this.resourceSelectionListeners.add(resourceSelectionListener);
  }

  /**
   * Notifies all registered resource selection change listeners about selection change.
   *
   * @param resource the resource who's selection changed
   * @param isSelected the new selection state
   */
  private void notifyResourceSelectionChanged(IResource resource, boolean isSelected) {
    ResourceSelectionChangedEvent event = new ResourceSelectionChangedEvent(resource, isSelected);
    for (BaseResourceSelectionListener resourceSelectionListener :
        this.resourceSelectionListeners) {
      resourceSelectionListener.resourceSelectionChanged(event);
    }
  }

  @Override
  public void setLayout(Layout layout) {
    // ignore
  }
}
