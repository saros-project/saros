package de.fu_berlin.inf.dpp.vcs;

import de.fu_berlin.inf.dpp.filesystem.IProject;

public interface VCSProviderFactory {

    /**
     * Determine the repository provider of the project and return the
     * corresponding {@link VCSProvider}. The method will return
     * <code>null</code> if the project is not under version control, or if no
     * provider could be found for the project.
     * 
     * @param project
     * @return
     */
    public VCSProvider getProvider(IProject project);

}
