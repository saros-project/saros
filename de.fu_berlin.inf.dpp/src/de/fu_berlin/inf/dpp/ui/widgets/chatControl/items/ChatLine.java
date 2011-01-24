package de.fu_berlin.inf.dpp.ui.widgets.chatControl.items;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * This composite is used to display a chat message.
 * 
 * @author bkahlert
 */
public class ChatLine extends Composite {
    protected Label label;

    public ChatLine(Composite parent, String message) {
        super(parent, SWT.NONE);
        this.setLayout(new FillLayout());

        this.label = new Label(this, SWT.WRAP);
        this.label.setText(message);
    }

    @Override
    public void setBackground(Color color) {
        super.setBackground(color);
        this.label.setBackground(color);
    }

    public String getText() {
        return this.label.getText();
    }
}
