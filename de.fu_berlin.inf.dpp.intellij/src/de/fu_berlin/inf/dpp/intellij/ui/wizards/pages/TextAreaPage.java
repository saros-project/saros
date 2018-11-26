package de.fu_berlin.inf.dpp.intellij.ui.wizards.pages;

import com.intellij.ui.components.JBScrollPane;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.DefaultEditorKit;

/** Wizard page with text area for displaying multiple lines of info. */
public class TextAreaPage extends AbstractWizardPage {
  private JTextArea display;
  private String title = "";
  private Color fontColor = Color.BLACK;

  /**
   * Constructor with custom ID
   *
   * @param fileListPageId
   * @param title identifier
   * @param pageListener
   */
  public TextAreaPage(String fileListPageId, String title, PageActionListener pageListener) {
    super(fileListPageId, pageListener);
    this.title = title;
    create();
  }

  private void create() {
    setLayout(new BorderLayout());

    JPanel middlePanel = new JPanel();
    middlePanel.setBorder(new TitledBorder(new EtchedBorder(), title));

    display = new JTextArea(10, 48);
    display.getDocument().putProperty(DefaultEditorKit.EndOfLineStringProperty, "\n");
    display.setEditable(false);
    display.setForeground(fontColor);

    JScrollPane scroll = new JBScrollPane(display);
    scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    middlePanel.add(scroll);

    add(middlePanel, BorderLayout.CENTER);

    JPanel progressPanel = new JPanel();
    progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));
  }

  /**
   * Adds text paragraph
   *
   * @param text
   */
  public void addLine(final String text) {
    SwingUtilities.invokeLater(
        new Runnable() {
          @Override
          public void run() {
            display.append(text + "\n");
          }
        });
  }

  @Override
  public boolean isBackButtonEnabled() {
    return false;
  }

  @Override
  public boolean isNextButtonEnabled() {
    return true;
  }
}
