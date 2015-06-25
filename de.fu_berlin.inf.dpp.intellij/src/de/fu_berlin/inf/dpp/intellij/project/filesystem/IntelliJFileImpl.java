/*
 *
 *  DPP - Serious Distributed Pair Programming
 *  (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2010
 *  (c) NFQ (www.nfq.com) - 2014
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 1, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * /
 */

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
 * FIXME: Remove all hacks regarding absolute files.
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
        if (file.isAbsolute() && file.exists()) {
            return new FileInputStream(file);
        }

        if (!file.isAbsolute() & getFullPath().toFile().exists()) {
            return new FileInputStream(getFullPath().toFile());
        }

        return null;
    }

    @Override
    public void setContents(InputStream input, boolean force,
        boolean keepHistory) throws IOException {

        FileOutputStream fos = null;
        if (!file.isAbsolute()) {
            fos = new FileOutputStream(getFullPath().toFile());
        } else {
            fos = new FileOutputStream(file);
        }

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
    public IPath getLocation() {
        return new IntelliJPathImpl(file);
    }

    @Override
    public long getSize() throws IOException {
        return getFullPath().toFile().length();
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
        boolean result = true;
        if (!file.isAbsolute()) {
            File absoluteFile = getFullPath().toFile();
            result = absoluteFile.delete();
            LocalFileSystem.getInstance()
                .refreshAndFindFileByIoFile(absoluteFile);
        } else {
            result = file.delete();
            LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
        }
        if (!result) {
            LOG.error("Could not delete " + file);
        }
    }

    @Override
    public void move(IPath destination, boolean force) throws IOException {
        File absoluteFile = getFullPath().toFile();
        if (absoluteFile.renameTo(destination.toFile())) {
            IPath newRelativePath = destination
                .removeFirstSegments(project.getFullPath().segmentCount());
            file = new File(newRelativePath.toPortableString());
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

    public String toString() {
        return file.getPath();
    }
}
