package de.fu_berlin.inf.dpp.ui.model.session;

import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.jivesoftware.smack.packet.Presence;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.annotations.SarosAnnotation;
import de.fu_berlin.inf.dpp.ui.ImageManager;

public class SessionLabelProvider extends LabelProvider implements
    ITableLabelProvider, ITableColorProvider, ITableFontProvider {

    private Font boldFont = null;

    @Inject
    private EditorManager editorManager;

    @Inject
    protected Saros saros;

    public String getColumnText(Object obj, int index) {

        User participant = (User) obj;
        return participant.getHumanReadableName()
            + (participant.hasReadOnlyAccess() ? " (read-only)" : "")
            + (participant.isInvitationComplete() ? "" : " (joining...)");
    }

    public SessionLabelProvider() {
        super();

        SarosPluginContext.initComponent(this);

        Display disp = Display.getCurrent();
        FontData[] data = disp.getSystemFont().getFontData();
        for (FontData fontData : data) {
            fontData.setStyle(SWT.BOLD);
        }
        this.boldFont = new Font(disp, data);
    }

    @Override
    public Image getImage(Object obj) {
        User user = (User) obj;
        Presence userPresence = saros.getRoster().getPresence(
            user.getJID().toString());
        boolean userAway = userPresence.isAway();

        if (userAway) {
            if (user.hasWriteAccess())
                return ImageManager.ICON_BUDDY_SAROS_AWAY;
            else
                return ImageManager.ICON_BUDDY_SAROS_READONLY_AWAY;
        } else {
            if (user.hasWriteAccess())
                return ImageManager.ICON_BUDDY_SAROS;
            else
                return ImageManager.ICON_BUDDY_SAROS_READONLY;
        }
    }

    public Image getColumnImage(Object obj, int index) {
        return getImage(obj);
    }

    // TODO getting current color does not work if default was changed.
    public Color getBackground(Object element, int columnIndex) {
        return SarosAnnotation.getUserColor((User) element);
    }

    public Color getForeground(Object element, int columnIndex) {
        return null;
    }

    public Font getFont(Object element, int columnIndex) {
        if (element instanceof User) {
            User user = (User) element;

            if (user.equals(editorManager.getFollowedUser())) {
                return this.boldFont;
            }
        }
        return null;
    }

    @Override
    public void dispose() {
        if (this.boldFont != null) {
            this.boldFont.dispose();
            this.boldFont = null;
        }

        super.dispose();
    }
}
