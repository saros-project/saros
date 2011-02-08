package de.fu_berlin.inf.dpp.ui.util.selection.retriever;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

import de.fu_berlin.inf.dpp.ui.util.selection.retriever.impl.AbstractSelectionConvertingRetriever;
import de.fu_berlin.inf.dpp.ui.util.selection.retriever.impl.SelectionRetriever;

/**
 * Instances of this class can return an {@link ISelectionRetriever} which is
 * capable of filtering the {@link IWorkbenchPart}'s {@link ISelection}s.<br/>
 * The filtered selection consists only of objects that can adapt to
 * AdapterType.
 * 
 * @author bkahlert
 */
public class SelectionRetrieverFactory {

    static public <AdapterType> ISelectionRetriever<AdapterType> getSelectionRetriever(
        Class<? extends AdapterType> adapter) {

        if (adapter.equals(IProject.class)) {
            /*
             * If looking for projects we also want to find them in case a
             * project's resource was selected.
             */
            return new AbstractSelectionConvertingRetriever<IResource, AdapterType>(
                IResource.class) {

                @SuppressWarnings("unchecked")
                @Override
                protected AdapterType convert(IResource resource) {
                    return (AdapterType) resource.getProject();
                }
            };
        } else {
            /*
             * Default selection retriever
             */
            return new SelectionRetriever<AdapterType>(adapter);
        }
    }

}
