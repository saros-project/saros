package saros.stf.server.rmi.superbot.internal.impl;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
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
import saros.stf.server.StfRemoteObject;
import saros.stf.server.rmi.superbot.internal.IInternal;
import saros.versioning.Version;
import saros.versioning.VersionManager;

public final class InternalImpl extends StfRemoteObject implements IInternal {

  private static final Logger LOG = Logger.getLogger(InternalImpl.class);

  private static final InternalImpl INSTANCE = new InternalImpl();

  private Field localVersionField;

  private Version originalVersion;

  private static class GeneratingInputStream extends InputStream {

    private Random random = null;
    private int size;

    public GeneratingInputStream(int size, boolean compressAble) {
      this.size = size;
      if (!compressAble) this.random = new Random();
    }

    @Override
    public int read() throws IOException {
      if (size == 0) return -1;

      size--;

      if (random != null) return random.nextInt() & 0xFF;

      return 'A';
    }
  }

  public static IInternal getInstance() {
    return INSTANCE;
  }

  private InternalImpl() {
    try {
      localVersionField = VersionManager.class.getDeclaredField("localVersion");
      localVersionField.setAccessible(true);
    } catch (SecurityException e) {
      LOG.error("reflection failed", e);
      localVersionField = null;
    } catch (NoSuchFieldException e) {
      LOG.error("reflection failed", e);
      localVersionField = null;
    }
  }

  @Override
  public void changeSarosVersion(String version) throws RemoteException {

    Version newVersion;

    LOG.trace("attempting to change saros version to: " + version);

    if (localVersionField == null) {
      LOG.error("unable to change version, reflection failed during initialization");
      throw new IllegalStateException(
          "unable to change version, reflection failed during initialization");
    }

    try {
      newVersion = Version.parseVersion(version);
    } catch (IllegalArgumentException e) {
      LOG.error(e.getMessage(), e);
      throw e;
    }

    try {

      if (originalVersion == null)
        originalVersion = (Version) localVersionField.get(getVersionManager());

      localVersionField.set(getVersionManager(), newVersion);

    } catch (IllegalArgumentException e) {
      LOG.error("unable to change saros version, reflection failed", e);
      throw new RemoteException("unable to change saros version, reflection failed", e);
    } catch (IllegalAccessException e) {
      LOG.error("unable to change saros version, reflection failed", e);
      throw new RemoteException("unable to change saros version, reflection failed", e);
    }
  }

  @Override
  public void resetSarosVersion() throws RemoteException {

    LOG.trace("attempting to reset saros version");

    if (originalVersion == null) {
      LOG.trace("saros version was not changed");
      return;
    }

    try {
      localVersionField.set(getVersionManager(), originalVersion);
    } catch (IllegalArgumentException e) {
      LOG.error("unable to reset saros version, reflection failed", e);
      throw new RemoteException("unable to reset saros version, reflection failed", e);
    } catch (IllegalAccessException e) {
      LOG.error("unable to reset saros version, reflection failed", e);
      throw new RemoteException("unable to reset saros version, reflection failed", e);
    }

    LOG.trace("changed saros version to its default state");
    originalVersion = null;
  }

  @Override
  public void createFile(String projectName, String path, String content) throws RemoteException {

    LOG.trace(
        "creating file in project '" + projectName + "', path '" + path + "' content: " + content);

    path = path.replace('\\', '/');

    int idx = path.lastIndexOf('/');

    if (idx != -1) createFolder(projectName, path.substring(0, idx));

    IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);

    IFile file = project.getFile(path);

