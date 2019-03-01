package saros.whiteboard.sxe.records;

import com.google.gson.annotations.Expose;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;
import saros.whiteboard.sxe.SXEController;
import saros.whiteboard.sxe.constants.NodeType;
import saros.whiteboard.sxe.exceptions.XMLNotWellFormedException;
import saros.whiteboard.sxe.records.ChildRecordChangeCache.ChildRecordChangeListener;
import saros.whiteboard.sxe.util.AttributeSet;
import saros.whiteboard.sxe.util.NodeSet;

/**
 * Implementation of a DOM element in context of Shared XML Editing XEP-0284 (SXE).
 *
 * <p>It extends the {@link NodeRecord} functionality by an attribute and child element hierarchy
 * plus respective utility methods and notification.
 *
 * @author jurke
 */
public class ElementRecord extends NodeRecord {

  private static final Logger log = Logger.getLogger(ElementRecord.class);

  @Expose private final NodeSet<ElementRecord> children = new NodeSet<ElementRecord>();
  @Expose private final AttributeSet attributes = new AttributeSet();

  /**
   * Used to notify listeners only after a whole SXECommand/SXEMessage was executed.
   *
   * @see saros.whiteboard.sxe.SXEController#notifyLocalListeners()
   */
  protected ChildRecordChangeCache changeSupport;

  public ElementRecord(DocumentRecord documentRecord) {
    super(documentRecord, NodeType.ELEMENT);
  }

  protected NodeSet<ElementRecord> getChildElements() {
    return children;
  }

  public List<ElementRecord> getVisibleChildElements() {
    LinkedList<ElementRecord> visibleChildren = new LinkedList<ElementRecord>();
    for (ElementRecord e : children) {
      if (e.isVisible()) visibleChildren.add(e);
    }
    return visibleChildren;
  }

  protected List<ElementRecord> getAllDescendantElements() {
    LinkedList<ElementRecord> descendants = new LinkedList<ElementRecord>();

    descendants.addAll(getChildElements());

    for (ElementRecord er : getChildElements()) {
      descendants.addAll(er.getAllDescendantElements());
    }

    return descendants;
  }

  public List<ElementRecord> getAllVisibleDescendantElements() {
    LinkedList<ElementRecord> descendants = new LinkedList<ElementRecord>();

    descendants.addAll(getVisibleChildElements());

    for (ElementRecord er : getVisibleChildElements()) {
      descendants.addAll(er.getAllVisibleDescendantElements());
    }

    return descendants;
  }

  protected List<NodeRecord> getAllDescendantNodes() {
    List<NodeRecord> records = new LinkedList<NodeRecord>();

    records.addAll(getAttributes());
    records.addAll(getChildElements());

    for (ElementRecord r : getChildElements()) records.addAll(r.getAllDescendantNodes());

    return records;
  }

  protected AttributeSet getAttributes() {
    return attributes;
  }

  public List<AttributeRecord> getVisibleAttributes() {
    LinkedList<AttributeRecord> visibleAttributes = new LinkedList<AttributeRecord>();
    for (AttributeRecord e : attributes) {
      if (e.isVisible()) visibleAttributes.add(e);
    }
    return visibleAttributes;
  }

  /* Helpers */

  public AttributeRecord getAttribute(String key) {
    return attributes.get(key);
  }

  /**
   * Helper to access the chdata attribute of a child AttributeRecord
   *
   * @param key
   * @return the chdata value of the child AttributeRecord with name key
   */
  public String getAttributeValue(String key) {
    AttributeRecord r = attributes.get(key);
    if (r == null) return null;
    return r.getChdata();
  }

  /**
   * Helper to access the chdata attribute as int of a child AttributeRecord
   *
   * @param key
   * @return parsed integer from chdata attribute value of the child AttributeRecord with name key
   * @throws NullPointerException , NumberFormatException respective Integer.parseInt()
   */
  public int getAttributeInt(String key) throws NullPointerException, NumberFormatException {
    return Integer.parseInt(getAttributeValue(key));
  }

  /* create child records */

  /**
   * Returns the ElementRecord created by DocumentRecord with this as parent.
   *
   * @see saros.whiteboard.sxe.records.DocumentRecord#createElementRecord(String, String)
   */
  public ElementRecord createNewElementRecord(String ns, String name) {
    ElementRecord record = getDocumentRecord().createElementRecord(ns, name);
    record.setParent(this);
    return record;
  }

  /**
   * Helper to generalize the access to create Attributes.
   *
   * <p>With "withoutSetRecords" set to true it has the same behavior as {@link
   * #createNewAttributeRecord(String, String, String)}. </br>
   *
   * <p>Set to false: It returns a SetRecord if an AttributeRecord for the provided name exists
   * already, else a new AttributeRecord is returned.
   *
   * @param onlyCreateNewRecords whether to return a SetRecord if an AttributeRecord for the
   *     provided name exists already or not to do it.
   * @return A AttributeRecord or SetRecord respectively
   */
  public IRecord createNewOrSetAttributeRecord(
      String ns, String name, String chdata, boolean onlyCreateNewRecords) {
    AttributeRecord r = attributes.get(name);
    if (r == null || onlyCreateNewRecords) return createNewAttributeRecord(ns, name, chdata);
    if (ns != null && !ns.equals(r.getNs())) {
      log.warn("Namespaces do not match: " + ns + " vs. " + r.getNs());
    }
    return r.createSetRecord(chdata);
  }

  /**
   * Returns the AttributeRecord created by DocumentRecord with this as parent.
   *
   * @see saros.whiteboard.sxe.records.DocumentRecord#createAttributeRecord(String, String)
   */
  public AttributeRecord createNewAttributeRecord(String ns, String name, String chdata) {
    AttributeRecord record = getDocumentRecord().createAttributeRecord(ns, name, chdata);
    record.setParent(this);
    return record;
  }

