package de.fu_berlin.inf.dpp.ui.util.selection.retriever.impl;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import de.fu_berlin.inf.dpp.ui.util.selection.retriever.ISelectionConvertingRetriever;
import de.fu_berlin.inf.dpp.ui.util.selection.retriever.ISelectionRetriever;

/**
 * This abstract class implements an {@link ISelectionRetriever} which retrieves
 * selections which are adaptable to AdapterType and convertible to ConvertType.
 * <p>
 * E.g. if you wish to retrieve all selected {@link IResource} in order to get
 * their surrounding {@link IProject}
 * <p>
 * For example,
 * 
 * <pre>
 * ISelectionRetriever&lt;IProject&gt; mySelectionRetriever = 
 *      new AbstractSelectionConvertingRetriever<IResource, IProject>(IResource.class) {
 *      protected IProject convert(IResource resource) {
 *              return resource.getProject();
 *      }
 * }
 * </pre>
 * 
 * @param <AdapterType>
 *            selections need to be adaptable to
 * @param <ConvertType>
 *            adaptable selections are converted into
 * 
 * @see ISelectionConvertingRetriever
 * @author bkahlert
 */
public abstract class AbstractSelectionConvertingRetriever<AdapterType, ConvertType>
    implements ISelectionConvertingRetriever<AdapterType, ConvertType> {

    protected SelectionRetriever<AdapterType> selectionRetriever;

    public AbstractSelectionConvertingRetriever(
        Class<? extends AdapterType> adapter) {
        selectionRetriever = new SelectionRetriever<AdapterType>(adapter);
    }

    public List<ConvertType> getSelection() {
        return convert(selectionRetriever.getSelection());
    }

    public List<ConvertType> getSelection(String partId) {
        return convert(selectionRetriever.getSelection(partId));
    }

    public List<ConvertType> getOverallSelection() {
        return convert(selectionRetriever.getOverallSelection());
    }

    /**
     * Converts the the provided list of objects of AdapterType to a
     * corresponding list of objects of ConvertType.
     * 
     * @param objects
     *            to convert
     * @return converted objects
     */
    protected List<ConvertType> convert(List<AdapterType> objects) {
        List<ConvertType> convertedObjects = new ArrayList<ConvertType>();
        for (AdapterType object : objects) {
            ConvertType convertedObject = convert(object);
            if (convertedObject != null
                && !convertedObjects.contains(convertedObject)) {
                convertedObjects.add(convertedObject);
            }
        }
        return convertedObjects;
    }

    /**
     * Converts the the provided object of AdapterType to a corresponding object
     * of ConvertType.
     * 
     * @param object
     *            to convert
     * @return converted object
     */
    protected abstract ConvertType convert(AdapterType object);

}
