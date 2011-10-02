package de.fu_berlin.inf.dpp.ui.widgets.chatControl.items;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * This composite is used to display a chat message.
 * 
 * @author bkahlert
 */
public class ChatLine extends Composite {
    protected StyledText text;

    public ChatLine(Composite parent, String message) {
        super(parent, SWT.NONE);
        this.setLayout(new FillLayout());

        text = new StyledText(this, SWT.WRAP);
        text.setText(message);
        text.setEditable(false);
    }

    @Override
    public void setBackground(Color color) {
        super.setBackground(color);
        text.setBackground(color);
    }

    public String getText() {
        return text.getText();
    }
}
