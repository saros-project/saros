package saros.ui.widgets.viewer.project;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
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
import saros.ui.widgets.viewer.project.events.BaseResourceSelectionListener;
import saros.ui.widgets.viewer.project.events.ResourceSelectionChangedEvent;
import saros.util.ArrayUtils;

/**
 * This {@link Composite} allows to check (via check boxes) {@link IProject}s, {@link IFolder}s and
 * {@link IFile} s.
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
 *
 * @author bkahlert
 * @author kheld
 * @author waldmann
 */
public abstract class BaseResourceSelectionComposite extends ViewerComposite<CheckboxTreeViewer> {

  private static final Logger log = Logger.getLogger(BaseResourceSelectionComposite.class);

  private static final String SAROS_RESOURCE_SELECTION_PRESET_NAMES =
      "Saros.resource_selection.preset_names";
  protected List<BaseResourceSelectionListener> resourceSelectionListeners =
      new ArrayList<BaseResourceSelectionListener>();
  protected final CheckboxTreeViewer checkboxTreeViewer;

  /** List of base resources checked by the user. */
  private List<IResource> selectedBaseResources;

  /*
   * Stacks used for saving previous selections in the tree view, enabling
   * undo/redo functionality
   */
  protected Stack<Object[]> lastChecked = new Stack<Object[]>();
  protected Stack<Object[]> lastGrayed = new Stack<Object[]>();
  protected Stack<Object[]> prevChecked = new Stack<Object[]>();
  protected Stack<Object[]> prevGrayed = new Stack<Object[]>();

  protected static final String SERIALIZATION_SEPARATOR = "**#**";
  protected static final String SERIALIZATION_SEPARATOR_REGEX = "\\*\\*#\\*\\*";

  @Inject protected Saros saros;

  protected ICheckStateListener checkStateListener =
      new ICheckStateListener() {
        @Override
        public void checkStateChanged(CheckStateChangedEvent event) {
          final Object element = event.getElement();
          boolean isChecked = event.getChecked();

          IResource resource = (IResource) element;

          boolean isValidChange = handleCheckStateChanged(resource, isChecked);

          if (isValidChange) {
            notifyResourceSelectionChanged(resource, isChecked);
          }

          rememberSelection();
        }
      };

  /**
   * Updates the tree on user input. Returns whether the change made by the user was valid.
   * Non-valid changes are reverted.
   *
   * <p>The tree is updates as follows:
   *
   * <ul>
   *   <li>If a folder is checked, its complete subtree is checked as well.
   *   <li>If a file is checked, its parent folder as well as its other children are checked as
   *       well.
   *   <li>If a resource is no longer checked while its parent resource is still checked, the user
   *       action is reverted by re-checking the resource.
   * </ul>
   *
   * Reverting the un-checking of a resource whose parent resource is still checked is necessary to
   * ensure that only complete trees are shared. As the {@link CheckboxTreeViewer} does not allow
   * disabling elements, reverting the user changes is the only option to enforce this.
   *
   * <p>This method is only called for changes caused by user actions.
   *
   * @param resource the resource element whose state changed
   * @param checked the new state of the resource element
   * @return whether the change is valid
   */
  private boolean handleCheckStateChanged(IResource resource, boolean checked) {
    IResource parentResource = resource.getParent();

    boolean parentChecked = checkboxTreeViewer.getChecked(parentResource);

    if (parentChecked && !checked) {
      checkboxTreeViewer.setChecked(resource, true);

      // TODO inform user about preventing illegal selection; whole tree must always be selected

      return false;
    }

    IResource resourceToCheck;

    if (resource.getType() == IResource.FILE) {
      if (parentResource == null) {
        log.error("Encountered file without parent resource: " + resource);

        if (checked) {
          checkboxTreeViewer.setChecked(resource, false);
        }

        return false;
      }

      resourceToCheck = parentResource;
    } else {
      resourceToCheck = resource;
    }

    checkboxTreeViewer.setSubtreeChecked(resourceToCheck, checked);

    updateCheckedBaseResources(resourceToCheck, checked);

    return true;
  }

