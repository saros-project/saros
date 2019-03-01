package saros.intellij.ui.wizards.pages;

import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 * Wizard page for displaying text information. Usage:
 *
 * <p>InfoPage page = new InfoPage("New session wizard", "Accept", actionListener);
 *
 * <p>infoPage.addText("This is a lengthy info text"); infoPage.addText("spanning several text
 * areas");
 */
public class InfoPage extends AbstractWizardPage {
  private String nextButtonTitle = "Accept";
  private JPanel infoPanel;

  public InfoPage(String id, String nextButtonTitle, PageActionListener pageListener) {
    super(id, pageListener);
    this.nextButtonTitle = nextButtonTitle;
    create();
  }

  private void create() {
    infoPanel = new JPanel();
    infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
    add(infoPanel);
  }

  /**
   * Adds text paragraph
   *
   * @param text
   */
  public void addText(String text) {
    JTextArea textItem = new JTextArea(text);
    textItem.setLineWrap(true);
    textItem.setWrapStyleWord(true);
    textItem.setEditable(false);
    textItem.setBackground(infoPanel.getBackground());
    textItem.setPreferredSize(new Dimension(560, 45));

    JPanel itemPanel = new JPanel();
    itemPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
    itemPanel.add(textItem);

    infoPanel.add(itemPanel);
  }

  @Override
  public String getNextButtonTitle() {
    return nextButtonTitle;
  }
}
