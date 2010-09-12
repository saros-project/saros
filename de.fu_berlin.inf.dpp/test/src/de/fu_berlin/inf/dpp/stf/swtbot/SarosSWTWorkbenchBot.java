package de.fu_berlin.inf.dpp.stf.swtbot;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.allOf;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withMnemonic;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withStyle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRadio;
import org.hamcrest.Matcher;

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
    @SuppressWarnings("unchecked")
    public SarosSWTBotRadio radio(String mnemonicText, int index) {
        Matcher matcher = allOf(widgetOfType(Button.class),
            withMnemonic(mnemonicText), withStyle(SWT.RADIO, "SWT.RADIO"));
        return new SarosSWTBotRadio((Button) widget(matcher, index), matcher);
    }
}
