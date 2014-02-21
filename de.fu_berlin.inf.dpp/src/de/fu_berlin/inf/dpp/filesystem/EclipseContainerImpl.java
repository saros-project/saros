package de.fu_berlin.inf.dpp.filesystem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;

public abstract class EclipseContainerImpl extends EclipseResourceImpl
    implements IContainer {

    EclipseContainerImpl(org.eclipse.core.resources.IResource delegate) {
        super(delegate);
    }

    @Override
    public IResource[] members() throws IOException {
        org.eclipse.core.resources.IResource[] resources;

        try {
            resources = ((org.eclipse.core.resources.IContainer) delegate)
                .members();

            List<IResource> result = new ArrayList<IResource>(resources.length);
            ResourceAdapterFactory.convertTo(Arrays.asList(resources), result);

            return result.toArray(new IResource[0]);
        } catch (CoreException e) {
            throw new IOException(e);
        }
    }

}
