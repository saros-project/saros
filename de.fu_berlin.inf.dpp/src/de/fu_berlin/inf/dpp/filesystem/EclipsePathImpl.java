package de.fu_berlin.inf.dpp.filesystem;

public class EclipsePathImpl implements IPath {

    private final org.eclipse.core.runtime.IPath delegate;

    EclipsePathImpl(org.eclipse.core.runtime.IPath delegate) {
        if (delegate == null)
            throw new NullPointerException("delegate is null");

        this.delegate = delegate;
    }

    @Override
    public IPath append(IPath path) {
        return new EclipsePathImpl(delegate.append(((EclipsePathImpl) path)
            .getDelegate()));
    }

    @Override
    public boolean isAbsolute() {
        return delegate.isAbsolute();
    }

    @Override
    public boolean isPrefixOf(IPath path) {
        return delegate.isPrefixOf(((EclipsePathImpl) path).getDelegate());
    }

    @Override
    public String toOSString() {
        return delegate.toOSString();
    }

    @Override
    public String toPortableString() {
        return delegate.toPortableString();
    }

    /**
     * Returns the original {@link org.eclipse.core.runtime.IPath IPath} object.
     * 
     * @return
     */
    public org.eclipse.core.runtime.IPath getDelegate() {
        return delegate;
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof EclipsePathImpl))
            return false;

        return delegate.equals(((EclipsePathImpl) obj).delegate);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
