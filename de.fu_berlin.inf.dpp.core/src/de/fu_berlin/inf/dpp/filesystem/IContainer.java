package de.fu_berlin.inf.dpp.filesystem;

import java.io.IOException;

/**
 * This interface is under development. It currently equals its Eclipse
 * counterpart. If not mentioned otherwise all offered method are equivalent to
 * their Eclipse counterpart.
 */
public interface IContainer extends IResource {

    public IResource[] members() throws IOException;
}
