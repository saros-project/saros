package de.fu_berlin.inf.dpp.ui.widgets.chat.items;

import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.widgets.SimpleRoundedComposite;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;

/**
 * This composite is used to display a separator between messages of different chat partners.
 *
 * @author bkahlert
 */
public class ChatLinePartnerChangeSeparator extends SimpleRoundedComposite {

  protected final SimpleDateFormat dateFormatter =
      new SimpleDateFormat(Messages.ChatLinePartnerChangeSeparator_date_formatter_pattern);

  protected String receivedOn;
  protected String username;

  public ChatLinePartnerChangeSeparator(Composite parent, String username, Color color, Date date) {
    super(parent, SWT.BORDER);
    this.setBackground(color);
    this.username = username;

    receivedOn = dateFormatter.format(date);
    setTexts(new String[] {username, receivedOn});
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
    setTexts(new String[] {username, receivedOn});
  }
}
