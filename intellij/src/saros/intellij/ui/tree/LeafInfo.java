package saros.intellij.ui.tree;

import javax.swing.ImageIcon;

/** Default class to keep item info in tree with key, title and icon. */
public class LeafInfo {
  protected String title;
  private ImageIcon icon;

  LeafInfo(String title) {
    this(title, null);
  }

  LeafInfo(String title, ImageIcon icon) {
    this.title = title;
    this.icon = icon;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public ImageIcon getIcon() {
    return icon;
  }

  public void setIcon(ImageIcon icon) {
    this.icon = icon;
  }

  @Override
  public String toString() {
    return title;
  }
}
