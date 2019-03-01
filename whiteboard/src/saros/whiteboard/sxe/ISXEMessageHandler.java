package saros.whiteboard.sxe;

import saros.whiteboard.sxe.net.SXEMessage;
import saros.whiteboard.sxe.records.DocumentRecord;
import saros.whiteboard.sxe.records.ElementRecord;

/**
 * Interface for message handling notification especially for a client like the GUI.
 *
 * @author jurke
 */
public interface ISXEMessageHandler {

  /**
   * Informs about sent and applied messages. </br>
   *
   * <p>Note: only notifies about {@link saros.whiteboard.sxe.constants.SXEMessageType#RECORDS}
   * messages
   *
   * @author jurke
   */
  public interface MessageListener {

    public void sxeMessageSent(SXEMessage message);

    public void sxeRecordMessageApplied(SXEMessage message);

    public void sxeStateMessageApplied(SXEMessage message, ElementRecord root);
  }

  public abstract class MessageAdapter implements MessageListener {

    @Override
    public void sxeMessageSent(SXEMessage message) {
      ;
    }

    @Override
    public void sxeRecordMessageApplied(SXEMessage message) {
      ;
    }

    @Override
    public void sxeStateMessageApplied(SXEMessage message, ElementRecord root) {
      ;
    }
  }

  /**
   * This listener notifies about the notification due to the ChildRecordChangeCache.
   *
   * <p>This was added as a fix for bad GEF listener performance, especially respective selection
   * handles.
   *
   * @author jurke
   * @see saros.whiteboard.sxe.records.ChildRecordChangeCache
   * @see saros.whiteboard.gef.editor.WhiteboardEditor#createGraphicalViewer(Composite)
   */
  public interface NotificationListener {

    public void beforeNotification();

    public void afterNotificaion();
  }

  public void addMessageListener(MessageListener listener);

  public void removeMessageListener(MessageListener listener);

  public void addNotificationListener(NotificationListener listener);

  public void removeNotificationListener(NotificationListener listener);

  public DocumentRecord getDocumentRecord();
}
