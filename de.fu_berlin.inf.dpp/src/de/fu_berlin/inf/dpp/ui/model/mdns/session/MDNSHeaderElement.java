package de.fu_berlin.inf.dpp.ui.model.mdns.session;

import javax.jmdns.JmDNS;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.model.HeaderElement;
import de.fu_berlin.inf.dpp.ui.model.mdns.MDNSContentProvider;

public class MDNSHeaderElement extends HeaderElement {

    private final MDNSContentProvider provider;
    private final JmDNS jmDNS;

    public MDNSHeaderElement(Font font, MDNSContentProvider provider,
        JmDNS jmDNS) {
        super(font);
        this.provider = provider;
        this.jmDNS = jmDNS;
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
        return provider.getElements(jmDNS);
    }
}