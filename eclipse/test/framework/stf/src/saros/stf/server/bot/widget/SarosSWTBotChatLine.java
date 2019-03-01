package saros.stf.server.bot.widget;

import org.eclipse.swtbot.swt.finder.ReferenceBy;
import org.eclipse.swtbot.swt.finder.SWTBotWidget;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBot;
import org.hamcrest.SelfDescribing;
import saros.ui.widgets.chat.items.ChatLine;

/**
 * This represents a {@link ChatLine} widget in chat view. GUI peoples have changed the chat view's
 * looks using self defined SWT-components, which are obviously not supported by SWTBot. So i need
 * to define a corresponding SWTBot[widget name](in this case, SWTBbotLine) class to access every
 * chat widgets.
 *
 * @author lchen
 */
@SWTBotWidget(
    clasz = ChatLine.class,
    preferredName = "chatLine",
    referenceBy = {ReferenceBy.TEXT})
public class SarosSWTBotChatLine extends AbstractSWTBot<ChatLine> {

  /**
   * Constructs a new instance of this object.
   *
   * @param w the widget.
   * @throws WidgetNotFoundException if the widget is <code>null</code> or widget has been disposed.
   */
  public SarosSWTBotChatLine(ChatLine w) throws WidgetNotFoundException {
    this(w, null);
  }

  /**
   * Constructs a new instance of this object.
   *
   * @param w the widget.
   * @param description the description of the widget, this will be reported by {@link #toString()}
   * @throws WidgetNotFoundException if the widget is <code>null</code> or widget has been disposed.
   */
  public SarosSWTBotChatLine(ChatLine w, SelfDescribing description)
      throws WidgetNotFoundException {
    super(w, description);
  }
}
