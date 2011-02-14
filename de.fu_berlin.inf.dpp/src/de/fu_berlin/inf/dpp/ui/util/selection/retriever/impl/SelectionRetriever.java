package de.fu_berlin.inf.dpp.ui.util.selection.retriever.impl;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ISelection;

import de.fu_berlin.inf.dpp.ui.util.selection.SelectionUtils;
import de.fu_berlin.inf.dpp.ui.util.selection.retriever.ISelectionRetriever;

/**
 * This class implements a generic {@link ISelectionRetriever} which retrieves
 * selections which are adaptable to AdapterType.
 * <p>
 * E.g. if you wish to retrieve all selected {@link IFile}s
 * 
 * @param <AdapterType>
 *            selections need to be adaptable to
 * 
 * @author bkahlert
 */
public class SelectionRetriever<AdapterType> implements
    ISelectionRetriever<AdapterType> {

    public Class<? extends AdapterType> adapter;

    public SelectionRetriever(Class<? extends AdapterType> adapter) {
        this.adapter = adapter;
    }

    public List<AdapterType> getSelection() {
        return this.getSelection(null);
    }

    public List<AdapterType> getSelection(String partId) {
        List<AdapterType> objects = new ArrayList<AdapterType>();

        ISelection selection = (partId == null) ? SelectionUtils.getSelection()
            : SelectionUtils.getSelection(partId);

        for (AdapterType object : SelectionUtils.getAdaptableObjects(selection,
            adapter)) {
            objects.add(object);
        }

        return objects;
    }

    public List<AdapterType> getOverallSelection() {
        List<AdapterType> objects = new ArrayList<AdapterType>();

        List<ISelection> selections = SelectionUtils.getOverallSelections();
        for (ISelection selection : selections) {
            for (AdapterType object : SelectionUtils.getAdaptableObjects(
                selection, adapter)) {
                objects.add(object);
            }
        }

        return objects;
    }
}
