package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noGUI;

import java.io.IOException;
import java.rmi.RemoteException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.program.Program;

import de.fu_berlin.inf.dpp.stf.server.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.SarosRmiSWTWorkbenchBot;
import de.fu_berlin.inf.dpp.util.FileUtil;
import de.fu_berlin.inf.dpp.vcs.VCSAdapter;
import de.fu_berlin.inf.dpp.vcs.VCSResourceInfo;

public class EclipseState extends EclipseObject implements IEclipseState {

    public static EclipseState classVariable;

    public EclipseState(SarosRmiSWTWorkbenchBot rmiBot) {
        super(rmiBot);
    }

    /**
     * @param filePath
     *            the path of the file. e.g. {Foo_Saros, myFolder, myFile.xml}
     */
    public void waitUntilFileExist(String... filePath) throws RemoteException {
        String fullPath = "";
        for (int i = 0; i < filePath.length; i++) {
            if (i == filePath.length - 1)
                fullPath += filePath[i];
            else
                fullPath += filePath[i] + "/";
        }
        waitUntil(SarosConditions.isResourceExist(fullPath));
    }

    public void waitUntilClassExist(String projectName, String pkg,
        String className) throws RemoteException {
        String path = projectName + "/src/" + pkg.replaceAll("\\.", "/") + "/"
            + className + ".java";
        waitUntil(SarosConditions.isResourceExist(path));
    }

    public void waitUntilClassNotExist(String projectName, String pkg,
        String className) throws RemoteException {
        String path = projectName + "/src/" + pkg.replaceAll("\\.", "/") + "/"
            + className + ".java";
        waitUntil(SarosConditions.isResourceNotExist(path));
    }

    /**
     * Sometimes you want to know, if a peer(e.g. Bob) can see the changes of
     * file, which is modified by another peer (e.g. Alice). Because of data
     * transfer delay Bob need to wait a minute to see the changes . So it will
     * be a good idea that you give bob some time before you compare the two
     * files from Alice and Bob.
     * 
     * <p>
     * <b>Note:</b> the mothod is different from
     * {@link #waitUntilEditorContentSame(String, String, String, String)}. this
     * method compare only the contents of the class files which is saved.
     * </p>
     * * *
     */
    public void waitUntilClassContentsSame(String projectName, String pkg,
        String className, String otherClassContent) throws RemoteException {
        waitUntil(SarosConditions.isClassContentsSame(rmiBot, projectName, pkg,
            className, otherClassContent));
    }

    /**
     * @param folderPath
     *            the path of the new folder. e.g. {Foo_Saros, myFolder,
     *            subFolder}
     */
    public void waitUntilFolderExist(String... folderPath)
        throws RemoteException {
        String fullPath = "";
        for (int i = 0; i < folderPath.length; i++) {
            if (i == folderPath.length - 1)
                fullPath += folderPath[i];
            else
                fullPath += folderPath[i] + "/";
        }
        waitUntil(SarosConditions.isResourceExist(fullPath));
    }

    public void waitUntilProjectNotInSVN(String projectName)
        throws RemoteException {
        waitUntil(SarosConditions.isNotInSVN(projectName));
    }

    public void waitUntilProjectInSVN(String projectName)
        throws RemoteException {
        waitUntil(SarosConditions.isInSVN(projectName));
    }

    public void waitUntilPkgExist(String projectName, String pkg)
        throws RemoteException {
        String path = projectName + "/src/" + pkg.replaceAll("\\.", "/");
        waitUntil(SarosConditions.isResourceExist(path));
    }

    public void waitUntilPkgNotExist(String projectName, String pkg)
        throws RemoteException {
        String path = projectName + "/src/" + pkg.replaceAll("\\.", "/");
        waitUntil(SarosConditions.isResourceNotExist(path));
    }

    public void openClassWithSystemEditor(String projectName, String pkg,
        String className) throws RemoteException {
        IPath path = new Path(projectName + "/src/"
            + pkg.replaceAll("\\.", "/") + "/" + className + ".java");
        final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IResource resource = root.findMember(path);
        Program.launch(resource.getLocation().toString());
    }

