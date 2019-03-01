package de.fu_berlin.inf.dpp.ui.model.mdns;

import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.model.TreeElement;
import javax.jmdns.ServiceInfo;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

public class MDNSEntryElement extends TreeElement {

  private final String qualifiedName;
  private final String displayName;

  public MDNSEntryElement(final ServiceInfo info) {
    qualifiedName = info.getQualifiedName();
    displayName = info.getName();
  }

  public String getName() {
    return qualifiedName;
  }

  @Override
  public Image getImage() {
    return ImageManager.ICON_CONTACT_SAROS_SUPPORT;
  }

  @Override
  public StyledString getStyledText() {
    final StyledString styledString = new StyledString();
    styledString.append(displayName);
    return styledString;
  }

  @Override
  public int hashCode() {
    return qualifiedName.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) return true;

    if (!(obj instanceof MDNSEntryElement)) return false;

    final MDNSEntryElement other = (MDNSEntryElement) obj;

    return qualifiedName.equals(other.qualifiedName);
  }
}
