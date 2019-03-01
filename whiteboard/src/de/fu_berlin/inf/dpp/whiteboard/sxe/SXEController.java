package de.fu_berlin.inf.dpp.whiteboard.sxe;

import de.fu_berlin.inf.dpp.whiteboard.sxe.constants.RecordType;
import de.fu_berlin.inf.dpp.whiteboard.sxe.constants.SXEMessageType;
import de.fu_berlin.inf.dpp.whiteboard.sxe.exceptions.MissingRecordException;
import de.fu_berlin.inf.dpp.whiteboard.sxe.exceptions.XMLNotWellFormedException;
import de.fu_berlin.inf.dpp.whiteboard.sxe.net.ISXETransmitter;
import de.fu_berlin.inf.dpp.whiteboard.sxe.net.SXEMessage;
import de.fu_berlin.inf.dpp.whiteboard.sxe.net.SXESession;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.DocumentRecord;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.IRecord;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.ISXERecordFactory;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.SetRecord;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.serializable.RecordDataObject;
import de.fu_berlin.inf.dpp.whiteboard.sxe.util.SXEUtils;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * Central part of an SXE session that maintains the document.
 *
 * <p>To its functionalities belongs:
 *
 * <ul>
 *   <li>maintain state of the controller during session negotiation
 *   <li>applying a local list of records
 *   <li>applying a remote SXE message
 *   <li>queue incoming messages while synchronizing and apply them on finish
 *   <li>queue out-of-order (non-causal-ready) records to apply them when possible
 * </ul>
 *
 * @author jurke
 */
public class SXEController extends AbstractSXEMessageHandler {

  private static final Logger log = Logger.getLogger(SXEController.class);

  public enum State {
    DISCONNECTED,
    INIT,
    CONNECTING,
    CONNECTED;
  }

  /*
   * This might be changed to a list to maintain multiple documents. However,
   * SXE is specified to handle multiple documents by means of multiple
   * sessions.
   */
  protected DocumentRecord document;

  /** a factory to create custom record instances */
  protected ISXERecordFactory recordFactory;

  protected SXESession session = null;

  protected State connectionState;

  protected ISXETransmitter transmitter;

  protected List<SXEMessage> queuedMessagesWhileSynchronizing = new LinkedList<SXEMessage>();

  /*
   * TODO remove after time X and inform peers or request missing record from
   * peer
   */
  /** A target of a record is missing and it is supposed to be "on the way" */
  protected MappedList<RecordDataObject> unappliedMissingTargetRecords =
      new MappedList<RecordDataObject>();
  /** A predecessor of these SetRecords is missing */
  protected MappedList<SetRecord> unappliedBigVersionSetRecords = new MappedList<SetRecord>();

  public SXEController(ISXERecordFactory factory) {
    this.recordFactory = factory;
    connectionState = State.DISCONNECTED;
    initDocument();
  }

  @Override
  public DocumentRecord getDocumentRecord() {
    return document;
  }

  protected void initDocument() {
    document = recordFactory.createDocument(this);
    recordFactory.createRoot(document).apply(document);
  }

  public void setDisconnected() {
    connectionState = State.DISCONNECTED;
    session = null;
  }

  public void clear() {
    document.clear();
    initDocument();
  }

  public void initNetwork(ISXETransmitter transmitter) {
    this.transmitter = transmitter;
    connectionState = State.INIT;
  }

  /** Initializes the session as host and starts to receive records from NOW. */
  public void startSession() {
    session = new SXESession();

    log.debug(prefix() + "initialize session at inviter side");

    transmitter.installRecordReceiver(this);

    connectionState = State.CONNECTED;
  }

  /**
   * Accepts a remote state and applies it to this controller, discarding the previous content.
   * </br>
   *
   * <p>Queues messages will be applied afterwards and the lists is cleared.
   *
   * @param message the state message
   */
  public void startSession(SXEMessage message) {
    log.debug(prefix() + "receive state and start session at invitee side");

    if (!(connectionState == State.CONNECTING)) {
      log.error("Received state while not in connecting state");
      return;
    }

    document.clear();

    if (message.getRecords().isEmpty()) {
      log.warn("Received empty list of state records. Unsupported. ");
      assert (false);
    }

    for (RecordDataObject r : message.getRecords()) {
      apply(r);
    }

    connectionState = State.CONNECTED;

    if (unappliedBigVersionSetRecords.size() != 0)
      log.warn(prefix() + "Some set records haven't been applied during start synchronization");

    if (this.unappliedMissingTargetRecords.size() != 0)
      log.warn(prefix() + "Some records couldn't be inserted during start synchronization");

    for (SXEMessage m : queuedMessagesWhileSynchronizing) {
      for (RecordDataObject rdo : m.getRecords()) {
        apply(rdo);
      }
    }
    queuedMessagesWhileSynchronizing.clear();

    fireStateMessageApplied(message, document.getRoot());

    notifyLocalListeners();
  }

