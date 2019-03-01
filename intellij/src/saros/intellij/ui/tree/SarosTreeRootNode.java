package saros.intellij.ui.tree;

import javax.swing.tree.DefaultMutableTreeNode;

/** Root node of the tree view tree (contacts and sessions). */
public class SarosTreeRootNode extends DefaultMutableTreeNode {
  public static final String SPACER = " ";
  public static final String TITLE_JABBER_SERVER = "Not connected";

  public SarosTreeRootNode() {
    super(
        SPACER
            + TITLE_JABBER_SERVER
            + "                                                                    ");
  }

  public void setTitle(String title) {
    setUserObject(SPACER + title);
  }

  public void setTitleDefault() {
    setTitle(TITLE_JABBER_SERVER);
  }
}
