package de.fu_berlin.inf.dpp.filesystem;

/**
 * This interface is under development. It currently equals its Eclipse
 * counterpart. If not mentioned otherwise all offered method are equivalent to
 * their Eclipse counterpart.
 */
public interface IPath {

    public IPath append(IPath path);

    public boolean isAbsolute();

    public boolean isPrefixOf(IPath path);

    public String toOSString();

    public String toPortableString();
}
