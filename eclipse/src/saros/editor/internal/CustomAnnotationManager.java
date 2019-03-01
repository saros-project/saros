package saros.editor.internal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jface.text.IPainter;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationPainter;
import org.eclipse.jface.text.source.AnnotationPainter.IDrawingStrategy;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;

// TODO support color changes for annotations
public class CustomAnnotationManager {

  private Map<ISourceViewer, AnnotationPainter> installedPainters =
      new HashMap<ISourceViewer, AnnotationPainter>();

  private Map<String, IDrawingStrategy> drawingStrategyForAnnotationType =
      new HashMap<String, IDrawingStrategy>();

  private Map<String, Integer> customAnnotationTypes = new HashMap<String, Integer>();

  private static class CustomMarkerAnnotationAccess extends DefaultMarkerAnnotationAccess {

    private Map<String, Integer> annotationLayerInformation;

    public CustomMarkerAnnotationAccess(Map<String, Integer> annotationLayerInformation) {
      this.annotationLayerInformation = new HashMap<String, Integer>(annotationLayerInformation);
    }

    @Override
    public int getLayer(Annotation annotation) {
      Integer layer = annotationLayerInformation.get(annotation.getType());

      return layer == null ? 0 : layer;
    }
  }

  /**
   * Installs a custom {@link AnnotationPainter annotation painter} to the given source viewer. If
   * there is already an custom annotation painter installed this method just returns.
   *
   * @param sourceViewer
   */
  public void installPainter(ISourceViewer sourceViewer) {

    AnnotationPainter painter = installedPainters.get(sourceViewer);

    if (painter != null) return;

    painter =
        new AnnotationPainter(
            sourceViewer, new CustomMarkerAnnotationAccess(customAnnotationTypes));

    for (String annotationType : customAnnotationTypes.keySet()) {
      IDrawingStrategy strategy = drawingStrategyForAnnotationType.get(annotationType);

      if (strategy == null) continue;

      painter.addAnnotationType(annotationType, annotationType);
      painter.addDrawingStrategy(annotationType, strategy);
      // set a color or the drawing strategy will not be invoked
      // FIMXE no control when this color is disposed
      painter.setAnnotationTypeColor(annotationType, sourceViewer.getTextWidget().getForeground());
    }

    painter.paint(IPainter.CONFIGURATION);
  }

  /**
   * Uninstalls the custom {@link AnnotationPainter annotation painter} from the given source
   * viewer. If there is no custom annotation painter installed this method just returns.
   *
   * @param sourceViewer
   * @param redraw
   */
  public void uninstallPainter(ISourceViewer sourceViewer, boolean redraw) {

    AnnotationPainter painter = installedPainters.remove(sourceViewer);

    if (painter == null) return;

    painter.deactivate(redraw);
    painter.dispose();
  }

  /**
   * Uninstalls all custom {@link AnnotationPainter annotation painters} that were installed by
   * {@link #installPainter(ISourceViewer)}.
   *
   * @param redraw
   */
  public void uninstallAllPainters(boolean redraw) {
    for (ISourceViewer sourceViewer :
        Arrays.asList(installedPainters.keySet().toArray(new ISourceViewer[0])))
      uninstallPainter(sourceViewer, redraw);
  }

  public void registerDrawingStrategy(String annotationType, IDrawingStrategy strategy) {
    drawingStrategyForAnnotationType.put(annotationType, strategy);
  }

  public void registerAnnotation(String annotationType, int layer) {
    customAnnotationTypes.put(annotationType, layer);
  }
}
