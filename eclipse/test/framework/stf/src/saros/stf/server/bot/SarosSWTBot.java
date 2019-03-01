package de.fu_berlin.inf.dpp.stf.server.bot;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.allOf;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;

import de.fu_berlin.inf.dpp.stf.server.bot.widget.SarosSWTBotChatInput;
import de.fu_berlin.inf.dpp.stf.server.bot.widget.SarosSWTBotChatLine;
import de.fu_berlin.inf.dpp.stf.server.bot.widget.SarosSWTBotChatLinePartnerChangeSeparator;
import de.fu_berlin.inf.dpp.ui.widgets.chat.items.ChatLine;
import de.fu_berlin.inf.dpp.ui.widgets.chat.items.ChatLinePartnerChangeSeparator;
import de.fu_berlin.inf.dpp.ui.widgets.chat.parts.ChatInput;
import de.fu_berlin.inf.dpp.ui.widgets.chat.parts.SkypeStyleChatDisplay;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.WidgetResult;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.hamcrest.Matcher;

/**
 * SarosSWTBot is a {@link SWTBot} with capabilities for testing specific GUI items only defined for
 * Saros like chatInput and chatLine and fixing some methods defined by SWTBot which are not really
 * working yet .
 *
 * @author lchen
 */
public final class SarosSWTBot extends SWTBot {

  private static final Logger log = Logger.getLogger(SarosSWTBot.class);

  private Widget widget;

  public SarosSWTBot() {
    super();
  }

  public SarosSWTBot(Widget widget) {
    super(widget);
    this.widget = widget;
  }

  /**
   * @return a {@link SarosSWTBotChatInput} with the specified <code>none</code>.
   * @throws WidgetNotFoundException if the widget is not found or is disposed.
   */
  public SarosSWTBotChatInput chatInput() {
    return chatInput(0);
  }

  /**
   * @param index the index of the widget.
   * @return a {@link SarosSWTBotChatInput} with the specified <code>none</code>.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public SarosSWTBotChatInput chatInput(int index) {
    Matcher matcher = allOf(widgetOfType(ChatInput.class));
    return new SarosSWTBotChatInput((ChatInput) widget(matcher, index), matcher);
  }

  /**
   * @return a {@link SarosSWTBotChatLine} with the specified <code>none</code>.
   * @throws WidgetNotFoundException if the widget is not found or is disposed.
   */
  public SarosSWTBotChatLine chatLine() {
    return chatLine(0);
  }

  /**
   * @param index the index of the widget.
   * @return a {@link SarosSWTBotChatLine} with the specified <code>none</code>.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public SarosSWTBotChatLine chatLine(int index) {
    Matcher matcher = allOf(widgetOfType(ChatLine.class));
    return new SarosSWTBotChatLine((ChatLine) widget(matcher, index), matcher);
  }

  /** @return the last {@link SarosSWTBotChatLine} in the {@link SkypeStyleChatDisplay}. */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public SarosSWTBotChatLine lastChatLine() {
    Matcher matcher = allOf(widgetOfType(ChatLine.class));
    List<? extends Widget> allWidgets = widgets(matcher);

    Widget lastChatLine = allWidgets.get(allWidgets.size() - 1);
    return new SarosSWTBotChatLine((ChatLine) lastChatLine, matcher);
  }

  /**
   * @param regex the given regular expression, which represents the text of a chatLine'label.
   * @return a {@link SarosSWTBotChatLine} with the specified <code>regex</code>.
   * @throws WidgetNotFoundException if the widget is not found or is disposed.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public SarosSWTBotChatLine chatLine(final String regex) {
    Matcher matcher = allOf(widgetOfType(ChatLine.class));
    final List<? extends ChatLine> allWidgets = widgets(matcher);

    final ChatLine matchedChatLine =
        UIThreadRunnable.syncExec(
            new WidgetResult<ChatLine>() {
              @Override
              public ChatLine run() {
                ChatLine matchedChatLine = null;
                for (final ChatLine chatLine : allWidgets) {
                  log.debug("chatLine's text: " + chatLine.getText());
                  if (chatLine.getText().matches(regex)) {
                    matchedChatLine = chatLine;
                    break;
                  }
                }
                return matchedChatLine;
              }
            });
    return new SarosSWTBotChatLine(matchedChatLine, matcher);
  }

  /**
   * @return a {@link SarosSWTBotChatLinePartnerChangeSeparator} with the specified <code>none
   *     </code>.
   * @throws WidgetNotFoundException if the widget is not found or is disposed.
   */
  public SarosSWTBotChatLinePartnerChangeSeparator chatLinePartnerChangeSeparator() {
    return chatLinePartnerChangeSeparator(0);
  }

  /**
   * @param index the index of the widget.
   * @return a {@link SarosSWTBotChatLinePartnerChangeSeparator} with the specified <code>none
   *     </code>.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public SarosSWTBotChatLinePartnerChangeSeparator chatLinePartnerChangeSeparator(int index) {
    Matcher matcher = allOf(widgetOfType(ChatLinePartnerChangeSeparator.class));
    return new SarosSWTBotChatLinePartnerChangeSeparator(
        (ChatLinePartnerChangeSeparator) widget(matcher, index), matcher);
  }

  /**
   * @param username the user name.
   * @return a {@link SarosSWTBotChatLinePartnerChangeSeparator} with the specified <code>plainID
   *     </code>.
   * @throws WidgetNotFoundException if the widget is not found or is disposed.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public SarosSWTBotChatLinePartnerChangeSeparator chatLinePartnerChangeSeparator(
      final String username) {
    Matcher matcher = allOf(widgetOfType(ChatLinePartnerChangeSeparator.class));

    final List<? extends ChatLinePartnerChangeSeparator> allWidgets = widgets(matcher);

    final ChatLinePartnerChangeSeparator matchedSeparator =
        UIThreadRunnable.syncExec(
            new WidgetResult<ChatLinePartnerChangeSeparator>() {
              @Override
              public ChatLinePartnerChangeSeparator run() {
                ChatLinePartnerChangeSeparator matchedSeparator = null;
                for (final ChatLinePartnerChangeSeparator separator : allWidgets) {
                  if (separator.getUsername().equals(username)) {
                    matchedSeparator = separator;
                    break;
                  }
                }
                return matchedSeparator;
              }
            });
    return new SarosSWTBotChatLinePartnerChangeSeparator(matchedSeparator, matcher);
  }

  /**
   * This method does the same as {@link SWTBot#shells()}, but doesn't throw an exception if a shell
   * was already disposed.
   */
  @Override
  public SWTBotShell[] shells() {

    Shell[] shells = finder.getShells();
    ArrayList<SWTBotShell> result = new ArrayList<SWTBotShell>();
    for (Shell shell : shells) {

      if (!shell.isDisposed()) result.add(new SWTBotShell(shell));
      else log.warn("found disposed shell while iterating over all shells");
    }

    return result.toArray(new SWTBotShell[] {});
  }

  @Override
  public <T extends Widget> List<? extends T> widgets(Matcher<T> matcher) {
    return widget != null ? super.widgets(matcher, widget) : super.widgets(matcher);
  }
}
