package de.fu_berlin.inf.dpp.intellij.filesystem;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.vfs.VirtualFile;

import de.fu_berlin.inf.dpp.filesystem.IContainer;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;

public final class IntelliJFileImplV2 extends IntelliJResourceImplV2 implements
    IFile {

    private static final int BUFFER_SIZE = 32 * 1024;

    /** Relative path from the given project */
    private final IPath path;

    private final IntelliJProjectImplV2 project;

    public IntelliJFileImplV2(@NotNull final IntelliJProjectImplV2 project,
        @NotNull final IPath path) {
        this.project = project;
        this.path = path;

    }

    @Override
    public boolean exists() {
        final VirtualFile file = project.findVirtualFile(path);

        return file != null && file.exists() && !file.isDirectory();
    }

    @NotNull
    @Override
    public IPath getFullPath() {
        return project.getFullPath().append(path);
    }

    @NotNull
    @Override
    public String getName() {
        return path.lastSegment();
    }

    @Nullable
    @Override
    public IContainer getParent() {
        if (path.segmentCount() == 1)
            return project;

        return new IntelliJFolderImplV2(project, path.removeLastSegments(1));
    }

    @NotNull
    @Override
    public IProject getProject() {
        return project;
    }

    @NotNull
    @Override
    public IPath getProjectRelativePath() {
        return path;
    }

    @Override
    public int getType() {
        return IResource.FILE;
    }

    @Override
    public boolean isDerived(final boolean checkAncestors) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isDerived() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void delete(final int updateFlags) throws IOException {
        throw new IOException("NYI");
    }

    @Override
    public void move(@NotNull final IPath destination, final boolean force)
        throws IOException {
        throw new IOException("NYI");
    }

    @NotNull
    @Override
    public IPath getLocation() {
        // TODO might return a wrong location
        return project.getLocation().append(path);
    }

    @Nullable
    @Override
    public String getCharset() throws IOException {
        final VirtualFile file = project.findVirtualFile(path);

        return file == null ? null : file.getCharset().name();
    }

    @NotNull
    @Override
    public InputStream getContents() throws IOException {
        /*
         * TODO maybe this needs to be wrapped, the core logic assumes that it
         * can read from any thread
         */

        final VirtualFile file = project.findVirtualFile(path);

        if (file == null)
            throw new FileNotFoundException(this + " does not exist");

        return file.getInputStream();
    }

    @Override
    public void setContents(final InputStream input, final boolean force,
        final boolean keepHistory) throws IOException {

        Filesystem.runWriteAction(new ThrowableComputable<Void, IOException>() {

            @Override
            public Void compute() throws IOException {

                final VirtualFile file = project.findVirtualFile(path);

                if (file == null)
                    throw new FileNotFoundException(
                        IntelliJFileImplV2.this
                            + " does not exist, force option not supported - force="
                            + force);

                final OutputStream out = file
                    .getOutputStream(IntelliJFileImplV2.this);

                final InputStream in = input == null ? new ByteArrayInputStream(
                    new byte[0]) : input;

                final byte[] buffer = new byte[BUFFER_SIZE];

                int read = 0;

                try {

                    while ((read = in.read(buffer)) != -1)
                        out.write(buffer, 0, read);

                } finally {
                    IOUtils.closeQuietly(out);
                    IOUtils.closeQuietly(in);
                }

                return null;
            }

        }, ModalityState.any());
    }

    @Override
    public void create(final InputStream input, final boolean force)
        throws IOException {
        throw new IOException("NYI");
    }

    @Override
    public long getSize() throws IOException {
        final VirtualFile file = project.findVirtualFile(path);

        return file == null ? 0L : file.getLength();
    }

    @Override
    public int hashCode() {
        return project.hashCode() + 31 * path.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {

        if (this == obj)
            return true;

        if (obj == null)
            return false;

        if (getClass() != obj.getClass())
            return false;

        IntelliJFileImplV2 other = (IntelliJFileImplV2) obj;

        return project.equals(other.project) && path.equals(other.path);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " : " + path + " - " + project;
    }
}
