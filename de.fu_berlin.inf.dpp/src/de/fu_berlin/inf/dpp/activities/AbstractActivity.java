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
        final int prime = 31;
        int result = 1;
        result = prime * result + ((source == null) ? 0 : source.hashCode());
        return result;
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

        if (!ObjectUtils.equals(this.source, other.source))
            return false;

        return true;
    }

    public String toXML() {
        StringBuilder sb = new StringBuilder();
        this.toXML(sb);
        return sb.toString();
    }
}
