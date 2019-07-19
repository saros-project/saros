package saros.intellij.ui.views;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.components.JBScrollPane;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.HeadlessException;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ScrollPaneConstants;
import org.jetbrains.annotations.NotNull;
import saros.intellij.ui.tree.SessionAndContactsTreeView;

/**
 * Saros main panel view containing the {@link SessionAndContactsTreeView}, the {@link SarosToolbar}
 * and empty space for future components.
 *
 * <p><b>NOTE:</b> Any component added to the main view must be correctly torn down (i.e. dropping
 * any references that would keep the object from being garbage collected) when the project is
 * disposed to avoid memory leaks. To tear down a component when the project is disposed, implement
 * {@link Disposable} and register it to a suitable parent (either the project or another disposable
 * component registered with the project) using {@link Disposer#register(Disposable, Disposable)}.
 * This will cause {@link Disposable#dispose()} to be called when the parent is disposed.
 */
public class SarosMainPanelView extends JPanel {

  /**
   * Creates the content of the tool window panel, with {@link SarosToolbar} and the {@link
   * SessionAndContactsTreeView}.
   */
  public SarosMainPanelView(@NotNull Project project) throws HeadlessException {
    super(new BorderLayout());
    SessionAndContactsTreeView sarosTree = new SessionAndContactsTreeView(project);
    SarosToolbar sarosToolbar = new SarosToolbar(project);

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
