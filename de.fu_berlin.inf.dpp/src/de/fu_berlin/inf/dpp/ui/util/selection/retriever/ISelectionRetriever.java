package de.fu_berlin.inf.dpp.ui.util.selection.retriever;

import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Instances of this class can retrieve {@link ISelection}s provided through the
 * {@link ISelectionService} and return all selections which are adaptable to
 * AdapterType.
 * 
 * @param <AdapterType>
 *            selections need to be adaptable to
 * 
 * @author bkahlert
 */
public interface ISelectionRetriever<AdapterType> {

    /**
     * Returns the currently selected objects in the active
     * {@link IWorkbenchPart} adapted to the provided Adapter.
     * 
     * @return a list of the selected and adapted objects; never null
     */
    public List<AdapterType> getSelection();

    /**
     * Returns the currently selected objects in the given
     * {@link IWorkbenchPart} adapted to the provided Adapter.
     * 
     * @param partId
     *            of the part
     * 
     * @return a list of the selected and adapted objects; never null
     */
    public List<AdapterType> getSelection(String partId);

    /**
     * Returns the currently selected objects in all {@link IWorkbenchPart}s of
     * opened perspective adapted to the provided Adapter.
     * 
     * @return a list of the selected and adapted objects; never null
     */
    public List<AdapterType> getOverallSelection();

}
