package de.fu_berlin.inf.dpp.filesystem;

import org.eclipse.core.resources.ResourceAttributes;

public class EclipseResourceAttributesImpl implements IResourceAttributes {

    private ResourceAttributes delegate;

    public EclipseResourceAttributesImpl(ResourceAttributes attributes) {
        delegate = attributes;
    }

    @Override
    public boolean isReadOnly() {
        return delegate.isReadOnly();
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        delegate.setReadOnly(readOnly);
    }

    /**
     * Returns the original
     * {@link org.eclipse.core.resources.ResourceAttributes ResourceAttributes}
     * object.
     * 
     * @return
     */
    public ResourceAttributes getDelegate() {
        return delegate;
    }
}
