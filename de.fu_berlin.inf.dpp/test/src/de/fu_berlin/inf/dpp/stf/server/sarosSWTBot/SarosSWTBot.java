package de.fu_berlin.inf.dpp.stf.server.sarosSWTBot;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.allOf;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withMnemonic;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withStyle;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.WidgetResult;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.hamcrest.Matcher;

import de.fu_berlin.inf.dpp.stf.server.sarosSWTBot.widgets.SarosSWTBotChatInput;
import de.fu_berlin.inf.dpp.stf.server.sarosSWTBot.widgets.SarosSWTBotChatLine;
import de.fu_berlin.inf.dpp.stf.server.sarosSWTBot.widgets.SarosSWTBotChatLinePartnerChangeSeparator;
import de.fu_berlin.inf.dpp.stf.server.sarosSWTBot.widgets.SarosSWTBotRadio;
import de.fu_berlin.inf.dpp.ui.widgets.chatControl.items.ChatLine;
import de.fu_berlin.inf.dpp.ui.widgets.chatControl.items.ChatLinePartnerChangeSeparator;
import de.fu_berlin.inf.dpp.ui.widgets.chatControl.parts.ChatDisplay;
import de.fu_berlin.inf.dpp.ui.widgets.chatControl.parts.ChatInput;

/**
 * SarosSWTBot is a {@link SWTWorkbenchBot} with capabilities for testing
 * specific GUI items only defined for saros like chatInput and chatLine and
 * fixing some methods defined by SWTBot which are not really working yet .
 * 
 * @author lchen
 */
public class SarosSWTBot extends SWTWorkbenchBot {
    private static final transient Logger log = Logger
        .getLogger(SarosSWTBot.class);

    private static transient SarosSWTBot self;

    /**
     * {@link SarosSWTBot} is a singleton
     */
    public static SarosSWTBot getInstance() {
        if (self != null)
            return self;
        self = new SarosSWTBot();
        return self;
    }

    /**
     * @param mnemonicText
     *            the mnemonicText on the widget.
     * @return a {@link SarosSWTBotRadio} with the specified
     *         <code>mnemonicText</code>.
     * @throws WidgetNotFoundException
     *             if the widget is not found or is disposed.
     */
    @Override
    public SarosSWTBotRadio radio(String mnemonicText) {
        return radio(mnemonicText, 0);
    }

    /**
     * @param mnemonicText
     *            the mnemonicText on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link SarosSWTBotRadio} with the specified
     *         <code>mnemonicText</code>.
     */
    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public SarosSWTBotRadio radio(String mnemonicText, int index) {
        Matcher matcher = allOf(widgetOfType(Button.class),
            withMnemonic(mnemonicText), withStyle(SWT.RADIO, "SWT.RADIO"));
        return new SarosSWTBotRadio((Button) widget(matcher, index), matcher);
    }

    /**
     * @return a {@link SarosSWTBotChatInput} with the specified
     *         <code>none</code>.
     * @throws WidgetNotFoundException
     *             if the widget is not found or is disposed.
     */
    public SarosSWTBotChatInput chatInput() {
        return chatInput(0);
    }

    /**
     * @param index
     *            the index of the widget.
     * @return a {@link SarosSWTBotChatInput} with the specified
     *         <code>none</code>.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public SarosSWTBotChatInput chatInput(int index) {
        Matcher matcher = allOf(widgetOfType(ChatInput.class));
        return new SarosSWTBotChatInput((ChatInput) widget(matcher, 0), matcher);
    }

    /**
     * @return a {@link SarosSWTBotChatLine} with the specified
     *         <code>none</code>.
     * @throws WidgetNotFoundException
     *             if the widget is not found or is disposed.
     */
    public SarosSWTBotChatLine chatLine() {
        return chatLine(0);
    }