    /**
     * Delete a class of the specified java project using
     * FileUntil.delete(resource).
     * 
     * @param projectName
     *            name of the project, which class you want to delete.
     * @param pkg
     *            name of the package, which class you want to delete.
     * @param className
     *            name of the class, which you want to delete.
     */
    public void deleteClass(String projectName, String pkg, String className)
        throws RemoteException {
        IPath path = new Path(projectName + "/src/"
            + pkg.replaceAll("\\.", "/") + "/" + className + ".java");
        final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IResource resource = root.findMember(path);
        if (resource.isAccessible()) {
            try {
                FileUtil.delete(resource);
                root.refreshLocal(IResource.DEPTH_INFINITE, null);

            } catch (CoreException e) {
                log.debug("Couldn't delete file " + className + ".java", e);
            }
        }
    }

    /**
     * Delete a package of the specified java project using
     * FileUntil.delete(resource).
     * 
     * @param projectName
     *            name of the project, which package you want to delete.
     * @param pkg
     *            name of the package, which you want to delete.
     */
    public void deleteFolder(String... folders) throws RemoteException {
        String folderpath = "";
        for (int i = 0; i < folders.length; i++) {
            if (i == folders.length - 1) {
                folderpath += folders[i];
            } else
                folderpath += folders[i] + "/";
        }

        IPath path = new Path(folderpath);
        final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IResource resource = root.findMember(path);
        if (resource.isAccessible()) {
            try {
                FileUtil.delete(resource);
                root.refreshLocal(IResource.DEPTH_INFINITE, null);
            } catch (CoreException e) {
                log.debug("Couldn't delete folder " + folderpath, e);
            }
        }
    }

    /**
     * Delete a package of the specified java project using
     * FileUntil.delete(resource).
     * 
     * @param projectName
     *            name of the project, which package you want to delete.
     * @param pkg
     *            name of the package, which you want to delete.
     */
    public void deletePkg(String projectName, String pkg)
        throws RemoteException {
        IPath path = new Path(projectName + "/src/"
            + pkg.replaceAll("\\.", "/"));
        final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IResource resource = root.findMember(path);
        if (resource.isAccessible()) {
            try {
                FileUtil.delete(resource);
                root.refreshLocal(IResource.DEPTH_INFINITE, null);
            } catch (CoreException e) {
                log.debug("Couldn't delete file " + projectName, e);
            }
        }
    }

    /**
     * Delete the project using FileUntil.delete(resource). This delete-method
     * costs less time than the previous version.
     * 
     * @param projectName
     *            name of the project, which you want to delete.
     */
    public void deleteProject(String projectName) throws RemoteException {
        IPath path = new Path(projectName);
        final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IResource resource = root.findMember(path);
        if (resource.isAccessible()) {
            try {
                FileUtil.delete(resource);
                root.refreshLocal(IResource.DEPTH_INFINITE, null);
            } catch (CoreException e) {
                log.debug("Couldn't delete file " + projectName, e);
            }
        }
    }

    public void deleteAllProjects() throws RemoteException {
        final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IProject[] projects = root.getProjects();
        for (int i = 0; i < projects.length; i++) {
            try {
                FileUtil.delete(projects[i]);
                root.refreshLocal(IResource.DEPTH_INFINITE, null);
            } catch (CoreException e) {
                log.debug("Couldn't delete files ", e);
            }
        }
    }

    public boolean isInSVN() throws RemoteException {
        IProject project = ResourcesPlugin.getWorkspace().getRoot()
            .getProject(BotConfiguration.PROJECTNAME_SVN);
        final VCSAdapter vcs = VCSAdapter.getAdapter(project);
        if (vcs == null)
            return false;
        return true;
    }

    /**
     * @param projectName
     *            name of the project, e.g. Foo_Saros.
     */
    public boolean existsProject(String projectName) throws RemoteException {
        IProject project = ResourcesPlugin.getWorkspace().getRoot()
            .getProject(projectName);
        return project.exists();
    }

