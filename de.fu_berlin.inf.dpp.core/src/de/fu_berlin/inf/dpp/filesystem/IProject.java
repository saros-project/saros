package de.fu_berlin.inf.dpp.filesystem;

import java.io.IOException;

/**
 * This interface is under development. It currently equals its Eclipse
 * counterpart. If not mentioned otherwise all offered method are equivalent to
 * their Eclipse counterpart.
 */
public interface IProject extends IContainer {

    public IResource findMember(IPath path);

    public IFile getFile(String name);

    public IFile getFile(IPath path);

    public IFolder getFolder(String name);

    public IFolder getFolder(IPath path);

    public boolean isOpen();

    public void open() throws IOException;
}
