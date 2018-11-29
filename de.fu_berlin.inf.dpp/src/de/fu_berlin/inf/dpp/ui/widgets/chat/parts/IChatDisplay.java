package de.fu_berlin.inf.dpp.ui.widgets.chat.parts;

import de.fu_berlin.inf.dpp.ui.widgets.chat.events.IChatDisplayListener;
import java.util.Date;
import org.eclipse.swt.graphics.Color;

/** Interface for adding, removing, and modifying chat content. */
public interface IChatDisplay {

  /**
   * Adds the given listeners to this <code>IChatDisplay</code>.
   *
   * @param listener the listener to add
   */
  public void addChatDisplayListener(IChatDisplayListener listener);

  /**
   * Removes the given listeners from this <code>IChatDisplay</code>.
   *
   * @param listener the listener to removed
   */
  public void removeChatDisplayListener(IChatDisplayListener listener);

  /**
   * Adds a message to the current content of this <code>IChatDisplay</code>.
   *
   * @param entity a reference to originator of the message
   * @param name the name of the entity
   * @param message the message to add
   * @param time the time to display belong the message, e.g the time the message was received
   * @param color a color hint which should be used to apply additional visual appearance to the
   *     entity name
   */
  public void addMessage(Object entity, String name, String message, Date time, Color color);

  /** Clears the current content of this <code>IChatDisplay</code>. */
  public void clear();

  /**
   * Updates the entity name for all message entries. Optional operation.
   *
   * @param entity the entity to update
   * @param name the new name for the entity
   */
  public void updateEntityName(Object entity, String name);

  /**
   * Updates the entity color for all message entries. Optional operation.
   *
   * @param entity the entity to update
   * @param color the new color for the entity
   */
  public void updateEntityColor(Object entity, Color color);
}