  /**
   * This method attempts to convert a RecordDataObject to the corresponding record that will be
   * applied then.</br>
   *
   * <p>If a RecordDataObject is not casual ready (parent, target or predecessor SetRecord is
   * missing), it will be queued. The only exception are SetRecords during start synchronization
   * where missing records (==version) are expected due to possible previous conflicts.
   *
   * @param rdo
   * @return whether the RecordDataObject could be applied
   */
  protected boolean apply(RecordDataObject rdo) {
    try {
      // if contained, ignore
      if (rdo.isAlreadyApplied(document)) {
        log.debug("Doubled " + rdo.getRecordType() + " record. Target RID " + rdo.getTargetRid());
        return false;
      }

      // try to create the record
      IRecord r = rdo.getIRecord(document);

      /*
       * check if SetRecord is casual ready, this is not given if the
       * version difference is unequal 1
       */
      if (r.getRecordType() == RecordType.SET) {

        int versionDiff = ((SetRecord) r).getVersionDifference();

        if (versionDiff != 1)
          if (handleVersionDiff((SetRecord) r, connectionState == State.CONNECTING))
            // on start synchronization, let it be seen as if
            // applied properly
            return connectionState == State.CONNECTING;
      }

      boolean applied = apply(r);

      if (!applied) {
        /*
         * At this stage must be an application error that should be
         * solved with high priority
         */
        throw new RuntimeException("local apply failed");
      }

      return applied;
    } catch (MissingRecordException e) {
      log.debug(
          "Missing record: " + e.getMissingRid() + " for " + rdo + ". Queued for the moment.");
      /*
       * this record is not casual ready yet but will be or it's an error
       */
      // TODO request record after time or re-synchronize
      unappliedMissingTargetRecords.put(e.getMissingRid(), rdo);
      return false;
    } catch (XMLNotWellFormedException xmlE) {
      // TODO investigate how to solve this
      log.warn("Received record that caused a non well-formed XML due to a conflict!");

      applyNullRecord(xmlE.getCausingSetRecord());
      return false;
    } catch (Exception e) {
      // TODO delete on peers or re-synchronize
      log.error("Unexpected exception when applying record: " + rdo, e);
      return false;
    }
  }

  protected void applyNullRecord(SetRecord toConflict) {
    SetRecord setRecord = new SetRecord(toConflict.getTarget(), toConflict.getVersion());
    // let's apply the conflict locally, too, to ensure a same version
    setRecord.apply(document);
  }

  /**
   * This method sends a SetRecord to all peers that conflicts with the provided one so that this
   * will be reverted to a previous version. This may be necessary, if due to concurrent SetRecords
   * the XML document would not be well-formed anymore due to a circular relationship.
   *
   * @param toConflict
   */
  protected void applyLocallyAndsentConflictingSetRecord(SetRecord toConflict) {
    LinkedList<IRecord> records = new LinkedList<IRecord>();
    SetRecord setRecord = new SetRecord(toConflict.getTarget(), toConflict.getVersion());
    // let's apply the conflict locally, too, to ensure a same version
    setRecord.apply(document);
    records.add(setRecord);
    commitRecords(records);
  }

  /**
   * Method to take actions if a remote SetRecord has wrong version.
   *
   * <p>This may be expected behavior:</br>
   *
   * <p>During start synchronization SetRecords may be missing (received version is too big) because
   * they were discarded remotely due to previous conflicts.</br>
   *
   * <p>In a session, there may arise conflicts (received version is too small).<br>
   * Everything else is an error, while no action has to be taken during start synchronization but
   * remote records must be queued to wait for the SetRecord with the missing version to arrive.
   *
   * @param r
   * @param startSync
   * @return whether the difference is handled and the record should not be applied
   */
  protected boolean handleVersionDiff(SetRecord r, boolean startSync) {
    int versionDiff = r.getVersionDifference();

    if (versionDiff < 1) {
      /*
       * Remote version is too small, means we already received a latter
       * SetRecord
       */
      if (startSync) {
        /*
         * do nothing, must be an inconvenience while creating the state
         * record list on the peer side
         */
        log.warn("Received an out-dated SetRecord during start synchronization. Ignored.");
        return true;
      } else {
        // must be an conflict, apply it
        return false;
      }
    } else if (versionDiff > 1) {
      /*
       * Remote version is too high, there have been SetRecords in between
       * we don't have
       */
      if (startSync) {
        /*
         * set the target's version to the correct one because we expect
         * the gap occurred due to a previous conflict.
         */
        log.debug("Received advanced SetRecord during start synchronization. Versions skipped");
        r.getTarget().repairVersion(r.getVersion() - 1);
        return false;
      } else {
        /*
         * Queue the record because there might be a record on the way.
         */
        log.debug(
            "Set record with out-of-order version. Should be "
                + (r.getTarget().getVersion() + 1)
                + " but is "
                + (r).getVersion()
                + ", set-record: "
                + r
                + ". Queued for the moment.");

        unappliedBigVersionSetRecords.put(r.getTarget().getRid(), r);
        return true;
      }
    }
    return false;
  }

