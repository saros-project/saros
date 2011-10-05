package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.internal.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.internal.IInternal;
import de.fu_berlin.inf.dpp.util.VersionManager;

public final class InternalImpl extends StfRemoteObject implements IInternal {

    private static final Logger log = Logger.getLogger(InternalImpl.class);

    private static final InternalImpl INSTANCE = new InternalImpl();

    private Field versionManagerBundleField;

    private Bundle sarosBundle;

    public static IInternal getInstance() {
        return INSTANCE;
    }

    private InternalImpl() {
        try {
            versionManagerBundleField = VersionManager.class
                .getDeclaredField("bundle");
            versionManagerBundleField.setAccessible(true);
        } catch (SecurityException e) {
            log.error("reflection failed", e);
            versionManagerBundleField = null;
        } catch (NoSuchFieldException e) {
            log.error("reflection failed", e);
            versionManagerBundleField = null;
        }
    }

    public void changeSarosVersion(String version) throws RemoteException {

        Version v;

        log.trace("attempting to change saros version to: " + version);

        if (versionManagerBundleField == null) {
            log.error("unable to change version, reflection failed during initialization");
            throw new IllegalStateException(
                "unable to change version, reflection failed during initialization");
        }

        try {
            v = Version.parseVersion(version);
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage(), e);
            throw e;
        }