    try {
      file.create(
          new ByteArrayInputStream(content.getBytes(project.getDefaultCharset())), true, null);
    } catch (CoreException e) {
      LOG.error(e.getMessage(), e);
      throw new RemoteException(e.getMessage(), e.getCause());
    } catch (UnsupportedEncodingException e) {
      LOG.error(e.getMessage(), e);
      throw new RemoteException(e.getMessage(), e.getCause());
    }
  }

  @Override
  public void append(String projectName, String path, String content) throws RemoteException {
    LOG.trace(
        "appending content '"
            + content
            + "' to file '"
            + path
            + "' in project '"
            + projectName
            + "'");
    path = path.replace('\\', '/');

    IFile file = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName).getFile(path);

    try {
      file.appendContents(new ByteArrayInputStream(content.getBytes()), true, false, null);

    } catch (CoreException e) {
      LOG.error(e.getMessage(), e);
      throw new RemoteException(e.getMessage(), e.getCause());
    }
  }

  @Override
  public void createFile(String projectName, String path, int size, boolean compressAble)
      throws RemoteException {

    LOG.trace(
        "creating file in project '"
            + projectName
            + "', path '"
            + path
            + "' size: "
            + size
            + ", compressAble="
            + compressAble);

    path = path.replace('\\', '/');

    int idx = path.lastIndexOf('/');

    if (idx != -1) createFolder(projectName, path.substring(0, idx));

    IFile file = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName).getFile(path);

    try {
      file.create(new GeneratingInputStream(size, compressAble), true, null);
    } catch (CoreException e) {
      LOG.error(e.getMessage(), e);
      throw new RemoteException(e.getMessage(), e.getCause());
    }
  }

  @Override
  public boolean clearWorkspace() throws RemoteException {
    boolean error = false;

    for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
      try {
        LOG.trace("deleting project: " + project.getName());
        project.delete(true, true, null);
      } catch (CoreException e) {
        error = true;
        LOG.error("unable to delete project '" + project.getName() + "' :" + e.getMessage(), e);
      }
    }
    return !error;
  }

  @Override
  public long getFileSize(String projectName, String path) throws RemoteException {

    return new File(
            ResourcesPlugin.getWorkspace()
                .getRoot()
                .getProject(projectName)
                .getFile(path)
                .getLocationURI())
        .length();
  }

  @Override
  public void createProject(String projectName) throws RemoteException {

    LOG.trace("creating project: " + projectName);
    IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
    try {
      project.create(null);
      project.open(null);
    } catch (CoreException e) {
      LOG.error("unable to create project '" + projectName + "' : " + e.getMessage(), e);
      throw new RemoteException(e.getMessage(), e.getCause());
    }
  }

  @Override
  public void createJavaProject(String projectName) throws RemoteException {

    LOG.trace("creating java project: " + projectName);

    IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);

    try {

      project.create(null);
      project.open(null);

      IProjectDescription description = project.getDescription();
      description.setNatureIds(new String[] {JavaCore.NATURE_ID});
      project.setDescription(description, null);

      IJavaProject javaProject = JavaCore.create(project);

      Set<IClasspathEntry> entries = new HashSet<IClasspathEntry>();

      entries.add(JavaCore.newSourceEntry(javaProject.getPath().append("src"), new IPath[0]));

      IVMInstall vmInstall = JavaRuntime.getDefaultVMInstall();

      LibraryLocation[] locations = JavaRuntime.getLibraryLocations(vmInstall);

      for (LibraryLocation element : locations) {
        entries.add(JavaCore.newLibraryEntry(element.getSystemLibraryPath(), null, null));
      }

      javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), null);

    } catch (CoreException e) {
      LOG.error("unable to create java project '" + projectName + "' :" + e.getMessage(), e);
      throw new RemoteException(e.getMessage(), e.getCause());
    }
  }

  @Override
  public void createFolder(String projectName, String path) throws RemoteException {

    path = path.replace('\\', '/');

    IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);

    try {

      String segments[] = path.split("/");
      IFolder folder = project.getFolder(segments[0]);
      LOG.trace(Arrays.asList(segments));
      if (!folder.exists()) folder.create(true, true, null);

      if (segments.length <= 1) return;

      for (int i = 1; i < segments.length; i++) {
        folder = folder.getFolder(segments[i]);
        if (!folder.exists()) folder.create(true, true, null);
      }

    } catch (CoreException e) {
      LOG.error(e.getMessage(), e);
      throw new RemoteException(e.getMessage(), e.getCause());
    }
  }

  @Override
  public void createJavaClass(String projectName, String packageName, String className)
      throws RemoteException {
    String path = "src/" + packageName.replace(".", "/") + "/" + className + ".java";

    StringBuilder content = new StringBuilder();

    if (packageName.trim().length() > 0)
      content.append("package ").append(packageName).append(';').append('\n').append('\n');

    content.append("public class ").append(className).append(" {\n\n}");

    createFile(projectName, path, content.toString());
  }

  @Override
  public boolean existsResource(String path) throws RemoteException {
    return ResourcesPlugin.getWorkspace().getRoot().exists(new Path(path));
  }

  @Override
  public byte[] getFileContent(String projectName, String path) throws RemoteException {

    int size = (int) getFileSize(projectName, path);

    IFile file = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName).getFile(path);

    byte[] content = new byte[size];

    DataInputStream in;

    try {
      in = new DataInputStream(file.getContents());
    } catch (CoreException e) {
      LOG.error(e.getMessage(), e);
      throw new RemoteException(e.getMessage(), e.getCause());
    }

    try {
      in.readFully(content);
      in.close();
    } catch (IOException e) {
      throw new RemoteException("error while reading file:" + file, e);
    }

    return content;
  }

  @Override
  public void changeProjectEncoding(String projectName, String charset) throws RemoteException {

    IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);

    try {
      project.setDefaultCharset(charset, null);
    } catch (CoreException e) {
      LOG.error(e.getMessage(), e);
      throw new RemoteException(e.getMessage(), e.getCause());
    }
  }

  @Override
  public void changeFileEncoding(String projectName, String path, String charset)
      throws RemoteException {
    IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);

    IFile file = project.getFile(path);

    try {
      file.setCharset(charset, null);
    } catch (CoreException e) {
      LOG.error(e.getMessage(), e);
      throw new RemoteException(e.getMessage(), e.getCause());
    }
  }
}
