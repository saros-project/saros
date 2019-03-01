package saros.ui.widgets.viewer.roster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Tree;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.RosterEntry;
import org.picocontainer.annotations.Inject;
import saros.SarosPluginContext;
import saros.net.ConnectionState;
import saros.net.xmpp.IConnectionListener;
import saros.net.xmpp.JID;
import saros.net.xmpp.XMPPConnectionService;
import saros.ui.model.ITreeElement;
import saros.ui.model.TreeLabelProvider;
import saros.ui.model.roster.RosterCheckStateProvider;
import saros.ui.model.roster.RosterComparator;
import saros.ui.model.roster.RosterContentProvider;
import saros.ui.model.roster.RosterEntryElement;
import saros.ui.util.LayoutUtils;
import saros.ui.util.ViewerUtils;
import saros.ui.widgets.viewer.ViewerComposite;
import saros.ui.widgets.viewer.roster.events.ContactSelectionChangedEvent;
import saros.ui.widgets.viewer.roster.events.ContactSelectionListener;
import saros.util.ArrayUtils;

/**
 * This {@link Composite} displays {@link RosterEntry roster entries} and allows to check (via check
 * boxes) them.
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
 */
public class ContactSelectionComposite extends ViewerComposite<CheckboxTreeViewer> {

  protected RosterCheckStateProvider checkStateProvider;

  protected List<ContactSelectionListener> contactSelectionListeners =
      new ArrayList<ContactSelectionListener>();

  @Inject protected XMPPConnectionService connectionService;

  protected final IConnectionListener connectionListener =
      new IConnectionListener() {
        @Override
        public void connectionStateChanged(Connection connection, ConnectionState newState) {
          switch (newState) {
            case CONNECTED:
              ViewerUtils.setInput(getViewer(), connectionService.getRoster());
              ViewerUtils.expandAll(getViewer());
              break;
            case NOT_CONNECTED:
              /*
               * The Roster should also be displayed in case we are not
               * connected but have been already connected before.
               */
              // ViewerUtils.setInput(viewer, null);
              break;
            default:
              break;
          }
        }
      };

  protected final ICheckStateListener checkStateListener =
      new ICheckStateListener() {
        @Override
        public void checkStateChanged(CheckStateChangedEvent event) {
          // Update the check state
          checkStateProvider.setChecked(event.getElement(), event.getChecked());

          // Fire selection event
          JID jid = (JID) Platform.getAdapterManager().getAdapter(event.getElement(), JID.class);
          if (jid != null) notifyContactSelectionChanged(jid, event.getChecked());
        }
      };

  public ContactSelectionComposite(Composite parent, int style) {
    super(parent, style | SWT.CHECK);

    SarosPluginContext.initComponent(this);

    super.setLayout(LayoutUtils.createGridLayout());

    getViewer().getControl().setLayoutData(LayoutUtils.createFillGridData());
    getViewer().setInput(connectionService.getRoster());

    ViewerUtils.expandAll(getViewer());

    connectionService.addListener(connectionListener);

    getViewer().addCheckStateListener(checkStateListener);

    addDisposeListener(
        new DisposeListener() {
          @Override
          public void widgetDisposed(DisposeEvent e) {
            CheckboxTreeViewer viewer = getViewer();
            if (viewer != null) viewer.removeCheckStateListener(checkStateListener);

            if (connectionService != null) connectionService.removeListener(connectionListener);
          }
        });
  }

  @Override
  public CheckboxTreeViewer createViewer(int style) {
    /*
     * The normal CheckboxTreeViewer does not preserve the checkbox states.
     * We therefore use a workaround class.
     */
    return new CheckboxTreeViewer(new Tree(this, style));
  }

  @Override
  protected void configureViewer(CheckboxTreeViewer viewer) {
    viewer.setContentProvider(new RosterContentProvider());
    viewer.setLabelProvider(new TreeLabelProvider());
    viewer.setComparator(new RosterComparator());
    viewer.setUseHashlookup(true);

    checkStateProvider = new RosterCheckStateProvider();
    viewer.setCheckStateProvider(checkStateProvider);
  }

  /**
   * Sets the currently selected {@link JID}s.
   *
   * @param contacts
   */
  public void setSelectedContacts(List<JID> contacts) {
    CheckboxTreeViewer treeViewer = getViewer();

    List<RosterEntryElement> allElements = collectAllRosterEntryElement(treeViewer);

    List<RosterEntryElement> checkedElements =
        ArrayUtils.getAdaptableObjects(
            treeViewer.getCheckedElements(),
            RosterEntryElement.class,
            Platform.getAdapterManager());

    List<RosterEntryElement> elementsToCheck = new ArrayList<RosterEntryElement>();

    // insert dummy values except the jid as those elements are never
    // display anyways and only used for comparison
    for (JID contact : contacts) elementsToCheck.add(new RosterEntryElement(null, contact, false));

    Map<RosterEntryElement, Boolean> checkStatesChanges =
        calculateCheckStateDiff(allElements, checkedElements, elementsToCheck);

    /*
     * Update the check state in the RosterCheckStateProvider
     */
    for (Entry<RosterEntryElement, Boolean> entryElement : checkStatesChanges.entrySet()) {
      RosterEntryElement rosterEntryElement = entryElement.getKey();
      boolean checked = checkStatesChanges.get(rosterEntryElement);
      checkStateProvider.setChecked(rosterEntryElement, checked);
    }

    /*
     * Refresh the viewer in order to reflect the new check states.
     */
    treeViewer.refresh();

    /*
     * Fire events
     */
    for (Entry<RosterEntryElement, Boolean> entryElement : checkStatesChanges.entrySet()) {
      RosterEntryElement rosterEntryElement = entryElement.getKey();
      boolean checked = checkStatesChanges.get(rosterEntryElement);
      notifyContactSelectionChanged((JID) rosterEntryElement.getAdapter(JID.class), checked);
    }
  }

