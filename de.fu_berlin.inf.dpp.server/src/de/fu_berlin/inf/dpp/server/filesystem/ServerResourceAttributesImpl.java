package de.fu_berlin.inf.dpp.server.filesystem;

import de.fu_berlin.inf.dpp.filesystem.IResourceAttributes;

/**
 * Server implementation of the {@link IResourceAttributes} interface.
 */
public class ServerResourceAttributesImpl implements IResourceAttributes {

    private boolean readOnly;

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
}
