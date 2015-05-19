package de.fu_berlin.inf.dpp.vcs;

import de.fu_berlin.inf.dpp.filesystem.IProject;

/**
 * INTERFACE UNDER DEVELOPMENT!
 */
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

    /**
     * Determine and instantiate the corresponding {@link VCSProvider} for the
     * provided identifier.
     * 
     * @param identifier
     * @return the corresponding provider for the given identifier or
     *         <code>null</code> if no provider can be found
     */
    public VCSProvider getProvider(String identifier);
}