  /**
   * Updates the list of base resources checked by the user.
   *
   * <p>If <code>checked==false</code>, the given resource is removed from the list of checked base
   * resources.
   *
   * <p>If <code>checked==true</code>, the given resource is added to the list of checked base
   * resources. Furthermore, all child resources of the given resources are removed from the list as
   * they are now represented by the new base resource.
   *
   * @param resource the base resource entry changed by the user
   * @param checked the new state of the resource
   */
  private void updateCheckedBaseResources(IResource resource, boolean checked) {
    if (!checked) {
      selectedBaseResources.remove(resource);

      return;
    }

    selectedBaseResources.removeIf(
        selectedBaseResource -> isChildResource(resource, selectedBaseResource));

    selectedBaseResources.add(resource);
  }

  /**
   * Returns whether the given resource is a child resource of the given base resource.
   *
   * @param base the base resource to use for the check
   * @param other the potential child resource
   * @return whether the given resource is a child resource of the given base resource
   */
  boolean isChildResource(IResource base, IResource other) {
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
      prevChecked.clear();
      prevGrayed.clear();
    }

    lastGrayed.push(checkboxTreeViewer.getGrayedElements());
    lastChecked.push(checkboxTreeViewer.getCheckedElements());
    log.debug(
        "Remembered checked/grayed items: "
            + lastChecked.lastElement().length
            + "/"
            + lastGrayed.lastElement().length
            + " Saved "
            + lastChecked.size()
            + " snapshots");

