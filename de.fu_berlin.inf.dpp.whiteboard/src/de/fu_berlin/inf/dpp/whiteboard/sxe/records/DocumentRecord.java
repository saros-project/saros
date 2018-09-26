package de.fu_berlin.inf.dpp.whiteboard.sxe.records;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import de.fu_berlin.inf.dpp.whiteboard.sxe.SXEController;
import de.fu_berlin.inf.dpp.whiteboard.sxe.exceptions.MissingRecordException;

/**
 * <p>
 * Implementation of a DOM document in context of Shared XML Editing XEP-0284
 * (SXE).
 * </p>
 * 
 * <p>
 * It stores a linked map with all new records, where the first one inserted
 * will be the root record.</br>
 * 
 * Records are inserted and removed during applying
 * {@link IRecord#apply(DocumentRecord)}, only be done by record classes in this
 * package.
 * </p>
 * 
 * @author jurke
 * 
 */
public class DocumentRecord {

    private static final Logger log = LogManager.getLogger(DocumentRecord.class);

    private final SXEController controller;

    /** linked map if NodeRecords contained in this document */
    protected LinkedHashMap<String, NodeRecord> newRecords;

    // TODO install document prolog etc

    public DocumentRecord(SXEController controller) {
        this.controller = controller;
        newRecords = new LinkedHashMap<String, NodeRecord>();
    }

    public SXEController getController() {
        return controller;
    }

    /**
     * Creates an ElementRecord using the factory of the controller.
     * 
     * @see de.fu_berlin.inf.dpp.whiteboard.sxe.records.ISXERecordFactory
     */
    public ElementRecord createElementRecord(String ns, String name) {
        ElementRecord e = controller.getRecordFactory().createElementRecord(
            this, ns, name);
        e.setNs(ns);
        e.setName(name);
        return e;
    }

    /**
     * Creates an AttributeRecord using the factory of the controller.
     * 
     * @see de.fu_berlin.inf.dpp.whiteboard.sxe.records.ISXERecordFactory
     */
    public AttributeRecord createAttributeRecord(String ns, String name,
        String chdata) {
        AttributeRecord a = controller.getRecordFactory()
            .createAttributeRecord(this, ns, name, chdata);
        a.setNs(ns);
        a.setName(name);
        a.setChdata(chdata);
        return a;
    }

    /**
     * <p>
     * Inserts a new NodeRecord to the document.
     * </p>
     * 
     * <p>
     * To be called during applying of a NodeRecord after adding it to the tree.
     * </p>
     * 
     * @param record
     */
    void insert(NodeRecord record) {
        if (record.getDocumentRecord() != this) {
            log.warn("Added record from other docuemnt");
        }
        log.debug("Added Node: " + record);
        record.setDocumentRecord(this);
        newRecords.put(record.getRid(), record);
    }

    /**
     * Method to use when trying to create IRecords from the serializable
     * RecordDataObject.
     * 
     * @param rid
     *            RecordID of the record
     * @return the record with the RID rid
     * @throws MissingRecordException
     *             if there is no respective NodeRecord
     */
    public NodeRecord getRecordById(String rid) throws MissingRecordException {
        NodeRecord r = newRecords.get(rid);
        if (r == null)
            throw new MissingRecordException(rid);
        return r;
    }

    /**
     * Method to use when trying to create IRecords from the serializable
     * RecordDataObject.
     * 
     * @param rid
     *            RecordID of the record
     * @return the record with the RID rid
     * @throws MissingRecordException
     *             if there is no respective ElementRecord (a AttributeRecord
     *             might exist though)
     */
    public ElementRecord getElementRecordById(String rid)
        throws MissingRecordException {
        try {
            return (ElementRecord) getRecordById(rid);
        } catch (ClassCastException e) {
            throw new MissingRecordException(rid,
                "rid does not correspond to ElementRecord");
        }
    }

    /**
     * clears history and contained records
     */
    public void clear() {
        newRecords.clear();
    }

    /**
     * <p>
     * Provides a list of IRecord that defines the current state of this shared
     * XML document that can be used for start synchronization.
     * </p>
     * <p>
     * This may be the set of NodeRecords with their history (SetRecords)
     * </p>
     * 
     * @return all records defining the current state of the XML document
     */
    public List<IRecord> getState() {
        List<IRecord> records = new LinkedList<IRecord>();
        for (NodeRecord r : newRecords.values()) {
            records.add(r);
            records.addAll(r.getSetRecords());
        }
        return records;
    }

    public boolean contains(NodeRecord r) {
        return newRecords.containsKey(r.getRid());
    }

    public boolean isEmpty() {
        return newRecords.isEmpty();
    }

    /**
     * 
     * @return the document root ElementRecord
     */
    public ElementRecord getRoot() {
        // TODO loop with return?
        for (NodeRecord r : newRecords.values())
            return (ElementRecord) r;
        return null;
    }

}
