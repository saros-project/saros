package de.fu_berlin.inf.dpp.editor.annotations;

import org.eclipse.jface.text.source.Annotation;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.net.JID;

public class AnnotationSaros extends Annotation {

    private String source;

    AnnotationSaros(String type, boolean isPersistent, String text,
        String source) {
        super(type, isPersistent, text);
        this.source = source;

        if (type.equals(ContributionAnnotation.TYPE)
            || type.equals(SelectionAnnotation.TYPE)) {
            setType(type + "." + (getColorIdForUser(source) + 1));
        }
    }

    AnnotationSaros(String type, boolean isPersistent, String text) {
        super(type, isPersistent, text);
        this.source = null;
    }

    public String getSource() {
        return this.source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    protected int getColorIdForUser(String username) {
        User user = Saros.getDefault().getSessionManager().getSharedProject()
            .getParticipant(new JID(username));

        int colorid = 0;
        if (user != null) {
            colorid = user.getColorID();
        }

        return colorid;
    }

}