    /**
     * @param resourcePath
     *            full path of the resource, e.g.
     *            Foo_Saros/src/my/pkg/myClass.java.
     *            Foo_Saros/myFolder/myFile.xml.
     */
    public boolean isResourceExist(String resourcePath) throws RemoteException {
        IPath path = new Path(resourcePath);
        IResource resource = ResourcesPlugin.getWorkspace().getRoot()
            .findMember(path);

        if (resource == null)
            return false;
        return true;
    }

    /**
     * @param projectName
     *            name of the java project, e.g. Foo_Saros.
     * @param pkg
     *            name of the package, e.g. my.pkg.
     */
    public boolean isPkgExist(String projectName, String pkg)
        throws RemoteException {
        IPath path = new Path(projectName + "/src/"
            + pkg.replaceAll("\\.", "/"));

        IResource resource = ResourcesPlugin.getWorkspace().getRoot()
            .findMember(path);
        if (resource == null)
            return false;
        return true;
    }

    /**
     * @param projectName
     *            name of the java project, e.g. Foo_Saros.
     * @param pkg
     *            name of the package, e.g. my.pkg.
     * @param className
     *            name of the class, e.g. myclass.
     */
    public boolean existsClass(String projectName, String pkg, String className)
        throws RemoteException {
        IPath path = new Path(projectName + "/src/"
            + pkg.replaceAll("\\.", "/") + "/" + className + ".java");
        log.info("Checking existence of file \"" + path + "\"");
        final IFile file = ResourcesPlugin.getWorkspace().getRoot()
            .getFile(path);
        return file.exists();
    }

    /**
     * @param filePath
     *            path of the file, e.g. {Foo_Saros, myFolder, myFile.xml}.
     * 
     */
    public boolean isFileExist(String... filePath) throws RemoteException {
        String fullpath = "";
        for (int i = 0; i < filePath.length; i++) {
            if (i == filePath.length - 1)
                fullpath += filePath[i];
            else
                fullpath += filePath[i] + "/";
        }

        log.info("Checking existence of file \"" + fullpath + "\"");
        IPath path = new Path(fullpath);
        final IFile file = ResourcesPlugin.getWorkspace().getRoot()
            .getFile(path);
        return file.exists();
    }

    /**
     * get the content of the class file, which is saved.
     */
    public String getClassContent(String projectName, String pkg,
        String className) throws RemoteException, IOException, CoreException {
        IPath path = new Path(projectName + "/src/"
            + pkg.replaceAll("\\.", "/") + "/" + className + ".java");
        log.info("Checking existence of file \"" + path + "\"");
        final IFile file = ResourcesPlugin.getWorkspace().getRoot()
            .getFile(path);

        log.info("Checking full path: \"" + file.getFullPath().toOSString()
            + "\"");
        return mainObject.ConvertStreamToString(file.getContents());
    }

    /**
     * @param folderPath
     *            the path of the folder. e.g. {FOO_Saros, myFolder}
     */
    public boolean isFolderExist(String... folderPath) throws RemoteException {
        String folderpath = "";
        for (int i = 0; i < folderPath.length; i++) {
            if (i == folderPath.length - 1) {
                folderpath += folderPath[i];
            } else
                folderpath += folderPath[i] + "/";
        }

        IPath path = new Path(folderpath);
        IResource resource = ResourcesPlugin.getWorkspace().getRoot()
            .findMember(path);

        if (resource == null)
            return false;

        return true;
    }

    public String getURLOfRemoteResource(String fullPath)
        throws RemoteException {
        IPath path = new Path(fullPath);
        IResource resource = ResourcesPlugin.getWorkspace().getRoot()
            .findMember(path);
        final VCSAdapter vcs = VCSAdapter.getAdapter(resource.getProject());
        if (vcs == null)
            return null;
        final VCSResourceInfo info = vcs.getResourceInfo(resource);
        return info.url;
    }

    public String getRevision(String fullPath) throws RemoteException {
        IPath path = new Path(fullPath);
        IResource resource = ResourcesPlugin.getWorkspace().getRoot()
            .findMember(path);
        final VCSAdapter vcs = VCSAdapter.getAdapter(resource.getProject());
        if (vcs == null)
            return null;
        final VCSResourceInfo info = vcs.getResourceInfo(resource);
        return info.revision;
    }
}
