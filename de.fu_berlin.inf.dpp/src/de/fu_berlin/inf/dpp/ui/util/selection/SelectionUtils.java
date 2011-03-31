package de.fu_berlin.inf.dpp.ui.util.selection;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.inf.dpp.util.ArrayUtils;

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
    public static <Adapter> List<Adapter> getAdaptableObjects(
        ISelection selection, Class<? extends Adapter> adapter) {
        List<Object> objectsToAdapt = new ArrayList<Object>();

        if (selection == null) {
            // do nothing
        } else if (selection instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection) selection;
            for (Object structuredSelectionItem : structuredSelection.toArray()) {
                objectsToAdapt.add(structuredSelectionItem);
            }
        } else {
            objectsToAdapt.add(objectsToAdapt);
        }

        return ArrayUtils
            .getAdaptableObjects(objectsToAdapt.toArray(), adapter);
    }

}
