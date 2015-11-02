package de.fu_berlin.inf.dpp.intellij.project.filesystem;

import de.fu_berlin.inf.dpp.filesystem.IResourceAttributes;

public class IntelliJResourceAttributesImpl implements IResourceAttributes {
    private boolean readOnly = false;

    public IntelliJResourceAttributesImpl() {
    }

    public IntelliJResourceAttributesImpl(boolean readOnly) {
        this.readOnly = readOnly;
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
}
