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

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.filesystem.IContainer;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.filesystem.IResourceAttributes;

import java.io.File;
import java.net.URI;

public abstract class ResourceImp implements IResource {
    //TODO resolve charset issue by reading real data
    public static final String DEFAULT_CHARSET = "utf8";
    private String defaultCharset = DEFAULT_CHARSET;

    protected IntelliJProjectImpl project;
    protected File file;
    private IResourceAttributes attributes;
    private boolean isDerived = false;

    protected ResourceImp(IntelliJProjectImpl project, File file) {
        this.project = project;
        this.file = file;
        this.attributes = new IntelliJFileResourceAttributesImpl(file);
    }

    public String getDefaultCharset() {
        return defaultCharset;
    }

    public void setDefaultCharset(String defaultCharset) {
        this.defaultCharset = defaultCharset;
    }

    @Override
    public boolean exists() {
        return getFullPath().toFile().exists();
    }

    @Override
    public IPath getFullPath() {
        // TODO Comply with Interface description: workspace-relative paths
        if (project != null && !file.isAbsolute()) {
            return new IntelliJPathImpl(
                project.getFullPath() + File.separator + file.getPath());
        } else {
            return new IntelliJPathImpl(file.getAbsoluteFile());
        }
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public IContainer getParent() {
        return file == null || file.getParentFile() == null ?
            null :
            new IntelliJFolderImpl(project, file.getParentFile());
    }

    public IntelliJProjectImpl getProject() {
        return project;
    }

    public void setProject(IntelliJProjectImpl project) {
        this.project = project;
    }

    @Override
    public IPath getProjectRelativePath() {
        if (project == null) {
            return new IntelliJPathImpl(file);
        }

        File fPrj = project.getFullPath().toFile();
        if (fPrj.isFile()) {
            fPrj = fPrj.getParentFile();
        }

        if (file.isAbsolute()) {
            String prjPath = fPrj.getAbsolutePath();
            String path = file.getAbsolutePath();
            if (path.length() > prjPath.length()) {
                path = path.substring(prjPath.length() + 1);
            }
            return new IntelliJPathImpl(new File(path));
        }

        return new IntelliJPathImpl(new File(file.getPath()));
    }

    public SPath getSPath() {
        return new SPath(project, getProjectRelativePath());
    }

    @Override
    public int getType() {
        return NONE;
    }

    @Override
    public boolean isAccessible() {
        return getFullPath().toFile().canRead();
    }

    @Override
    public boolean isDerived(boolean checkAncestors) {
        return isDerived(); //todo.
    }

    @Override
    public boolean isDerived() {
        return isDerived;
    }

    public void setDerived(boolean derived) {
        isDerived = derived;
    }

    public File toFile() {
        return file;
    }

    public IResourceAttributes getResourceAttributes() {
        return attributes;
    }

    public void setResourceAttributes(IResourceAttributes attributes) {
        this.attributes = attributes;
    }

    @Override
    public URI getLocationURI() {
        return file.toURI();
    }

    @Override
    public int hashCode() {
        int hash = 1;

        hash = hash * 31 + this.file.getName().toLowerCase().hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ResourceImp)) {
            return false;
        }

        ResourceImp other = (ResourceImp) obj;

        if (this.getType() != other.getType()) {
            return false;
        }

        String thisPath;
        if (this.file.isAbsolute() || this.project == null) {
            thisPath = this.file.getAbsolutePath();
        } else {
            thisPath = this.project.getFullPath().toFile().getAbsolutePath()
                + this.file.getPath();
        }

        String otherPath;
        if (other.file.isAbsolute() || other.project == null) {
            otherPath = other.file.getAbsolutePath();
        } else {
            otherPath = other.project.getFullPath().toFile().getAbsolutePath()
                + File.separator + other.file.getPath();
        }

        return otherPath.equalsIgnoreCase(thisPath);
    }
}
