package de.fu_berlin.inf.dpp.activities;

import org.apache.commons.lang.ObjectUtils;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.util.Util;

public abstract class AbstractActivity implements IActivity {

    public AbstractActivity(String source) {
        if (source == null)
            throw new IllegalArgumentException("Source cannot be null");
        this.source = source;
    }

    protected final String source;

    public String getSource() {
        return this.source;
    }

    @Override
    public int hashCode() {
        return (source == null) ? 0 : source.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof AbstractActivity))
            return false;

        AbstractActivity other = (AbstractActivity) obj;
        return ObjectUtils.equals(this.source, other.source);
    }

    public void sourceToXML(StringBuilder sb) {
        sb.append("source=\"" + Util.urlEscape(this.source) + "\" ");
    }

    public String toXML() {
        StringBuilder sb = new StringBuilder();
        this.toXML(sb);
        return sb.toString();
    }

    public User getUser() {
        return Saros.getDefault().getSessionManager().getSharedProject()
            .getParticipant(new JID(source));
    }
}
