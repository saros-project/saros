package saros.intellij.ui.views;

import com.intellij.ui.components.JBScrollPane;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.HeadlessException;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ScrollPaneConstants;
import saros.intellij.ui.tree.SessionAndContactsTreeView;

/**
 * Saros main panel view containing the {@link SessionAndContactsTreeView}, the {@link SarosToolbar}
 * and empty space for future components.
 */
public class SarosMainPanelView extends JPanel {

  /**
   * Creates the content of the tool window panel, with {@link SarosToolbar} and the {@link
   * SessionAndContactsTreeView}.
   */
  public SarosMainPanelView() throws HeadlessException {
    super(new BorderLayout());
    SessionAndContactsTreeView sarosTree = new SessionAndContactsTreeView();
    SarosToolbar sarosToolbar = new SarosToolbar();

    JScrollPane treeScrollPane = new JBScrollPane(sarosTree);
    treeScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    treeScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    // chartPane is empty at the moment
    Container chartPane = new JPanel(new BorderLayout());

    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScrollPane, chartPane);
    splitPane.setOneTouchExpandable(true);
    splitPane.setDividerLocation(350);

    // Provide minimum sizes for the two components in the split pane
    Dimension minimumSize = new Dimension(300, 50);
    treeScrollPane.setMinimumSize(minimumSize);
    splitPane.setMinimumSize(minimumSize);

    add(splitPane, BorderLayout.CENTER);
    add(sarosToolbar, BorderLayout.NORTH);
  }
}
