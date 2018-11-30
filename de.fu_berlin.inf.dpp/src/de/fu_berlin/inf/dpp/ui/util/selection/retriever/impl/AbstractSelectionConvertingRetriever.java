package de.fu_berlin.inf.dpp.ui.util.selection.retriever.impl;

import de.fu_berlin.inf.dpp.ui.util.selection.retriever.ISelectionRetriever;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

/**
 * This abstract class implements an {@link ISelectionRetriever} which retrieves selections which
 * are adaptable to type T and convertible to type S.
 *
 * <p>E.g. if you wish to retrieve all selected {@link IResource} in order to get their surrounding
 * {@link IProject}
 *
 * <p>For example,
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
 * @param <T> selections need to be adaptable to
 * @param <S> adaptable selections are converted into
 * @author bkahlert
 */
public abstract class AbstractSelectionConvertingRetriever<T, S> implements ISelectionRetriever<S> {

  protected SelectionRetriever<T> selectionRetriever;

  public AbstractSelectionConvertingRetriever(Class<? extends T> adapter) {
    selectionRetriever = new SelectionRetriever<T>(adapter);
  }

  @Override
  public List<S> getSelection() {
    return convert(selectionRetriever.getSelection());
  }

  @Override
  public List<S> getSelection(String partId) {
    return convert(selectionRetriever.getSelection(partId));
  }

  @Override
  public List<S> getOverallSelection() {
    return convert(selectionRetriever.getOverallSelection());
  }

  /**
   * Converts the the provided list of objects of AdapterType to a corresponding list of objects of
   * ConvertType.
   *
   * @param objects to convert
   * @return converted objects
   */
  protected List<S> convert(List<T> objects) {
    List<S> convertedObjects = new ArrayList<S>();
    for (T object : objects) {
      S convertedObject = convert(object);
      if (convertedObject != null && !convertedObjects.contains(convertedObject)) {
        convertedObjects.add(convertedObject);
      }
    }
    return convertedObjects;
  }

  /**
   * Converts the the provided object of AdapterType to a corresponding object of ConvertType.
   *
   * @param object to convert
   * @return converted object
   */
  protected abstract S convert(T object);
}
