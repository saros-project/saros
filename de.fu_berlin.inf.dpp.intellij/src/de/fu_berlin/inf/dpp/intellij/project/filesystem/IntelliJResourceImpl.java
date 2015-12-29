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

public abstract class IntelliJResourceImpl implements IResource {
    //TODO resolve charset issue by reading real data
    public static final String DEFAULT_CHARSET = "utf8";
    private String defaultCharset = DEFAULT_CHARSET;

    protected IntelliJProjectImpl project;
    protected File projectRelativeFile;
    private IResourceAttributes attributes;

    protected IntelliJResourceImpl(IntelliJProjectImpl project,
        File projectRelativeFile) {
        this.project = project;
        if (project.getLocation().isPrefixOf(
            IntelliJPathImpl.fromString(projectRelativeFile.getPath())))
            this.projectRelativeFile = IntelliJPathImpl
                .fromString(projectRelativeFile.getPath())
                .removeFirstSegments(project.getLocation().segmentCount())
                .toFile();
        else
            this.projectRelativeFile = projectRelativeFile;
        this.attributes = new IntelliJFileResourceAttributesImpl(
            this.projectRelativeFile);
    }

    public String getDefaultCharset() {
        return defaultCharset;
    }

    @Override
    public boolean exists() {
        return getLocation().toFile().exists();
    }

    @Override
    public IPath getFullPath() {
        return IntelliJPathImpl.fromString(project.getName())
            .append(projectRelativeFile.getPath());
    }

    @Override
    public String getName() {
        return projectRelativeFile.getName();
    }

    @Override
    public IContainer getParent() {
        return projectRelativeFile == null
            || projectRelativeFile.getParentFile() == null ?
            null :
            new IntelliJFolderImpl(project,
                projectRelativeFile.getParentFile());
    }

    @Override
    public IntelliJProjectImpl getProject() {
        return project;
    }

    @Override
    public IPath getProjectRelativePath() {
        return IntelliJPathImpl.fromString(projectRelativeFile.getPath());
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
        return getLocation().toFile().canRead();
    }

    @Override
    public boolean isDerived(boolean checkAncestors) {
        return isDerived();
    }

    @Override
    public boolean isDerived() {
        //TODO: Query ModuleRootManager.getExcludedRoots whether this is ignored
        return false;
    }

    @Override
    public IResourceAttributes getResourceAttributes() {
        return attributes;
    }

    @Override
    public void setResourceAttributes(IResourceAttributes attributes) {
        this.attributes = attributes;
    }

    @Override
    public IPath getLocation() {
        return project.getLocation().append(projectRelativeFile.getPath());
    }

    @Override
    public URI getLocationURI() {
        return getLocation().toFile().toURI();
    }

    @Override
    public int hashCode() {
        int hash = 1;

        hash = hash * 31 + this.projectRelativeFile.getName().toLowerCase()
            .hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof IntelliJResourceImpl)) {
            return false;
        }

        IntelliJResourceImpl other = (IntelliJResourceImpl) obj;

        if (this.getType() != other.getType()) {
            return false;
        }

        return getLocation().equals(other.getLocation());
    }
}
