package de.fu_berlin.inf.dpp.ui.util.selection.retriever.impl;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

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

    protected Class<? extends AdapterType> adapter;

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

        for (AdapterType object : getAdaptableObjects(selection)) {
            objects.add(object);
        }

        return objects;
    }

    public List<AdapterType> getOverallSelection() {
        List<AdapterType> objects = new ArrayList<AdapterType>();

        List<ISelection> selections = SelectionUtils.getOverallSelections();
        for (ISelection selection : selections) {
            for (AdapterType object : this.getAdaptableObjects(selection)) {
                objects.add(object);
            }
        }

        return objects;
    }

    /**
     * Tries to adapt each selection item to the {@link #adapter} and returns
     * all adaptable items.
     * 
     * @param selection
     * @return
     */
    protected List<AdapterType> getAdaptableObjects(ISelection selection) {
        List<AdapterType> objects = new ArrayList<AdapterType>();

        if (selection == null)
            return objects;

        if (selection instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection) selection;
            for (Object structuredSelectionItem : structuredSelection.toArray()) {
                AdapterType object = this.getAdapter(structuredSelectionItem);

                if (object != null && !objects.contains(object)) {
                    objects.add(object);
                }
            }
        } else {
            AdapterType object = this.getAdapter(selection);

            if (object != null && !objects.contains(object)) {
                objects.add(object);
            }
        }

        return objects;
    }

    /**
     * Tries to adapt a given object to the {@link #adapter}.
     * 
     * @param adaptable
     *            object to adapt
     * @return null if object is not adaptable or not adapter is available.
     */
    @SuppressWarnings("unchecked")
    protected AdapterType getAdapter(Object adaptable) {
        try {
            return (AdapterType) ((IAdaptable) adaptable).getAdapter(adapter);
        } catch (Exception e) {
            return null;
        }
    }

}
