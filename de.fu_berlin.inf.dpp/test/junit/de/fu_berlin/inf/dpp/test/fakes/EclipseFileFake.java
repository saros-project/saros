package de.fu_berlin.inf.dpp.test.fakes;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.content.IContentDescription;

import java.io.*;
import java.net.URI;


/**
 * Fake-Implementation of {@link org.eclipse.core.resources.IFile}.
 * 
 * Wrappes a {@link java.io.File} to delegate the the most functionality.
 * 
 * If you call a functionallity which is not implemented you will get a
 * {@link UnsupportedOperationException}.
 * 
 * @see de.fu_berlin.inf.dpp.test.util.EclipseWorkspaceFakeFacadeTest
 * @author cordes
 */
public class EclipseFileFake extends EclipseResourceFake implements IFile {

    private EclipseFileFake(File wrappedFile) {
        super(wrappedFile);
    }

    public static EclipseFileFake getMockFile(IPath path, EclipseProjectFake project) {
        String systemPath = project.getWrappedFile().getPath() + "/"
            + path.toPortableString();
        File file = new File(systemPath);
        EclipseFileFake result = new EclipseFileFake(file);
        result.project = project;
        return result;
    }

    public static EclipseFileFake getMockFile(File file, EclipseProjectFake project) {
        EclipseFileFake result = new EclipseFileFake(file);
        result.project = project;
        return result;
    }

    public URI getLocationURI() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void appendContents(InputStream inputStream, boolean b, boolean b1,
        IProgressMonitor iProgressMonitor) throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void appendContents(InputStream inputStream, int i,
        IProgressMonitor iProgressMonitor) throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void create(InputStream inputStream, boolean b,
        IProgressMonitor iProgressMonitor) throws CoreException {
        setContents(inputStream, 0, iProgressMonitor);
    }

    public void create(InputStream inputStream, int i,
        IProgressMonitor iProgressMonitor) throws CoreException {
        setContents(inputStream, 0, iProgressMonitor);
    }

    public void createLink(IPath iPath, int i, IProgressMonitor iProgressMonitor)
        throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void createLink(URI uri, int i, IProgressMonitor iProgressMonitor)
        throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void delete(boolean b, boolean b1, IProgressMonitor iProgressMonitor)
        throws CoreException {
        try {
            FileUtils.forceDelete(wrappedFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getCharset() throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public String getCharset(boolean b) throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public String getCharsetFor(Reader reader) throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IContentDescription getContentDescription() throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public InputStream getContents() throws CoreException {
        FileInputStream result = null;
        try {
            result = new FileInputStream(wrappedFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

    public InputStream getContents(boolean b) throws CoreException {
        return getContents();
    }

    public int getEncoding() throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IFileState[] getHistory(IProgressMonitor iProgressMonitor)
        throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void move(IPath iPath, boolean b, boolean b1,
        IProgressMonitor iProgressMonitor) throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void setCharset(String s) throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void setCharset(String s, IProgressMonitor iProgressMonitor)
        throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void setContents(InputStream inputStream, boolean b, boolean b1,
        IProgressMonitor iProgressMonitor) throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void setContents(IFileState iFileState, boolean b, boolean b1,
        IProgressMonitor iProgressMonitor) throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void setContents(InputStream inputStream, int i,
        IProgressMonitor iProgressMonitor) throws CoreException {
        try {
            FileUtils.forceMkdir(wrappedFile.getParentFile());
            wrappedFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            StringBuilder data = new StringBuilder();
            int c;
            int j = 0;
            while ((c = inputStream.read()) > 0) {
                data.append((char) c);
                j++;
            }
            FileUtils.writeStringToFile(wrappedFile, data.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (iProgressMonitor != null) {
            iProgressMonitor.done();
        }
    }

    public void setContents(IFileState iFileState, int i,
        IProgressMonitor iProgressMonitor) throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }
}
