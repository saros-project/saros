package de.fu_berlin.inf.dpp.vcs;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.team.core.RepositoryProvider;

public class VCSAdapterFactory {
    private static final Logger log = Logger.getLogger(VCSAdapterFactory.class);

    /**
     * Determine and instantiate the corresponding {@link VCSAdapter} for the
     * provided identifier.<br>
     * Currently supported: Subclipse 1.6.10.
     * 
     * @param identifier
     * @return
     * @see RepositoryProvider#getID()
     */
    public static VCSAdapter getAdapter(String identifier) {
        try {
            if (identifier.equals(SubclipseAdapter.identifier)) {
                return new SubclipseAdapter();
            }
        } catch (NoClassDefFoundError e) {
            // TODO Should we inform the user?
            log.warn("Could not find a VCSAdapter for " + identifier);
        }
        return null;
    }

    /**
     * Determine the repository provider of the project and return the
     * corresponding {@link VCSAdapter}. The method will return
     * <code>null</code> if the project is not under version control, or if no
     * <code>VCSAdapter</code> was found for the repository provider used.
     * 
     * @param project
     * @return
     */
    public static VCSAdapter getAdapter(IProject project) {
        boolean underVCS;
        underVCS = RepositoryProvider.isShared(project);
        if (!underVCS)
            return null;

        RepositoryProvider provider = RepositoryProvider.getProvider(project);
        return getAdapter(provider.getID());
    }
}
