package de.fu_berlin.inf.dpp.filesystem;

import java.io.IOException;

/**
 * This interface is under development. It currently equals its Eclipse
 * counterpart. If not mentioned otherwise all offered method are equivalent to
 * their Eclipse counterpart.
 */
public interface IResource {

    public static final int FILE = 1;
    public static final int FOLDER = 2;
    public static final int PROJECT = 4;
    public static final int ROOT = 8;

    public boolean exists();

    public IPath getFullPath();

    public String getName();

    public IContainer getParent();

    public IProject getProject();

    public IPath getProjectRelativePath();

    public int getType();

    public boolean isAccessible();

    /**
     * Equivalent to the Eclipse call <code>IResource#isDerived(checkAncestors ?
     * IResource#CHECK_ANCESTORS : IResource#NONE)</code>
     * 
     * @param checkAncestors
     * @return
     */
    public boolean isDerived(boolean checkAncestors);

    /**
     * Equivalent to the Eclipse call
     * <code>IResource#refreshLocal(IResource#DEPTH_INFINITE, null)</code>
     */
    public void refreshLocal() throws IOException;
}
