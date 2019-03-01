package saros.stf.server.bot.widget;

import org.eclipse.swtbot.swt.finder.ReferenceBy;
import org.eclipse.swtbot.swt.finder.SWTBotWidget;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.results.StringResult;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBot;
import org.hamcrest.SelfDescribing;
import saros.ui.widgets.chat.items.ChatLinePartnerChangeSeparator;

/**
 * This represents a {@link ChatLinePartnerChangeSeparator} widget in chat view. GUI peoples have
 * changed the chat view's looks using self defined SWT-components, which are obviously not
 * supported by SWTBot. So i need to define a corresponding SWTBot[widget name](in this case,
 * SarosSWTBotChatLinePartnerChangeSeparator) class to access every chat widgets.
 *
 * @author lchen
 */
@SWTBotWidget(
    clasz = ChatLinePartnerChangeSeparator.class,
    preferredName = "chatLinePartnerChangeSeparator",
    referenceBy = {ReferenceBy.LABEL})
public class SarosSWTBotChatLinePartnerChangeSeparator
    extends AbstractSWTBot<ChatLinePartnerChangeSeparator> {

  /**
   * Constructs a new instance of this object.
   *
   * @param w the widget.
   * @throws WidgetNotFoundException if the widget is <code>null</code> or widget has been disposed.
   */
  public SarosSWTBotChatLinePartnerChangeSeparator(ChatLinePartnerChangeSeparator w)
      throws WidgetNotFoundException {
    this(w, null);
  }

  /**
   * Constructs a new instance of this object.
   *
   * @param w the widget.
   * @param description the description of the widget, this will be reported by {@link #toString()}
   * @throws WidgetNotFoundException if the widget is <code>null</code> or widget has been disposed.
   */
  public SarosSWTBotChatLinePartnerChangeSeparator(
      ChatLinePartnerChangeSeparator w, SelfDescribing description) throws WidgetNotFoundException {
    super(w, description);
  }

  /**
   * Gets the plainID of the given object.
   *
   * @return the username on the widget.
   */
  public String getPlainID() {
    return syncExec(
        new StringResult() {
          @Override
          public String run() {
            return widget.getUsername();
          }
        });
  }
}
