package de.fu_berlin.inf.dpp.stf.server.sarosSWTBot.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.utils.MessageFormat;
import org.eclipse.swtbot.swt.finder.utils.SWTUtils;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRadio;
import org.hamcrest.SelfDescribing;

/**
 * There are a bug in the SWTBotRadio. Problem is in SWTBotRadio.click().
 * SWTBotRadio.click() doesn't notify deselected button listeners. The current
 * SWTBotRadio.click() was providing a notify(SWT.selection) for the selected
 * button, but not for any button that was automatically deselected.
 * <p>
 * To fix the bug i need to rewrite the class SWTBotRadio.
 * 
 * @author lchen
 */
public class SarosSWTBotRadio extends SWTBotRadio {

    public SarosSWTBotRadio(Button w) throws WidgetNotFoundException {
        super(w);
    }

    public SarosSWTBotRadio(Button w, SelfDescribing description)
        throws WidgetNotFoundException {
        super(w, description);
    }

    /**
     * Selects the radio button.
     */
    @Override
    public SWTBotRadio click() {
        if (isSelected()) {
            log.debug(MessageFormat.format(
                "Widget {0} is already selected, not clicking again.", this)); //$NON-NLS-1$
            return this;
        }
        waitForEnabled();
        log.debug(MessageFormat.format("Clicking on {0}", this)); //$NON-NLS-1$
        asyncExec(new VoidResult() {
            public void run() {
                deselectOtherRadioButtons();
                log.debug(MessageFormat.format("Clicking on {0}", this)); //$NON-NLS-1$
                widget.setSelection(true);
            }

            /**
             * @see "http://dev.eclipse.org/viewcvs/index.cgi/org.eclipse.swt.snippets/src/org/eclipse/swt/snippets/Snippet224.java?view=co"
             */
            private void deselectOtherRadioButtons() {
                if (hasStyle(widget.getParent(), SWT.NO_RADIO_GROUP))
                    return;
                Widget[] siblings = SWTUtils.siblings(widget);
                for (Widget widget : siblings) {
                    if ((widget instanceof Button)
                        && hasStyle(widget, SWT.RADIO)
                        && ((Button) widget).getSelection() == true) {
                        ((Button) widget).setSelection(false);

                        widget.notifyListeners(SWT.Selection, createEvent());
                    }
                }
            }
        });
        notify(SWT.MouseEnter);
        notify(SWT.MouseMove);
        notify(SWT.Activate);
        notify(SWT.FocusIn);
        notify(SWT.MouseDown);
        notify(SWT.MouseUp);
        notify(SWT.Selection);
        notify(SWT.MouseHover);
        notify(SWT.MouseMove);
        notify(SWT.MouseExit);
        notify(SWT.Deactivate);
        notify(SWT.FocusOut);
        log.debug(MessageFormat.format("Clicked on {0}", this)); //$NON-NLS-1$
        return this;
    }
}