  /**
   * Calculates from a given set of {@link RosterEntryElement}s which {@link RosterEntryElement}s
   * change their check state.
   *
   * @param allRosterEntryElements
   * @param checkedRosterEntryElement {@link RosterEntryElement}s which are already checked
   * @param rosterEntryElementToCheck {@link RosterEntryElement}s which have to be exclusively
   *     checked
   * @return {@link Map} of {@link RosterEntryElement} that must change their check state
   */
  protected Map<RosterEntryElement, Boolean> calculateCheckStateDiff(
      List<RosterEntryElement> allRosterEntryElements,
      List<RosterEntryElement> checkedRosterEntryElement,
      List<RosterEntryElement> rosterEntryElementToCheck) {

    Map<RosterEntryElement, Boolean> checkStatesChanges =
        new HashMap<RosterEntryElement, Boolean>();
    for (RosterEntryElement rosterEntryElement : allRosterEntryElements) {
      if (rosterEntryElementToCheck.contains(rosterEntryElement)
          && !checkedRosterEntryElement.contains(rosterEntryElement)) {
        checkStatesChanges.put(rosterEntryElement, true);
      } else if (!rosterEntryElementToCheck.contains(rosterEntryElement)
          && checkedRosterEntryElement.contains(rosterEntryElement)) {
        checkStatesChanges.put(rosterEntryElement, false);
      }
    }

    return checkStatesChanges;
  }

  /**
   * Returns the currently selected {@link JID}s.
   *
   * @return
   */
  public List<JID> getSelectedContacts() {
    List<JID> contacts = new ArrayList<JID>();

    for (Object element : getViewer().getCheckedElements()) {
      JID contact = (JID) ((ITreeElement) element).getAdapter(JID.class);
      if (contact != null) contacts.add(contact);
    }
    return contacts;
  }

  /**
   * Returns the currently selected {@link JID}s that support Saros.
   *
   * @return
   */
  public List<JID> getSelectedContactsWithSarosSupport() {
    List<JID> contacts = new ArrayList<JID>();
    for (Object element : getViewer().getCheckedElements()) {
      JID contact = (JID) ((ITreeElement) element).getAdapter(JID.class);
      boolean isSarosSupported =
          element instanceof RosterEntryElement
              && ((RosterEntryElement) element).isSarosSupported();

      if (contact != null && isSarosSupported) contacts.add(contact);
    }
    return contacts;
  }

  /**
   * Returns the current online status of selected entries. If one of the selected
   * RosterEntryElements is offline, the return value is false.
   *
   * @return
   */
  public boolean areAllSelectedOnline() {
    for (Object element : getViewer().getCheckedElements()) {
      if (element instanceof RosterEntryElement && !((RosterEntryElement) element).isOnline())
        return false;
    }
    return true;
  }

  /**
   * Adds a {@link ContactSelectionListener}
   *
   * @param contactSelectionListener
   */
  public void addContactSelectionListener(ContactSelectionListener contactSelectionListener) {
    contactSelectionListeners.add(contactSelectionListener);
  }

  /**
   * Removes a {@link ContactSelectionListener}
   *
   * @param contactSelectionListener
   */
  public void removeContactSelectionListener(ContactSelectionListener contactSelectionListener) {
    contactSelectionListeners.remove(contactSelectionListener);
  }

  /**
   * Notify all {@link ContactSelectionListener}s about a changed selection.
   *
   * @param jid of the contact who's selection changed
   * @param isSelected new selection state
   */
  public void notifyContactSelectionChanged(JID jid, boolean isSelected) {
    ContactSelectionChangedEvent event = new ContactSelectionChangedEvent(jid, isSelected);

    for (ContactSelectionListener contactSelectionListener : contactSelectionListeners)
      contactSelectionListener.contactSelectionChanged(event);
  }

  @Override
  public void setLayout(Layout layout) {
    // ignore
  }

  /**
   * Gathers the checked states of the given widget and its descendants, following a pre-order
   * traversal of the {@link ITreeContentProvider}.
   *
   * @param treeViewer to be traversed
   * @return
   */
  protected static List<RosterEntryElement> collectAllRosterEntryElement(TreeViewer treeViewer) {
    ITreeContentProvider treeContentProvider =
        (ITreeContentProvider) treeViewer.getContentProvider();

    List<Object> collectedObjects = new ArrayList<Object>();

    Object[] objects = treeContentProvider.getElements(treeViewer.getInput());

    for (Object object : objects) {
      collectedObjects.add(object);
      collectAllRosterEntryElement(collectedObjects, treeViewer, object);
    }

    return ArrayUtils.getInstances(collectedObjects.toArray(), RosterEntryElement.class);
  }

  /**
   * Gathers the checked states of the given widget and its descendants, following a pre-order
   * traversal of the {@link ITreeContentProvider}.
   *
   * @param collectedObjects a writable list of elements (element type: <code>Object</code> )
   * @param treeViewer to be traversed
   * @param parentElement of which to determine the child nodes
   */
  protected static void collectAllRosterEntryElement(
      List<Object> collectedObjects, TreeViewer treeViewer, Object parentElement) {

    ITreeContentProvider treeContentProvider =
        (ITreeContentProvider) treeViewer.getContentProvider();

    Object[] objects = treeContentProvider.getChildren(parentElement);

    for (Object object : objects) {
      collectedObjects.add(object);
      collectAllRosterEntryElement(collectedObjects, treeViewer, object);
    }
  }
}
