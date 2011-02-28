package de.fu_berlin.inf.dpp.ui.widgets.enhancer;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import de.fu_berlin.inf.dpp.util.ColorUtils;

/**
 * Class which displays a default text if the user has not entered own input.
 */
public class EmptyText {
    protected Text control;
    protected String defaultText;

    protected Color foregroundColor;
    protected Color defaultTextForegroundColor;

    /**
     * Construct an {@link EmptyText} field on the specified control, whose
     * default text is characterized by the specified String.
     * 
     * @param control
     *            the control for which a default text is desired. May not be
     *            <code>null</code>.
     * @param defaultText
     *            the String representing the default text.
     */
    public EmptyText(Text control, String defaultText) {
        this.control = control;
        this.defaultText = defaultText;

        this.foregroundColor = this.control.getForeground();
        this.defaultTextForegroundColor = ColorUtils.addLightness(
            this.foregroundColor, 0.55f);

        update();
        registerListeners();
    }

    protected void registerListeners() {
        this.control.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                update(true);
            }

            public void focusLost(FocusEvent e) {
                update(false);
            }
        });

        this.control.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                if (defaultTextForegroundColor != null
                    && !defaultTextForegroundColor.isDisposed()) {
                    defaultTextForegroundColor.dispose();
                }
            }
        });
    }

    /**
     * Updates the {@link Text} {@link Control}'s contents according to its
     * focus.
     */
    protected void update() {
        this.update(this.control.isFocusControl());
    }

    /**
     * Updates the {@link Text} {@link Control}'s contents according to the
     * provided focus.
     */
    protected void update(boolean hasFocus) {
        if (hasFocus) {
            if (this.control.getText().equals(this.defaultText)) {
                this.control.setText("");
            } else {
                this.control.selectAll();
            }
            this.control.setForeground(foregroundColor);
        } else {
            if (this.control.getText().trim().isEmpty()) {
                this.control.setText(this.defaultText);
                this.control.setForeground(defaultTextForegroundColor);
            }
        }
    }

    /**
     * Returns the underlying {@link Text} {@link Control}.
     * 
     * @return
     */
    public Text getControl() {
        return this.control;
    }

    /**
     * @see Text#getText()
     */
    public String getText() {
        return (this.control.getText().equals(this.defaultText)) ? ""
            : this.control.getText();
    }

    /**
     * @see Text#setText(String)
     */
    public void setText(String string) {
        this.control.setText(string);
        update();
    }

    /**
     * @see Text#setEnabled(boolean)
     */
    public void setEnabled(boolean enabled) {
        this.control.setEnabled(enabled);
    }

    /**
     * @see Text#setFocus()
     */
    public void setFocus() {
        this.control.setFocus();
    }
}
