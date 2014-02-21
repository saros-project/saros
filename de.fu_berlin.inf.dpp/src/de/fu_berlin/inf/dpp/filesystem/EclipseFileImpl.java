package de.fu_berlin.inf.dpp.filesystem;

public class EclipseFileImpl extends EclipseResourceImpl implements IFile {

    EclipseFileImpl(org.eclipse.core.resources.IFile delegate) {
        super(delegate);
    }

    /**
     * Returns the original {@link org.eclipse.core.resources.IFile IFile}
     * object.
     * 
     * @return
     */
    @Override
    public org.eclipse.core.resources.IFile getDelegate() {
        return (org.eclipse.core.resources.IFile) delegate;
    }
}
