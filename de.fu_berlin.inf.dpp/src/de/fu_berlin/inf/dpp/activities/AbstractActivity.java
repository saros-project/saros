package de.fu_berlin.inf.dpp.activities;

public abstract class AbstractActivity implements IActivity {

    protected String source = null;

    public void setSource(String source) {
        this.source = source;
    }

    public String getSource() {
        return this.source;
    }
}
