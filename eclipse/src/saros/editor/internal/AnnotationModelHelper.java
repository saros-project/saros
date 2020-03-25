package saros.editor.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.log4j.Logger;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.DocumentProviderRegistry;
import org.eclipse.ui.texteditor.IDocumentProvider;
import saros.util.Predicate;

/** This class holds convenience methods for managing annotations. */
public class AnnotationModelHelper {

  private static final Logger log = Logger.getLogger(AnnotationModelHelper.class);

  private static Iterable<Annotation> toIterable(final IAnnotationModel model) {
    return new Iterable<Annotation>() {
      @Override
      public Iterator<Annotation> iterator() {
        return model.getAnnotationIterator();
      }
    };
  }

  /** Removes annotations that match a given predicate. */
  public void removeAnnotationsFromEditor(IEditorPart editor, Predicate<Annotation> predicate) {
    IAnnotationModel model = retrieveAnnotationModel(editor);

    if (model == null) {
      return;
    }

    removeAnnotationsFromModel(model, predicate);
  }

  /**
   * Removes annotations that match a given predicate.
   *
   * @param model The {@link IAnnotationModel} that should be cleaned.
   * @param predicate The filter to use for cleaning.
   */
  public void removeAnnotationsFromModel(IAnnotationModel model, Predicate<Annotation> predicate) {

    Map<Annotation, Position> replacement = Collections.emptyMap();

    replaceAnnotationsInModel(model, predicate, replacement);
  }

  /**
   * Removes annotations and replaces them in one step.
   *
   * @param model The {@link IAnnotationModel} that should be cleaned.
   * @param annotationsToRemove The annotations to remove.
   * @param annotationsToAdd The annotations to add.
   */
  public void replaceAnnotationsInModel(
      IAnnotationModel model,
      Collection<? extends Annotation> annotationsToRemove,
      Map<? extends Annotation, Position> annotationsToAdd) {

    if (model instanceof IAnnotationModelExtension) {
      IAnnotationModelExtension extension = (IAnnotationModelExtension) model;
      extension.replaceAnnotations(
          annotationsToRemove.toArray(new Annotation[annotationsToRemove.size()]),
          annotationsToAdd);
    } else {
      if (log.isTraceEnabled())
        log.trace("AnnotationModel " + model + " does not support IAnnotationModelExtension");

      for (Annotation annotation : annotationsToRemove) {
        model.removeAnnotation(annotation);
      }

      for (Entry<? extends Annotation, Position> entry : annotationsToAdd.entrySet()) {
        model.addAnnotation(entry.getKey(), entry.getValue());
      }
    }
  }

  /**
   * Removes annotations that match a given predicate and replaces them in one step.
   *
   * @param model The {@link IAnnotationModel} that should be cleaned.
   * @param predicate The filter to use for cleaning.
   */
  public void replaceAnnotationsInModel(
      IAnnotationModel model,
      Predicate<Annotation> predicate,
      Map<Annotation, Position> replacement) {

    ArrayList<Annotation> annotationsToRemove = new ArrayList<Annotation>(128);

    for (Annotation annotation : AnnotationModelHelper.toIterable(model)) {
      if (predicate.evaluate(annotation)) {
        annotationsToRemove.add(annotation);
      }
    }

    replaceAnnotationsInModel(model, annotationsToRemove, replacement);
  }

  public IAnnotationModel retrieveAnnotationModel(IEditorPart editorPart) {
    IEditorInput input = editorPart.getEditorInput();
    IDocumentProvider provider = DocumentProviderRegistry.getDefault().getDocumentProvider(input);
    IAnnotationModel model = provider.getAnnotationModel(input);

    return model;
  }
}
