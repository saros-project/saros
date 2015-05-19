package de.fu_berlin.inf.dpp.vcs;

import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.ResourceAdapterFactory;

public class EclipseVCSProviderFactoryImpl implements VCSProviderFactory {

    @Override
    public VCSProvider getProvider(final IProject project) {
        return VCSAdapter
            .getAdapter((org.eclipse.core.resources.IProject) (ResourceAdapterFactory
                .convertBack(project)));
    }

    @Override
    public VCSProvider getProvider(final String identifier) {
        return VCSAdapter.getAdapter(identifier);
    }
}