  /**
   * Checks whether the provided record makes any queue record casual ready.
   *
   * @param cause
   */
  protected void applyQueuedRecords(IRecord cause) {
    if (cause.getRecordType() == RecordType.NEW) {

      List<RecordDataObject> rdos =
          unappliedMissingTargetRecords.remove(cause.getTarget().getRid());

      if (rdos != null)
        for (RecordDataObject rdo : rdos) {
          log.debug("Applying queued record " + rdo);
          apply(rdo);
        }

    } else if (cause.getRecordType() == RecordType.SET) {

      List<SetRecord> setRecords = unappliedBigVersionSetRecords.remove(cause.getTarget().getRid());
      if (setRecords != null)
        for (SetRecord sr : setRecords) {
          log.debug("Applying queued set-record " + sr);
          apply(sr);
        }
    }
  }

  /**
   * Attempts to apply the passed record. If successful it will apply all queued records that became
   * casual ready now.
   *
   * @param record
   * @return whether the passed record could be applied
   */
  protected boolean apply(IRecord record) {
    boolean applied = record.apply(document);

    if (applied) {
      applyQueuedRecords(record);
    }

    return applied;
  }

  /**
   * Attempts to apply the records of a remote message
   *
   * @param message message containing records to apply
   */
  public void executeRemoteRecords(SXEMessage message) {
    if (connectionState == State.CONNECTING) {
      queuedMessagesWhileSynchronizing.add(message);
      return;
    }

    for (RecordDataObject r : message.getRecords()) {
      apply(r);
    }

    fireRecordMessageApplied(message);

    notifyLocalListeners();
  }

  /**
   * This method is to be used by commands to apply locally created records.
   *
   * <p>It will apply them, notify local listeners and if applying was successful transmit them to
   * the peers.
   *
   * @param records
   */
  public final void executeAndCommit(List<IRecord> records) {
    try {

      Iterator<IRecord> it = records.iterator();

      IRecord current;

      // apply one after the other
      while (it.hasNext()) {
        current = it.next();
        // and don't commit if not executed
        boolean applied = false;
        try {
          applied = apply(current);
          /*
           * i.e. may happen on undo a creation where one record of
           * the selection got deleted by a peer.
           *
           * Or if a record is trivial - does not change anything.
           */

          if (!applied) log.trace("Did not apply local record: " + current);
        } catch (XMLNotWellFormedException e) {
          log.debug(
              "Could not apply local record because would result in a non well-formed XML: "
                  + current);
        }

        if (!applied) {
          // if not applied, don't send to peers
          it.remove();
        }
      }

      // commit and notify the whole bunch of records
      notifyLocalListeners();

      commitRecords(records);

    } catch (Exception e) {
      /*
       * A runtime Exception here means a critical error in the data
       * structure or the command and should be addressed with high
       * priority.
       *
       * However, nothing is changed locally, nothing sent to peers.
       */
      log.error("Could not apply local command because of an error: ", e);
    }
  }

  /**
   * Pack the records in a message and send it to all peers in the session
   *
   * @param recordsToSend
   */
  public void commitRecords(List<IRecord> recordsToSend) {

    if (!isConnected()) return;

    try {
      SXEMessage message = session.getNextMessage(SXEMessageType.RECORDS);
      List<RecordDataObject> rdos = SXEUtils.toDataObjects(recordsToSend);
      message.setRecords(rdos);
      transmitter.sendAsync(message);

      fireMessageSent(message);

    } catch (Exception e) {
      log.error("Error sending operation", e);
      throw new RuntimeException(e);
    }
  }

  public boolean isConnected() {
    return connectionState == State.CONNECTED;
  }

  public ISXETransmitter getTransmitter() {
    return transmitter;
  }

  public String prefix() {
    return "SXE (" + session + "): ";
  }

  public ISXERecordFactory getRecordFactory() {
    return recordFactory;
  }

  public SXESession getSession() {
    return session;
  }

  /**
   * Attempts to change the state to {@link State#CONNECTING}. If successful, this will initialize
   * to receive remote records from NOW.
   *
   * @param session
   * @return whether the state change was successful
   */
  public boolean switchToConnectingState(SXESession session) {
    if (connectionState != State.INIT) {
      log.debug("Cannot change from " + connectionState + " to " + State.CONNECTING);
      return false;
    }
    log.debug("Whiteboard state changed from " + connectionState + " to " + State.CONNECTING);
    connectionState = State.CONNECTING;
    this.session = session;

    transmitter.installRecordReceiver(this);

    return true;
  }

  public State getState() {
    return connectionState;
  }

  public void dispose() {
    document.clear();
    queuedMessagesWhileSynchronizing.clear();
  }

  /**
   * Helper class to map a String to a list of T
   *
   * @author jurke
   */
  protected static class MappedList<T> extends LinkedHashMap<String, List<T>> {
    private static final long serialVersionUID = 1L;

    public List<T> put(String key, T value) {
      List<T> l = get(key);
      if (l == null) {
        l = new LinkedList<T>();
        super.put(key, l);
      }
      l.add(value);
      return l;
    }

    @Override
    public List<T> put(String key, List<T> value) {
      List<T> l = get(key);
      if (l == null) return super.put(key, value);
      else l.addAll(value);
      return l;
    }
  }
}
