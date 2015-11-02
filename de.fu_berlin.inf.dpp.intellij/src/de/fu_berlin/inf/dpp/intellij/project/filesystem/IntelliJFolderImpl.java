package de.fu_berlin.inf.dpp.intellij.project.filesystem;

import de.fu_berlin.inf.dpp.filesystem.IFolder;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IntelliJFolderImpl extends IntelliJResourceImpl
    implements IFolder {
    private static Logger LOG = Logger.getLogger(IntelliJFolderImpl.class);

    public IntelliJFolderImpl(IntelliJProjectImpl project, File file) {
        super(project, file);
    }

    @Override
    public void create(int updateFlags, boolean local) throws IOException {
        create(false, local);
    }

    @Override
    public void create(boolean force, boolean local) throws IOException {
        getLocation().toFile().mkdirs();
    }

    @Override
    public boolean exists(IPath path) {
        return getLocation().append(path).toFile().exists();
    }

    @Override
    public IResource[] members() {
        return members(NONE);
    }

    @Override
    public IResource[] members(int memberFlags) {
        List<IResource> list = new ArrayList<IResource>();

        File[] files = getLocation().toFile().listFiles();
        if (files == null)
            return list.toArray(new IResource[] {});

        for (File myFile : files) {
            if (myFile.isFile() && !myFile.isHidden() && (memberFlags == NONE
                || memberFlags == FILE)) {
                list.add(new IntelliJFileImpl(project, myFile));
            }

            if (myFile.isDirectory() && !myFile.isHidden() && (
                memberFlags == NONE || memberFlags == FOLDER)) {
                list.add(new IntelliJFolderImpl(project, myFile));
            }
        }

        return list.toArray(new IResource[] {});
    }

    @Override
    public void refreshLocal() throws IOException {
        LOG.trace("FolderIntl.refreshLocal //todo");
    }

    @Override
    public int getType() {
        return FOLDER;
    }

    @Override
    public void delete(int updateFlags) throws IOException {
        FileUtils.deleteDirectory(getLocation().toFile());
    }

    @Override
    public void move(IPath destination, boolean force) throws IOException {
        projectRelativeFile.renameTo(destination.toFile());
    }

    @Override
    public Object getAdapter(Class<? extends IResource> clazz) {
        if (clazz.isInstance(this)) {
            return this;
        }

        return null;
    }
}
