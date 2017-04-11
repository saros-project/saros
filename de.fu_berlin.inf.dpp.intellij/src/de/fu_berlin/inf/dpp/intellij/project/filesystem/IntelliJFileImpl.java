package de.fu_berlin.inf.dpp.intellij.project.filesystem;

import com.intellij.openapi.util.ThrowableComputable;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * IDEA implementation of the IFile interface.
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
        return getVirtualFile().getInputStream();
    }

    @Override
    public void setContents(final InputStream input, boolean force,
        boolean keepHistory) throws IOException {
        final OutputStream fos = getVirtualFile().getOutputStream(this);

        writeInUIThread(new ThrowableComputable<Void, IOException>() {
            @Override
            public Void compute() throws IOException {
                try {
                    int read;
                    byte[] buffer = new byte[1024];
                    while ((read = input.read(buffer)) != -1) {
                        fos.write(buffer, 0, read);
                    }
                } finally {
                    fos.flush();
                    fos.close();
                }

                return null;
            }
        });
    }

    @Override
    public void create(InputStream input, boolean force) throws IOException {
        writeInUIThread(new ThrowableComputable<Void, IOException>() {

            @Override
            public Void compute() throws IOException {
                getParent().getVirtualFile().createChildData(this, getName());
                return null;
            }
        });

        setContents(input, true, true);
        LOG.trace("Created file " + this);
    }

    @Override
    public long getSize() throws IOException {
        return getVirtualFile().getLength();
    }

    @Override
    public int getType() {
        return FILE;
    }

    @Override
    public Object getAdapter(Class<? extends IResource> clazz) {
        if (clazz.isInstance(this)) {
            return this;
        }

        return null;
    }
}
