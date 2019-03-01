package saros.stf.server.bot.widget;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swtbot.swt.finder.ReferenceBy;
import org.eclipse.swtbot.swt.finder.SWTBotWidget;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotStyledText;
import org.hamcrest.SelfDescribing;
import saros.ui.widgets.chat.ChatControl;
import saros.ui.widgets.chat.parts.ChatInput;

/**
 * Represents a {@link ChatInput}widget in chat view. GUI peoples have changed the chat view's looks
 * using self defined SWT-components, which are obviously not supported by SWTBot. So i need to
 * define a corresponding SWTBot[widget name](in this case, SWTBbotInput) class to access every chat
 * widgets.
 *
 * @author lchen
 */
@SWTBotWidget(
    clasz = ChatInput.class,
    preferredName = "chatInput",
    referenceBy = {ReferenceBy.TEXT})
public class SarosSWTBotChatInput extends AbstractSWTBot<ChatInput> {

  private SWTBotStyledText styledText;

  /**
   * Constructs a new instance of this object.
   *
   * @param w the widget.
   * @throws WidgetNotFoundException if the widget is <code>null</code> or widget has been disposed.
   */
  public SarosSWTBotChatInput(ChatInput w) throws WidgetNotFoundException {
    this(w, null);
  }

  /**
   * Constructs a new instance of this object.
   *
   * @param w the widget.
   * @param description the description of the widget, this will be reported by {@link #toString()}
   * @throws WidgetNotFoundException if the widget is <code>null</code> or widget has been disposed.
   */
  public SarosSWTBotChatInput(ChatInput w, SelfDescribing description)
      throws WidgetNotFoundException {
    super(w, description);
    this.styledText = new SWTBotStyledText(getStyledText(w));
  }

  /**
   * Sets the text of the widget.
   *
   * @param text the text to be set.
   * @return the same instance.
   */
  public SarosSWTBotChatInput setText(final String text) {
    styledText.setText(text);
    return this;
  }

  public SarosSWTBotChatInput pressEnterKey() {
    // the ChatControl key listener ignores the mock keyboard event
    // styledText.pressShortcut(Keystrokes.CR);
    syncExec(
        new VoidResult() {
          @Override
          public void run() {
            try {
              String message = styledText.widget.getText().trim();

              ChatControl control = (ChatControl) widget.getParent().getParent();

              Method sendChatMessageMethod =
                  ChatControl.class.getDeclaredMethod("sendMessage", new Class<?>[] {String.class});

              sendChatMessageMethod.setAccessible(true);

              sendChatMessageMethod.invoke(control, message);
            } catch (Exception e) {
              log.error("sending chat message failed", e);
            }
          }
        });
    return this;
  }

  /**
   * Types the string in the text box.
   *
   * @param text the text to be typed.
   * @return the same instance.
   * @since 1.2
   */
  public SarosSWTBotChatInput typeText(final String text) {
    return typeText(text, SWTBotPreferences.TYPE_INTERVAL);
  }

  /**
   * Types the string in the text box.
   *
   * @param text the text to be typed.
   * @param interval the interval between consecutive key strokes.
   * @return the same instance.
   * @since 1.2
   */
  public SarosSWTBotChatInput typeText(final String text, int interval) {
    styledText.typeText(text, interval);
    return this;
  }

  private StyledText getStyledText(ChatInput widget) {
    try {
      Field text = ChatInput.class.getDeclaredField("text");
      text.setAccessible(true);
      return (StyledText) text.get(widget);
    } catch (Exception e) {
      throw new WidgetNotFoundException(
          "reflection failed, getting field 'text' on class " + ChatInput.class.getName(), e);
    }
  }
}
