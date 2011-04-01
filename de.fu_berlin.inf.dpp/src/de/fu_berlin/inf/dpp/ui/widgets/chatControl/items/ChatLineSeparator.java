package de.fu_berlin.inf.dpp.ui.widgets.chatControl.items;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.editor.annotations.SarosAnnotation;
import de.fu_berlin.inf.dpp.ui.widgets.SimpleRoundedComposite;

/**
 * This composite is used to display a separator between messages of the same
 * chat partner.
 * 
 * @author bkahlert
 */
public class ChatLineSeparator extends SimpleRoundedComposite {

    protected final SimpleDateFormat dateFormatter = new SimpleDateFormat(
        "dd/MM/yy HH:mm");

    public ChatLineSeparator(Composite parent, User user, Date date) {
        this(parent, user.getHumanReadableName(), SarosAnnotation
            .getUserColor(user), date);
    }

    public ChatLineSeparator(Composite parent, String username, Color color,
        Date date) {
        super(parent, SWT.SEPARATOR);
        this.setBackground(color);

        String receivedOn = dateFormatter.format(date);
        setTexts(new String[] { receivedOn });
    }

}
