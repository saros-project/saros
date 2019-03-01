package de.fu_berlin.inf.dpp.ui.model.mdns;

import de.fu_berlin.inf.dpp.net.mdns.MDNSService;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.model.HeaderElement;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

public class MDNSHeaderElement extends HeaderElement {

  private final MDNSContentProvider provider;
  private final MDNSService mDNSService;

  public MDNSHeaderElement(
      final Font font, final MDNSContentProvider provider, final MDNSService mDNSService) {
    super(font);
    this.provider = provider;
    this.mDNSService = mDNSService;
  }

  @Override
  public StyledString getStyledText() {
    StyledString styledString = new StyledString();
    styledString.append("Local Area Network", boldStyler);
    return styledString;
  }

  @Override
  public Image getImage() {
    return ImageManager.ICON_GROUP;
  }

  @Override
  public boolean hasChildren() {
    return true;
  }

  @Override
  public Object[] getChildren() {
    return provider.getElements(mDNSService);
  }
}
