package de.fu_berlin.inf.dpp.editor.annotations;

import org.apache.log4j.Logger;
import org.eclipse.jface.text.source.Annotation;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * Abstract base class for {@link Annotation}s.
 * 
 * Configuration of the annotations is done in the plugin-xml.
 */
public abstract class SarosAnnotation extends Annotation {

    /** Source of this annotation (jabber id). */
    private String source;
    private Logger log = Logger.getLogger(SarosAnnotation.class);

    /**
     * Creates a SarosAnnotation.
     * 
     * @param type
     *            of the {@link Annotation}.
     * @param isNumbered
     *            wether the type should be extended by the color ID of the
     *            source.
     * @param text
     *            for the tooltip.
     * @param source
     *            jabber ID of the source.
     */
    SarosAnnotation(String type, boolean isNumbered, String text, String source) {
        super(type, false, text);
        this.source = source;

        if (isNumbered) {
            setType(type + "." + (getColorIdForUser(source) + 1));
        }
    }

    public String getSource() {
        return this.source;
    }

    protected int getColorIdForUser(String username) {
        User user = Saros.getDefault().getSessionManager().getSharedProject()
            .getParticipant(new JID(username));

        int colorid = 0;
        if (user != null) {
            colorid = user.getColorID();
        } else {
            // This should never happen.
            log.warn("User does not exist: " + username);
        }
        return colorid;
    }

    /**
     * @param prefix
     * @param source
     * @return a string with the given prefix followed by either the nickname of
     *         source or the name part of the jabber ID.
     */
    protected static String createLabel(String prefix, String source) {
        JID jid = new JID(source);
        String nick = Util.getNickname(jid);
        return prefix + " " + ((nick != null) ? nick : jid.getName());
    }
}
