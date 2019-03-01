package de.fu_berlin.inf.dpp.intellij.ui.wizards.pages;

import com.intellij.ui.JBColor;
import de.fu_berlin.inf.dpp.intellij.ui.util.IconManager;
import java.awt.Color;
import java.awt.FlowLayout;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/** Class presents header panel for wizards. */
public class HeaderPanel extends JPanel {
  private JTextArea textMain;
  private JLabel textTitle;
  private Color backColor = JBColor.WHITE;

  /**
   * Constructor creates header panel with given title and description
   *
   * @param title header title
   * @param text header description
   */
  public HeaderPanel(String title, String text) {
    create(title, text);
  }

  /**
   * Method creates header panel with given title and description
   *
   * @param title header title
   * @param text header description
   */
  private void create(String title, String text) {
    setLayout(new FlowLayout());

    Icon icon = IconManager.SESSION_INVITATION_ICON;
    textMain = new JTextArea();
    textMain.setEditable(false);

    textTitle = new JLabel();
    String textConvertedToJLabelHTML = convertTextToJLabelHTML(title);

    JPanel titlePanel = new JPanel();
    titlePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
    titlePanel.setBackground(backColor);

    textTitle.setText(textConvertedToJLabelHTML);
    titlePanel.add(textTitle);

    textMain.setText(text);
    textMain.setSize(500, 100);

    textMain.setWrapStyleWord(true);
    textMain.setLineWrap(true);

    JPanel textPanel = new JPanel();

    textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

    textPanel.add(titlePanel);
    textPanel.add(textMain);
    textPanel.setBackground(backColor);

    add(textPanel);

    JLabel lblIcon = new JLabel();
    lblIcon.setIcon(icon);
    add(lblIcon);

    setBackground(backColor);
  }

  private String convertTextToJLabelHTML(String text) {
    return "<html>" + text.replace("\n", "<br>") + "</html>";
  }

  public String getTitle() {
    return textTitle.getText();
  }

  public void setTitle(String title) {
    this.textTitle.setText(title);
  }

  public String getText() {
    return textTitle.getText();
  }

  public void setText(String text) {
    this.textMain.setText(text);
  }
}