    // Need to update the controls (if there are any)
    updateRedoUndoControls();
  }

  /** Returns a list of all names of the saved selection presets */
  public List<String> getSavedSelectionNames() {
    List<String> namesList = new ArrayList<String>();

    String namesString =
        saros.getPreferenceStore().getString(SAROS_RESOURCE_SELECTION_PRESET_NAMES);

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
    saros
        .getPreferenceStore()
        .setValue(SAROS_RESOURCE_SELECTION_PRESET_NAMES, namesString.toString());

    // delete the preference
    String checkedSelectionName = "Saros.resource_selection.checked." + name;
    saros.getPreferenceStore().setValue(checkedSelectionName, "");
    // TODO: find out if there is some sort of delete method..
  }

  /**
   * Save the current selection as a selection preset with the given name using the preferences api,
   * serializing all URIs of the selected iResources.
   *
   * <p>Overwrites any existing selections with the same name without warning. Use
   * hasSelectionWithName(String name) to find out if the user would overwrite an existing selection
   * preset!
   *
   * <p>Adds the name to a set of saved preset names in the preferences too. Use
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
    Object[] checked = checkboxTreeViewer.getCheckedElements();

    StringBuilder checkedString = new StringBuilder();

    for (Object resource : checked) {
      if (!checkboxTreeViewer.getGrayed(resource)) {
        checkedString.append(((IResource) resource).getFullPath()).append(SERIALIZATION_SEPARATOR);
      }
    }

    String checkedSelectionName = "Saros.resource_selection.checked." + name;

    log.debug("Storing checked elements (not grayed): " + checkedString.toString());

    saros.getPreferenceStore().setValue(checkedSelectionName, checkedString.toString());

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
    String checkedSelectionName = "Saros.resource_selection.checked." + name;

    String checked = saros.getPreferenceStore().getString(checkedSelectionName);

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
        log.error("Did not find resource with uri in workspace root to apply selection: " + uri);
      }
    }

    setSelectedResourcesInternal(checkedList);

    return true;
  }

  /** This needs to take care of enabling redo/undo controls */
  public abstract void updateRedoUndoControls();

  /** Undo the last user action (this enables the redo button if not already enabled). */
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
      checkboxTreeViewer.setGrayedElements(lastGrayed.lastElement());
      checkboxTreeViewer.setCheckedElements(lastChecked.lastElement());

    } else {
      /*
       * No previous selection available, so unset all selections (set to
       * initial state)
       */
      checkboxTreeViewer.setCheckedElements(new Object[0]);
      checkboxTreeViewer.setGrayedElements(new Object[0]);
    }

    // Need to update the controls (if there are any)
    updateRedoUndoControls();
  }

  /**
   * Restores the last selection if there is one because of a previous undo action. Does nothing if
   * there was no undo operation before the call to redoSelection()
   */
  protected void redoSelection() {
    if (!prevChecked.isEmpty()) {
      Object[] checkedElements = prevChecked.pop();
      Object[] grayedElements = prevGrayed.pop();

      lastChecked.push(checkedElements);
      lastGrayed.push(grayedElements);

      checkboxTreeViewer.setGrayedElements(grayedElements);
      checkboxTreeViewer.setCheckedElements(checkedElements);

      // Disable redo button if no redo possible anymore

    } else {
      log.debug("Cannot redo, no more snapshots!");
    }
    // Need to update the controls (if there are any)
    updateRedoUndoControls();
  }

  /** @return true if redoing the last "undone" selection is possible.. */
  protected boolean isRedoEnabled() {
    return !prevChecked.isEmpty();
  }

  /** @return true if redoing the last "undone" selection is possible.. */
  protected boolean isUndoEnabled() {
    return !lastChecked.isEmpty();
  }

  public BaseResourceSelectionComposite(Composite parent, int style) {
    super(parent, style | SWT.CHECK);

    SarosPluginContext.initComponent(this);

    super.setLayout(LayoutUtils.createGridLayout());

    checkboxTreeViewer = getViewer();

    checkboxTreeViewer.getControl().setLayoutData(LayoutUtils.createFillGridData());

    checkboxTreeViewer.setInput(ResourcesPlugin.getWorkspace().getRoot());
    checkboxTreeViewer.addCheckStateListener(checkStateListener);

    addDisposeListener(
        new DisposeListener() {
          @Override
          public void widgetDisposed(DisposeEvent e) {
            if (checkboxTreeViewer != null)
              checkboxTreeViewer.removeCheckStateListener(checkStateListener);
          }
        });

    SarosPluginContext.initComponent(this);
    checkboxTreeViewer.addFilter(sharedProjectsFilter);

    this.selectedBaseResources = new ArrayList<>();
  }

  @Inject protected ISarosSessionManager sessionManager;

  /** Filter for already shared resources. */
  protected ViewerFilter sharedProjectsFilter =
      new ViewerFilter() {
        @Override
        public boolean select(Viewer viewer, Object parentElement, Object element) {
          ISarosSession sarosSession = sessionManager.getSession();

          if (sarosSession != null && element instanceof IResource) {
            Set<IReferencePoint> sharedReferencePoints = sarosSession.getReferencePoints();
            saros.filesystem.IResource wrappedResource =
                ResourceConverter.convertToResource(sharedReferencePoints, (IResource) element);

            return !sarosSession.isShared(wrappedResource);
          }

          return true;
        }
      };

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
   * @see #handleCheckStateChanged(IResource, boolean)
   * @see #notifyResourceSelectionChanged(IResource, boolean)
   */
  public void setSelectedResources(List<IResource> resources) {
    List<IResource> baseResources = determineBaseResources(resources);

    setSelectedResourcesInternal(baseResources);
  }

  /**
   * Returns the base resources contained in the given list of resources.
   *
   * <p>In the returned list, it is guaranteed that none of the resources contained in the set is a
   * child resource of another resource in the set.
   *
   * @param resources the list of resources
   * @return the base resources contained in the given list of resources
   */
  private List<IResource> determineBaseResources(List<IResource> resources) {
    List<IResource> baseResources = new ArrayList<>();

    resources.sort(
        (r1, r2) -> {
          IPath path1 = r1.getFullPath();
          IPath path2 = r2.getFullPath();

          return Integer.compare(path1.segmentCount(), path2.segmentCount());
        });

    for (IResource resource : resources) {
      boolean isChild =
          baseResources
              .stream()
              .anyMatch((baseResource) -> isChildResource(baseResource, resource));

      if (!isChild) {
        baseResources.add(resource);
      }
    }

    return baseResources;
  }

  /**
   * Sets selects the given resources. This selection is handled as though it were made by the user,
   * meaning all necessary listeners and internal handlers are called.
   *
   * <p><b>NOTE:</b> This method expects that the list of given resources only contains base
   * resources, i.e. that none of the contained resources is a child resource of another contained
   * resource.
   *
   * @param resources the resources to select
   * @see #handleCheckStateChanged(IResource, boolean)
   * @see #notifyResourceSelectionChanged(IResource, boolean)
   */
  private void setSelectedResourcesInternal(List<IResource> resources) {

    List<IResource> checkedResourcesBeforeUpdate = selectedBaseResources;
    selectedBaseResources = new ArrayList<>(resources);

    /*
     * Does not fire events...
     */
    checkboxTreeViewer.setCheckedElements(resources.toArray());

    for (IResource resource : resources) {
      handleCheckStateChanged(resource, true);
    }

    /*
     * ... therefore we have to fire them.
     */

    Set<IResource> newCheckedResources = new HashSet<>(resources);
    newCheckedResources.removeAll(checkedResourcesBeforeUpdate);

    Set<IResource> newUncheckedResources = new HashSet<>(checkedResourcesBeforeUpdate);
    newUncheckedResources.removeAll(resources);

    for (IResource resource : newCheckedResources) {
      notifyResourceSelectionChanged(resource, true);
    }

    for (IResource resource : newUncheckedResources) {
      notifyResourceSelectionChanged(resource, false);
    }
  }

  /**
   * Returns the base resources currently checked by the user.
   *
   * @return the base resources currently checked by the user
   * @see #hasSelectedResources()
   */
  public List<IResource> getSelectedResources() {
    return new ArrayList<>(selectedBaseResources);
  }

  /** Returns true if at least one base resource is selected */
  public boolean hasSelectedResources() {
    return !selectedBaseResources.isEmpty();
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
   * Notify all {@link BaseResourceSelectionListener}s about a changed selection.
   *
   * @param resource {@link IResource} who's selection changed
   * @param isSelected new selection state
   */
  public void notifyResourceSelectionChanged(IResource resource, boolean isSelected) {
    ResourceSelectionChangedEvent event = new ResourceSelectionChangedEvent(resource, isSelected);
    for (BaseResourceSelectionListener resourceSelectionListener :
        this.resourceSelectionListeners) {
      resourceSelectionListener.resourceSelectionChanged(event);
    }
  }

  /**
   * Returns the displayed Project {@link IResource}s.
   *
   * @return
   */
  public List<IResource> getResources() {
    WorkbenchContentProvider contentProvider =
        (WorkbenchContentProvider) getViewer().getContentProvider();

    Object[] objects = contentProvider.getElements(getViewer().getInput());
    return ArrayUtils.getAdaptableObjects(objects, IResource.class, Platform.getAdapterManager());
  }

  /**
   * Returns the displayed {@link IProject}s.
   *
   * @return
   */
  public int getProjectsCount() {
    WorkbenchContentProvider contentProvider =
        (WorkbenchContentProvider) getViewer().getContentProvider();

    Object[] objects = contentProvider.getElements(getViewer().getInput());
    return ArrayUtils.getAdaptableObjects(objects, IProject.class, Platform.getAdapterManager())
        .size();
  }

  @Override
  public void setLayout(Layout layout) {
    // ignore
  }
}
