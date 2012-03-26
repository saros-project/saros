package de.fu_berlin.inf.dpp.ui.util.selection.retriever;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionService;

/**
 * Instances of this class can retrieve {@link ISelection}s provided through the
 * {@link ISelectionService} and return all to ConvertType converted selections
 * which are adaptable to AdapterType.
 * 
 * @param <AdapterType>
 *            selections need to be adaptable to
 * 
 * @author bkahlert
 */
public interface ISelectionConvertingRetriever<AdapterType, ConvertType>
    extends ISelectionRetriever<ConvertType> {

    // nothing
}
