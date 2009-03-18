package de.fu_berlin.inf.dpp.activities;
 
import org.apache.commons.lang.ObjectUtils;

public abstract class AbstractActivity implements IActivity {

    protected String source = null;

    public void setSource(String source) {
        this.source = source;
    }

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

    public String toXML() {
        StringBuilder sb = new StringBuilder();
        this.toXML(sb);
        return sb.toString();
    }
}