        try {

            if (sarosBundle == null)
                sarosBundle = (Bundle) versionManagerBundleField
                    .get(getVersionManager());

            versionManagerBundleField.set(getVersionManager(),
                new BundleFake(v));

        } catch (IllegalArgumentException e) {
            log.error("unable to change saros version, reflection failed", e);
            throw new RemoteException(
                "unable to change saros version, reflection failed", e);
        } catch (IllegalAccessException e) {
            log.error("unable to change saros version, reflection failed", e);
            throw new RemoteException(
                "unable to change saros version, reflection failed", e);
        }

    }

    public void resetSarosVersion() throws RemoteException {

        log.trace("attempting to reset saros version");

        if (sarosBundle == null) {
            log.trace("saros version was not changed");
            return;
        }

        try {
            versionManagerBundleField.set(getVersionManager(), sarosBundle);
        } catch (IllegalArgumentException e) {
            log.error("unable to reset saros version, reflection failed", e);
            throw new RemoteException(
                "unable to reset saros version, reflection failed", e);
        } catch (IllegalAccessException e) {
            log.error("unable to reset saros version, reflection failed", e);
            throw new RemoteException(
                "unable to reset saros version, reflection failed", e);
        }

        log.trace("changed saros version to its default state");
        sarosBundle = null;
    }

    public void createFile(String projectName, String path, String content)
        throws RemoteException {

        log.trace("creating file in project '" + projectName + "', path '"
            + path + "' content: " + content);

        path = path.replace('\\', '/');

        int idx = path.lastIndexOf('/');

        if (idx != -1)
            createFolder(projectName, path.substring(0, idx));

        IFile file = ResourcesPlugin.getWorkspace().getRoot()
            .getProject(projectName).getFile(path);

        try {
            file.create(new ByteArrayInputStream(content.getBytes()), true,
                null);
        } catch (CoreException e) {
            log.debug(e.getMessage(), e);
            throw new RemoteException(e.getMessage(), e.getCause());
        }
    }

    private static class GeneratingInputStream extends InputStream {

        private Random random = null;
        private int size;

        public GeneratingInputStream(int size, boolean compressAble) {
            this.size = size;
            if (!compressAble)
                this.random = new Random();
        }

        @Override
        public int read() throws IOException {
            if (size == 0)
                return -1;

            size--;

            if (random != null)
                return random.nextInt() & 0xFF;

            return 'A';
        }

    }

    public void append(String projectName, String path, String content)
        throws RemoteException {
        log.trace("appending content '" + content + "' to file '" + path
            + "' in project '" + projectName + "'");
        path = path.replace('\\', '/');

        IFile file = ResourcesPlugin.getWorkspace().getRoot()
            .getProject(projectName).getFile(path);

        try {
            file.appendContents(new ByteArrayInputStream(content.getBytes()),
                true, false, null);

        } catch (CoreException e) {
            log.error(e.getMessage(), e);
            throw new RemoteException(e.getMessage(), e.getCause());
        }

    }

    public void createFile(String projectName, String path, int size,
        boolean compressAble) throws RemoteException {

        log.trace("creating file in project '" + projectName + "', path '"
            + path + "' size: " + size + ", compressAble=" + compressAble);

        path = path.replace('\\', '/');

        int idx = path.lastIndexOf('/');

        if (idx != -1)
            createFolder(projectName, path.substring(0, idx));

        IFile file = ResourcesPlugin.getWorkspace().getRoot()
            .getProject(projectName).getFile(path);

        try {
            file.create(new GeneratingInputStream(size, compressAble), true,
                null);
        } catch (CoreException e) {
            log.error(e.getMessage(), e);
            throw new RemoteException(e.getMessage(), e.getCause());
        }
    }

    public boolean clearWorkspace() throws RemoteException {
        boolean error = false;

        for (IProject project : ResourcesPlugin.getWorkspace().getRoot()
            .getProjects()) {
            try {
                log.trace("deleting project: " + project.getName());
                project.delete(true, true, null);
            } catch (CoreException e) {
                error = true;
                log.debug("unable to delete project '" + project.getName()
                    + "' :" + e.getMessage(), e);
            }
        }
        return !error;
    }

    public long getFileSize(String projectName, String path)
        throws RemoteException {

        return new File(ResourcesPlugin.getWorkspace().getRoot()
            .getProject(projectName).getFile(path).getLocationURI()).length();

    }

    public void createProject(String projectName) throws RemoteException {

        log.trace("creating project: " + projectName);
        IProject project = ResourcesPlugin.getWorkspace().getRoot()
            .getProject(projectName);
        try {
            project.create(null);
            project.open(null);
        } catch (CoreException e) {
            log.debug(
                "unable to create project '" + projectName + "' : "
                    + e.getMessage(), e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    public void createJavaProject(String projectName) throws RemoteException {

        log.trace("creating java project: " + projectName);

        IProject project = ResourcesPlugin.getWorkspace().getRoot()
            .getProject(projectName);

        try {

            project.create(null);
            project.open(null);

            IProjectDescription description = project.getDescription();
            description.setNatureIds(new String[] { JavaCore.NATURE_ID });
            project.setDescription(description, null);

            IJavaProject javaProject = JavaCore.create(project);

            Set<IClasspathEntry> entries = new HashSet<IClasspathEntry>();

            entries.add(JavaCore.newSourceEntry(
                javaProject.getPath().append("src"), new IPath[0]));

            IVMInstall vmInstall = JavaRuntime.getDefaultVMInstall();

            LibraryLocation[] locations = JavaRuntime
                .getLibraryLocations(vmInstall);

            for (LibraryLocation element : locations) {
                entries.add(JavaCore.newLibraryEntry(
                    element.getSystemLibraryPath(), null, null));
            }

            javaProject.setRawClasspath(
                entries.toArray(new IClasspathEntry[entries.size()]), null);

        } catch (CoreException e) {
            log.debug("unable to create java project '" + projectName + "' :"
                + e.getMessage(), e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    public void createFolder(String projectName, String path)
        throws RemoteException {

        path = path.replace('\\', '/');

        IProject project = ResourcesPlugin.getWorkspace().getRoot()
            .getProject(projectName);

        try {

            String segments[] = path.split("/");
            IFolder folder = project.getFolder(segments[0]);
            log.trace(Arrays.asList(segments));
            if (!folder.exists())
                folder.create(true, true, null);

            if (segments.length <= 1)
                return;

            for (int i = 1; i < segments.length; i++) {
                folder = folder.getFolder(segments[i]);
                if (!folder.exists())
                    folder.create(true, true, null);
            }

        } catch (CoreException e) {
            log.error(e.getMessage(), e);
            throw new RemoteException(e.getMessage(), e.getCause());
        }
    }

    public void createJavaClass(String projectName, String packageName,
        String className) throws RemoteException {
        String path = "src/" + packageName.replace(".", "/") + "/" + className
            + ".java";

        StringBuilder content = new StringBuilder();

        if (packageName.trim().length() > 0)
            content.append("package ").append(packageName).append(';')
                .append('\n').append('\n');

        content.append("public class ").append(className).append(" {\n\n}");

        createFile(projectName, path, content.toString());

    }

    public boolean existsResource(String path) throws RemoteException {
        return ResourcesPlugin.getWorkspace().getRoot().exists(new Path(path));
    }
}