    /**
     * @param index
     *            the index of the widget.
     * @return a {@link SarosSWTBotChatLine} with the specified
     *         <code>none</code>.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public SarosSWTBotChatLine chatLine(int index) {
        Matcher matcher = allOf(widgetOfType(ChatLine.class));
        return new SarosSWTBotChatLine((ChatLine) widget(matcher, 0), matcher);
    }

    /**
     * @return the last {@link SarosSWTBotChatLine} in the {@link ChatDisplay}.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public SarosSWTBotChatLine lastChatLine() {
        Matcher matcher = allOf(widgetOfType(ChatLine.class));
        List<? extends Widget> allWidgets = widgets(matcher);
        Widget lastChatLine = allWidgets.get(allWidgets.size() - 1);
        return new SarosSWTBotChatLine((ChatLine) lastChatLine, matcher);
    }

    /**
     * @param regex
     *            the given regular expression, which represents the text of a
     *            chatLine'label.
     * @return a {@link SarosSWTBotChatLine} with the specified
     *         <code>regex</code>.
     * @throws WidgetNotFoundException
     *             if the widget is not found or is disposed.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public SarosSWTBotChatLine chatLine(final String regex) {
        Matcher matcher = allOf(widgetOfType(ChatLine.class));
        final List<? extends ChatLine> allWidgets = widgets(matcher);

        final ChatLine matchedChatLine = UIThreadRunnable
            .syncExec(new WidgetResult<ChatLine>() {
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
     * @return a {@link SarosSWTBotChatLinePartnerChangeSeparator} with the
     *         specified <code>none</code>.
     * @throws WidgetNotFoundException
     *             if the widget is not found or is disposed.
     */
    public SarosSWTBotChatLinePartnerChangeSeparator chatLinePartnerChangeSeparator() {
        return chatLinePartnerChangeSeparator(0);
    }

    /**
     * @param index
     *            the index of the widget.
     * @return a {@link SarosSWTBotChatLinePartnerChangeSeparator} with the
     *         specified <code>none</code>.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public SarosSWTBotChatLinePartnerChangeSeparator chatLinePartnerChangeSeparator(
        int index) {
        Matcher matcher = allOf(widgetOfType(ChatLinePartnerChangeSeparator.class));
        return new SarosSWTBotChatLinePartnerChangeSeparator(
            (ChatLinePartnerChangeSeparator) widget(matcher, 0), matcher);
    }

    /**
     * @param plainID
     *            the user name.
     * @return a {@link SarosSWTBotChatLinePartnerChangeSeparator} with the
     *         specified <code>plainID</code>.
     * @throws WidgetNotFoundException
     *             if the widget is not found or is disposed.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public SarosSWTBotChatLinePartnerChangeSeparator chatLinePartnerChangeSeparator(
        final String plainID) {
        Matcher matcher = allOf(widgetOfType(ChatLinePartnerChangeSeparator.class));
        final List<? extends ChatLinePartnerChangeSeparator> allWidgets = widgets(matcher);

        final ChatLinePartnerChangeSeparator matchedSeparator = UIThreadRunnable
            .syncExec(new WidgetResult<ChatLinePartnerChangeSeparator>() {
                public ChatLinePartnerChangeSeparator run() {
                    ChatLinePartnerChangeSeparator matchedSeparator = null;
                    for (final ChatLinePartnerChangeSeparator separator : allWidgets) {
                        log.debug("separator's user name: "
                            + separator.getPlainID());
                        if (separator.getPlainID().equals(plainID)) {
                            matchedSeparator = separator;
                            break;
                        }
                    }
                    return matchedSeparator;
                }
            });
        return new SarosSWTBotChatLinePartnerChangeSeparator(matchedSeparator,
            matcher);
    }

    /**
     * This method does the same as {@link SWTWorkbenchBot#shells()}, but
     * doesn't throw an exception if a shell was already disposed.
     */
    @Override
    public SWTBotShell[] shells() {
        super.shells();
        Shell[] shells = finder.getShells();
        ArrayList<SWTBotShell> result = new ArrayList<SWTBotShell>();
        for (Shell shell : shells) {
            if (!shell.isDisposed())
                result.add(new SWTBotShell(shell));
        }
        return result.toArray(new SWTBotShell[] {});
    }
}