  /**
   * Initializes the {@link ChildRecordChangeCache} to the passed controller.
   *
   * @param controller
   */
  protected void initChangeListenerCache(SXEController controller) {
    changeSupport = new ChildRecordChangeCache(this, controller);
  }

  /** Inserts this record to the SXE tree and DocumentRecord. */
  @Override
  public boolean apply(DocumentRecord document) {
    if (getParent() == null && !document.isEmpty()) return false; // has neither parent nor is root

    initChangeListenerCache(document.getController());

    if (!document.isEmpty()) {
      /*
       * Don't add it again (don't send it to peers) if it is already
       * contained
       */
      if (getDocumentRecord().contains(this)) return false;
      getParent().add(this);
      getParent().notifyChildChange(this);
    } // if root, just insert it to the document as first

    document.insert(this);

    return true;
  }

  protected String prefix() {
    return "[WB] " + getName() + "(" + getRid() + "): ";
  }

  /**
   * Removes a child node from its set and adds it again respective its primary-weight.
   *
   * <p>This has to be done if the primary-weight has changed.
   *
   * @param child
   */
  protected void reattachChild(NodeRecord child) {
    if (child.getNodeType().equals(NodeType.ATTR)) {
      attributes.remove(child);
      attributes.add((AttributeRecord) child);
    } else {
      children.remove(child);
      children.add((ElementRecord) child);
    }
  }

  /**
   * Removes all set records and child nodes. </br>
   *
   * <p>Usage: to remove all child references on uncommitted records
   */
  @Override
  public void clear() {
    super.clear();
    children.clear();
    attributes.clear();
  }

  @Override
  public ElementRecord getCopy() {
    ElementRecord copy = getDocumentRecord().createElementRecord(getNs(), getName());
    copy.setName(getName());
    copy.setNs(getNs());
    copy.setParent(currentParent);
    copy.setPrimaryWeight(currentPrimaryWeight);
    // primary weight is left open, will be set on apply
    return copy;
  }

  /**
   * Note: this method returns an element that contains copies of all descendants of this record.
   *
   * @param deep whether to include child nodes
   * @return if deep==false same as {@link #getCopy()} else the copy is extended by a copy of the
   *     subtree of child nodes
   */
  public ElementRecord getCopy(boolean deep) {
    ElementRecord copy = getCopy();
    if (!deep) return copy;
    AttributeRecord aCopy;
    for (AttributeRecord ar : attributes) {
      aCopy = ar.getCopy();
      aCopy.setParent(copy);
      copy.attributes.add(aCopy);
    }
    ElementRecord eCopy;
    for (ElementRecord er : children) {
      eCopy = er.getCopy(true);
      eCopy.setParent(copy);
      copy.children.add(eCopy);
    }
    return copy;
  }

  /**
   * Note, all returned records don't have children but applying would create a copied hierarchy of
   * this one's descendants to the passed ElementRecord parent.
   *
   * @param parent
   * @return a list with copies of all child nodes with <code>parent</code> as new parent, plus
   *     recursively copies of the whole subtree.
   */
  public List<NodeRecord> getCopiedSubtreeRecords(ElementRecord parent) {
    LinkedList<NodeRecord> records = new LinkedList<NodeRecord>();

    AttributeRecord aCopy;
    for (AttributeRecord ar : attributes) {
      aCopy = ar.getCopy();
      aCopy.setParent(parent);
      records.add(aCopy);
    }
    ElementRecord eCopy;
    for (ElementRecord er : children) {
      eCopy = er.getCopy();
      eCopy.setParent(parent);
      records.add(eCopy);
      records.addAll(er.getCopiedSubtreeRecords(eCopy));
    }
    return records;
  }

  protected void add(ElementRecord r) {
    children.add(r);
  }

  protected void add(AttributeRecord r) {
    attributes.add(r);
  }

  public void removeChild(NodeRecord r) {
    if (r.getNodeType().equals(NodeType.ATTR)) {
      attributes.remove(r);
    } else {
      this.children.remove(r);
    }
  }

  /* notification */

  /** Informs the ChildRecordChangeCache about a conflict. */
  public void notifyChildConflict(
      NodeRecord child, SetRecord previousState, SetRecord conflictSetRecord) {
    changeSupport.addChildRecordConflict(child, previousState, conflictSetRecord);
  }

  public void notifyChildChange(IRecord cause) {
    if (cause.getTarget().getNodeType().equals(NodeType.ATTR)) {
      changeSupport.attributeChange(cause);
    } else {
      changeSupport.childElementChange(cause);
    }
  }

  public void notifyChildChange(AttributeRecord cause) {
    changeSupport.attributeChange(cause);
  }

  public void notifyChildChange(ElementRecord cause) {
    changeSupport.childElementChange(cause);
  }

  public void addChildRecordChangeListener(ChildRecordChangeListener listener) {
    changeSupport.add(listener);
  }

  public void removeChildRecordChangeListener(ChildRecordChangeListener listener) {
    changeSupport.remove(listener);
  }

  @Override
  protected Float nextPrimaryWeight(ElementRecord newParent) {
    return newParent.getChildElements().nextPrimaryWeight();
  }

  @Override
  protected void setValuesTo(SetRecord setRecord) {
    if (isCircularRelationship(setRecord.getParentToChange())) {
      throw new XMLNotWellFormedException(setRecord, version != setRecord.getVersion());
    }
    super.setValuesTo(setRecord);
  }

  public boolean isCircularRelationship(ElementRecord newParent) {
    if (newParent == null) return false;
    if (currentParent == newParent) return false;

    if (this == newParent || getAllDescendantElements().contains(newParent)) return true;

    return false;
  }
}
