package de.fu_berlin.inf.dpp.intellij.project.filesystem;

import com.intellij.openapi.vfs.LocalFileSystem;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * IDEA implementation of the IFile interface.
 * <p/>
 */
public class IntelliJFileImpl extends IntelliJResourceImpl implements IFile {
    private static Logger LOG = Logger.getLogger(IntelliJFileImpl.class);

    public IntelliJFileImpl(IntelliJProjectImpl project, File file) {
        super(project, file);
    }

    @Override
    public String getCharset() throws IOException {
        return getDefaultCharset();
    }

    @Override
    public InputStream getContents() throws IOException {
        if (getLocation().toFile().exists()) {
            return new FileInputStream(getLocation().toFile());
        }

        return null;
    }

    @Override
    public void setContents(InputStream input, boolean force,
        boolean keepHistory) throws IOException {

        FileOutputStream fos = null;
        fos = new FileOutputStream(getLocation().toFile());

        try {
            int read = -1;
            byte[] buffer = new byte[1024];
            while ((read = input.read(buffer)) != -1) {
                fos.write(buffer, 0, read);
            }
        } finally {
            fos.flush();
            fos.close();
        }
    }

    @Override
    public void create(InputStream input, boolean force) throws IOException {
        setContents(input, true, true);
    }

    @Override
    public long getSize() throws IOException {
        return getLocation().toFile().length();
    }

    @Override
    public int getType() {
        return FILE;
    }

    /**
     * Deletes the file using {@link File#delete()} and updates the IDE with
     * {@link LocalFileSystem#refreshAndFindFileByIoFile(File)}.
     *
     * @param updateFlags - is ignored at the moment
     * @throws IOException
     */
    @Override
    public void delete(int updateFlags) throws IOException {

        File absoluteFile = getLocation().toFile();
        boolean result = absoluteFile.delete();
        LocalFileSystem.getInstance().refreshAndFindFileByIoFile(absoluteFile);

        if (!result) {
            LOG.error("Could not delete " + projectRelativeFile);
        }
    }

    @Override
    public void move(IPath destination, boolean force) throws IOException {
        File absoluteFile = getLocation().toFile();
        if (absoluteFile.renameTo(destination.toFile())) {
            IPath newRelativePath = destination
                .removeFirstSegments(project.getLocation().segmentCount());
            projectRelativeFile = new File(newRelativePath.toPortableString());
        }
    }

    @Override
    public void refreshLocal() throws IOException {
        LOG.trace("refreshLocal() //todo");
    }

    @Override
    public Object getAdapter(Class<? extends IResource> clazz) {
        if (clazz.isInstance(this)) {
            return this;
        }

        return null;
    }

    @Override
    public String toString() {
        return projectRelativeFile.getPath();
    }
}
