package de.fu_berlin.inf.dpp.ui.util.selection;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

/**
 * Utility class for a convenient work with the {@link ISelectionService}
 * 
 * @author bkahlert
 */
public class SelectionUtils {

    private SelectionUtils() {
        // no instantiation allowed
    }

    /**
     * Returns the {@link ISelectionService}
     * 
     * @return
     */
    public static ISelectionService getSelectionService() {
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow()
            .getSelectionService();
    }

    /**
     * Returns the current selection
     * 
     * @return
     * @see ISelectionService#getSelection()
     */
    public static ISelection getSelection() {
        return getSelectionService().getSelection();
    }

    /**
     * Returns the current selection in the given part
     * 
     * @param partId
     *            of the part
     * 
     * @return
     * @see ISelectionService#getSelection(String)
     */
    public static ISelection getSelection(String partId) {
        return getSelectionService().getSelection(partId);
    }

    /**
     * Returns all selections made in the current perspective.
     * 
     * @return
     */
    public static List<ISelection> getOverallSelections() {
        List<ISelection> selections = new ArrayList<ISelection>();

        for (IWorkbenchPage workbenchPage : PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow().getPages()) {
            for (IEditorReference editorReference : workbenchPage
                .getEditorReferences()) {
                ISelection selection = getSelection(editorReference.getId());
                if (selection != null)
                    selections.add(selection);
            }

            for (IViewReference viewReference : workbenchPage
                .getViewReferences()) {
                ISelection selection = getSelection(viewReference.getId());
                if (selection != null)
                    selections.add(selection);
            }
        }

        return selections;
    }

    /**
     * Tries to adapt each selection item to adapter and returns all adapted
     * items.
     * 
     * @param selection
     * @param adapter
     *            to adapt each object to
     * @return
     */
    public static <AdapterType> List<AdapterType> getAdaptableObjects(
        ISelection selection, Class<? extends AdapterType> adapter) {
        List<AdapterType> objects = new ArrayList<AdapterType>();

        if (selection == null)
            return objects;

        if (selection instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection) selection;
            for (Object structuredSelectionItem : structuredSelection.toArray()) {
                AdapterType object = getAdapter(structuredSelectionItem,
                    adapter);

                if (object != null && !objects.contains(object)) {
                    objects.add(object);
                }
            }
        } else {
            AdapterType object = getAdapter(selection, adapter);

            if (object != null && !objects.contains(object)) {
                objects.add(object);
            }
        }

        return objects;
    }

    /**
     * Tries to adapt a given object to adapter.
     * 
     * @param adaptable
     *            object to adapt
     * @param adapter
     *            to adapt the adaptable to
     * @return null if object is not adaptable or not adapter is available;
     *         adapted object otherwise
     */
    @SuppressWarnings("unchecked")
    protected static <AdapterType> AdapterType getAdapter(Object adaptable,
        Class<? extends AdapterType> adapter) {
        try {
            return (AdapterType) ((IAdaptable) adaptable).getAdapter(adapter);
        } catch (Exception e) {
            return null;
        }
    }

}
