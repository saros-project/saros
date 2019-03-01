package de.fu_berlin.inf.dpp.ui.widgets.chat.items;

import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.widgets.SimpleRoundedComposite;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;

/**
 * This composite is used to display a separator between messages of the same chat partner.
 *
 * @author bkahlert
 */
public class ChatLineSeparator extends SimpleRoundedComposite {

  protected final SimpleDateFormat dateFormatter =
      new SimpleDateFormat(Messages.ChatLineSeparator_date_formatter_pattern);

  protected String username;

  public ChatLineSeparator(Composite parent, String username, Color color, Date date) {
    super(parent, SWT.SEPARATOR | SWT.BORDER);
    this.setBackground(color);
    this.username = username;

    String receivedOn = dateFormatter.format(date);
    setTexts(new String[] {receivedOn});
  }

  public String getUsername() {
    return this.username;
  }
}
