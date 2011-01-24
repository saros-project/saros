package de.fu_berlin.inf.dpp.stf.server.sarosSWTBot.widgets;

import org.eclipse.swtbot.swt.finder.ReferenceBy;
import org.eclipse.swtbot.swt.finder.SWTBotWidget;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.utils.MessageFormat;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBot;
import org.hamcrest.SelfDescribing;

import de.fu_berlin.inf.dpp.ui.widgets.chatControl.parts.ChatInput;

/**
 * Represents a {@link ChatInput}widget in chat view. GUI peoples have changed
 * the chat view's looks using self defined SWT-components, which are obviously
 * not supported by SWTBot. So i need to define a corresponding SWTBot[widget
 * name](in this case, SWTBbotInput) class to access every chat widgets.
 * 
 * 
 * @author lchen
 */
@SWTBotWidget(clasz = ChatInput.class, preferredName = "chatInput", referenceBy = { ReferenceBy.TEXT })
public class SarosSWTBotChatInput extends AbstractSWTBot<ChatInput> {

    /**
     * Constructs a new instance of this object.
     * 
     * @param w
     *            the widget.
     * @throws WidgetNotFoundException
     *             if the widget is <code>null</code> or widget has been
     *             disposed.
     */
    public SarosSWTBotChatInput(ChatInput w) throws WidgetNotFoundException {
        this(w, null);
    }

    /**
     * Constructs a new instance of this object.
     * 
     * @param w
     *            the widget.
     * @param description
     *            the description of the widget, this will be reported by
     *            {@link #toString()}
     * @throws WidgetNotFoundException
     *             if the widget is <code>null</code> or widget has been
     *             disposed.
     */
    public SarosSWTBotChatInput(ChatInput w, SelfDescribing description)
        throws WidgetNotFoundException {
        super(w, description);
    }

    /**
     * Sets the text of the widget.
     * 
     * @param text
     *            the text to be set.
     * @return the same instance.
     */
    public SarosSWTBotChatInput setText(final String text) {
        waitForEnabled();
        asyncExec(new VoidResult() {
            public void run() {
                widget.setText(text);
            }
        });
        return this;
    }

    public SarosSWTBotChatInput pressEnterKey() {
        waitForEnabled();
        asyncExec(new VoidResult() {
            public void run() {
                widget.setFocus();
                // keyboard().pressShortcut(SWT.CR, SWT.LF);
                keyboard().pressShortcut(Keystrokes.LF);
            }
        });
        // pressShortcut(Keystrokes.LF);
        return this;
    }

    /**
     * Types the string in the text box.
     * 
     * @param text
     *            the text to be typed.
     * @return the same instance.
     * @since 1.2
     */
    public SarosSWTBotChatInput typeText(final String text) {
        return typeText(text, SWTBotPreferences.TYPE_INTERVAL);
    }

    /**
     * Types the string in the text box.
     * 
     * @param text
     *            the text to be typed.
     * @param interval
     *            the interval between consecutive key strokes.
     * @return the same instance.
     * @since 1.2
     */
    public SarosSWTBotChatInput typeText(final String text, int interval) {
        log.debug(MessageFormat.format(
            "Inserting text:{0} into text {1}", text, this)); //$NON-NLS-1$
        setFocus();
        keyboard().typeText(text, interval);
        return this;
    }

}
