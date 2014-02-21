package de.fu_berlin.inf.dpp.filesystem;

public class EclipseFolderImpl extends EclipseContainerImpl implements IFolder {

    EclipseFolderImpl(org.eclipse.core.resources.IFolder delegate) {
        super(delegate);
    }

    /**
     * Returns the original {@link org.eclipse.core.resources.IFolder IFolder}
     * object.
     * 
     * @return
     */
    @Override
    public org.eclipse.core.resources.IFolder getDelegate() {
        return (org.eclipse.core.resources.IFolder) delegate;
    }
}
