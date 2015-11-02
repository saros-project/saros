package de.fu_berlin.inf.dpp.intellij.project.filesystem;

import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IPathFactory;

public class PathFactory implements IPathFactory {
    @Override
    public String fromPath(IPath path) {
        return path.toPortableString();
    }

    @Override
    public IPath fromString(String name) {
        return IntelliJPathImpl.fromString(name);
    }

}
