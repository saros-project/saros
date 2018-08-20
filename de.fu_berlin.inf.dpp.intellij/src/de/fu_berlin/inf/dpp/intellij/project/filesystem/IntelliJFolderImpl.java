package de.fu_berlin.inf.dpp.intellij.project.filesystem;

import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import de.fu_berlin.inf.dpp.filesystem.IFolder;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
import de.fu_berlin.inf.dpp.filesystem.IResource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IntelliJFolderImpl extends IntelliJResourceImpl
    implements IFolder {

    public IntelliJFolderImpl(IntelliJProjectImpl project, File file) {
        super(project, file);
    }

    @Override
    public void create(int updateFlags, boolean local) throws IOException {
        create(false, local);
    }

    @Override
    public void create(boolean force, boolean local) throws IOException {
        writeInUIThread(new ThrowableComputable<Void, IOException>() {

            @Override
            public Void compute() throws IOException {
                getParent().getVirtualFile()
                    .createChildDirectory(this, getName());
                return null;
            }
        });
    }

    /**
     * Equivalent to the exists method of the IContainer interface from eclipse.
     */
    public boolean exists(IPath path) {
        return getLocation().append(path).toFile().exists();
    }

    @Override
    public IResource[] members() {
        return members(NONE);
    }

    @Override
    public IResource[] members(int memberFlags) {
        VirtualFile virtualFile = LocalFileSystem.getInstance()
            .refreshAndFindFileByIoFile(toFile());

        if (virtualFile == null) {
            return new IResource[0];
        }

        VirtualFile[] files = virtualFile.getChildren();

        if (files == null) {
            return new IResource[0];
        }

        List<IResource> list = new ArrayList<IResource>();

        for (VirtualFile file : files) {
            if (file.isDirectory() && (memberFlags == FOLDER
                || memberFlags == NONE)) {
                list.add(
                    new IntelliJFolderImpl(project, new File(file.getPath())));
            }

            if (!file.isDirectory() && (memberFlags == FILE
                || memberFlags == NONE)) {
                list.add(
                    new IntelliJFileImpl(project, new File(file.getPath())));
            }
        }

        return list.toArray(new IResource[list.size()]);
    }

    @Override
    public int getType() {
        return FOLDER;
    }

    @Override
    public Object getAdapter(Class<? extends IResource> clazz) {
        if (clazz.isInstance(this)) {
            return this;
        }

        return null;
    }

    @Override
    public IReferencePoint getReferencePoint()
    {
        return null;
    }
}
