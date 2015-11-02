package de.fu_berlin.inf.dpp.core.vcs;

import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.vcs.VCSProvider;
import de.fu_berlin.inf.dpp.vcs.VCSProviderFactory;

/**
 * An VCSProviderFactory implementation that always returns null. It just
 * satisfies dependencies.
 */
public class NullVCSProviderFactoryImpl implements VCSProviderFactory {

    /**
     * Returns null.
     *
     * @param project
     * @return always <code>null</code>
     */
    @Override
    public VCSProvider getProvider(final IProject project) {
        return null;
    }

    /**
     * Returns <code>null</code> for any given identifier.
     *
     * @return always <code>null</code>
     */
    @Override
    public VCSProvider getProvider(final String identifier) {
        return null;
    }
}
