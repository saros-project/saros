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

package de.fu_berlin.inf.dpp.intellij.project.fs;

import de.fu_berlin.inf.dpp.core.workspace.IWorkspaceRoot;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class WorkspaceRoot implements IWorkspaceRoot {
    public static final Logger LOG = Logger.getLogger(WorkspaceRoot.class);

    private File workspacePath;
    private Map<String, IProject> projects = new HashMap<String, IProject>();

    public WorkspaceRoot(File workspacePath) {
        this.workspacePath = workspacePath;
    }

    protected WorkspaceRoot() {
    }

    @Override
    public IProject getProject(String project) {

        IProject prj = projects.get(project);
        if (prj == null) {
            File fPrj = new File(
                this.workspacePath.getAbsolutePath() + PathImp.FILE_SEPARATOR
                    + project);
            ProjectImp myPrj = new ProjectImp(project, fPrj);

            addProject(myPrj);

            return myPrj;
        } else {
            return prj;
        }
    }

    public void addProject(IProject proj) {
        this.projects.put(proj.getName(), proj);
    }

    public ProjectImp addProject(String name, File path) {

        LOG.info("Add project [" + name + "] path=" + path.getAbsolutePath());

        ProjectImp prj = (ProjectImp) this.projects.get(name);
        if (prj == null) {
            prj = new ProjectImp(name, path);
            addProject(prj);
        }

        return prj;
    }

    @Override
    public IProject getDefaultProject() {
        return null;
    }

    public IProject locateProject(IPath path) {
        //calculate relative path
        String sPath = path.toFile().getAbsolutePath();
        String sWsPath = workspacePath.getAbsolutePath();
        if (!sPath.startsWith(sWsPath)) {
            return null;
        }

        String sPathRelative = sPath.substring(sWsPath.length()).toLowerCase();
        if (sPathRelative.startsWith(File.separator)) {
            sPathRelative = sPathRelative.substring(1);
        }

        for (String projectName : projects.keySet()) {
            if (sPathRelative.startsWith(projectName.toLowerCase())) {
                return projects.get(projectName);
            }
        }

        return null;
    }

}
