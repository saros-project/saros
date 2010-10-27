package de.fu_berlin.inf.dpp.stf.swtbot;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.allOf;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withMnemonic;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withStyle;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRadio;
import org.hamcrest.Matcher;

import de.fu_berlin.inf.dpp.stf.sarosswtbot.SWTBotChatInput;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.SWTBotChatLine;
import de.fu_berlin.inf.dpp.ui.chat.chatControl.items.ChatLine;
import de.fu_berlin.inf.dpp.ui.chat.chatControl.parts.ChatInput;

public class SarosSWTWorkbenchBot extends SWTWorkbenchBot {

    @Override
    public SarosSWTBotRadio radio(String mnemonicText) {
        return radio(mnemonicText, 0);
    }

    /**
     * @param mnemonicText
     *            the mnemonicText on the widget.
     * @param index
     *            the index of the widget.
     * @return a {@link SWTBotRadio} with the specified
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
     * @return a {@link SWTBotChatInput} with the specified <code>none</code>.
     */
    public SWTBotChatInput chatInput() {
        return chatInput(0);
    }

    /**
     * @param index
     *            the index of the widget.
     * @return a {@link SWTBotChatInput} with the specified <code>none</code>.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public SWTBotChatInput chatInput(int index) {
        Matcher matcher = allOf(widgetOfType(ChatInput.class));
        return new SWTBotChatInput((ChatInput) widget(matcher, 0), matcher);
    }

    /**
     * @return a {@link SWTBotChatLine} with the specified <code>text</code>.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public SWTBotChatLine lastChatLine() {
        Matcher matcher = allOf(widgetOfType(ChatLine.class));
        List<? extends Widget> allWidgets = widgets(matcher);
        Widget lastChatLine = allWidgets.get(allWidgets.size() - 1);
        return new SWTBotChatLine((ChatLine) lastChatLine, matcher);
    }
}
