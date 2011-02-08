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
 * <p>
 * For example,
 * 
 * <pre>
 * List&lt;JID&gt; buddies = SelectionRetrieverFactory.getSelectionRetriever(JID.class)
 *     .getSelection();
 * </pre>
 * 
 * or,
 * 
 * <pre>
 * List&lt;IProject&gt; projects = SelectionRetrieverFactory.getSelectionRetriever(
 *     IProject.class).getOverallSelection();
 * </pre>
 * 
 * @author bkahlert
 */
public class SelectionRetrieverFactory {

    /**
     * Returns a {@link SelectionRetriever} capable of retrieving selections of
     * a specific type.
     * <p>
     * Generally the returned {@link SelectionRetriever} is straight forward. If
     * smart is set to true an "intelligent" {@link SelectionRetriever} is
     * returned.
     * <p>
     * In case of {@link IProject}.class the smart {@link SelectionRetriever}
     * does not only return directly selected projects but also the projects,
     * selected resources (like source folder and files) belong to.
     * 
     * @param <AdapterType>
     *            type of the returned list
     * @param adapter
     *            type the selections should be adapted to
     * @param smart
     *            if true, returns an "intelligent" {@link SelectionRetriever}
     * @return
     */
    static public <AdapterType> ISelectionRetriever<AdapterType> getSelectionRetriever(
        Class<? extends AdapterType> adapter, boolean smart) {

        if (smart && adapter.equals(IProject.class)) {
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

    /**
     * Returns a smart {@link SelectionRetriever} capable of retrieving
     * selections of a specific type.
     * <p>
     * Generally the returned {@link SelectionRetriever} is straight forward.
     * <p>
     * In case of {@link IProject}.class the smart {@link SelectionRetriever}
     * does not only return directly selected projects but also the projects,
     * selected resources (like source folder and files) belong to.
     * 
     * @param <AdapterType>
     *            type of the returned list
     * @param adapter
     *            type the selections should be adapted to
     * @return
     */
    static public <AdapterType> ISelectionRetriever<AdapterType> getSelectionRetriever(
        Class<? extends AdapterType> adapter) {
        return getSelectionRetriever(adapter, true);
    }
}
