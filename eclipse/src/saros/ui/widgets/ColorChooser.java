package saros.ui.widgets;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.picocontainer.annotations.Inject;
import saros.SarosPluginContext;
import saros.editor.annotations.SarosAnnotation;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;

public class ColorChooser extends Composite {

  @Inject protected ISarosSessionManager sessionManager;

  /** List of selection listeners */
  protected final List<ColorSelectionListener> selectionListeners =
      new ArrayList<ColorSelectionListener>();

  /**
   * Current selection. The selection has to be greater or equal to 0 and smaller than COLORS. -1
   * indicates no selection.
   */
  protected int selection = -1;
  /** An array holding the color rectangles (color labels) */
  protected ArrayList<ColorLabel> colorLabels = new ArrayList<ColorLabel>(SarosAnnotation.SIZE);

  /** Updates all subscribers that selection has changed */
  protected MouseListener mouseListener =
      new MouseAdapter() {
        @Override
        public void mouseUp(MouseEvent e) {

          int index = colorLabels.indexOf(e.getSource());

          if (index == -1) return;

          if (!selectColor(index)) return;

          List<ColorSelectionListener> listeners =
              new ArrayList<ColorSelectionListener>(selectionListeners);

          for (ColorSelectionListener selectionListener : listeners)
            selectionListener.selectionChanged(index);
        }
      };

  /**
   * The GUI component used for choosing colors in Preferences -> Saros -> Appearance
   *
   * @param parent
   * @param style
   */
  public ColorChooser(Composite parent, int style) {
    super(parent, style);

    SarosPluginContext.initComponent(this);

    RowLayout layout = new RowLayout(SWT.HORIZONTAL);
    layout.center = true;
    layout.justify = true;
    layout.fill = true;

    setLayout(layout);

    for (int colorId = 0; colorId < SarosAnnotation.SIZE; colorId++) {
      ColorLabel label = createColorLabel(this, style, colorId);
      label.setPreferredSize(50, 50);
      colorLabels.add(label);
    }

    addDisposeListener(
        new DisposeListener() {
          @Override
          public void widgetDisposed(DisposeEvent e) {
            for (ColorLabel colorLabel : colorLabels) ((Color) colorLabel.getData()).dispose();
          }
        });
  }

  private ColorLabel createColorLabel(Composite parent, int style, int colorId) {
    ColorLabel label = new ColorLabel(this, style);

    Color color = SarosAnnotation.getUserColor(colorId);

    label.setData(color);
    label.setColor(color);
    label.addMouseListener(mouseListener);

    return label;
  }

  /**
   * Select color by its ID.
   *
   * @param colorId color to be selected or -1 to reset the current selection
   * @return <code>true</code> if the color has been selected, <code>false</code> if the color did
   *     not exist or was already selected
   */
  public boolean selectColor(int colorId) {

    if (colorId == selection) return false;

    if (selection != -1) colorLabels.get(selection).setSelected(false);

    selection = -1;

    if (colorId < 0 || colorId >= SarosAnnotation.SIZE) return false;

    colorLabels.get(colorId).setSelected(true);
    selection = colorId;

    return true;
  }

  /** Enable or disable {@link ColorLabel}s depending on the availability of its color. */
  public void updateColorEnablement() {
    ISarosSession session = sessionManager.getSession();

    Set<Integer> unavailableColors =
        session != null ? session.getUnavailableColors() : new HashSet<Integer>();

    for (int colorId = 0; colorId < colorLabels.size(); colorId++) {
      ColorLabel colorLabel = colorLabels.get(colorId);
      colorLabel.setEnabled(!unavailableColors.contains(colorId));
    }
  }

  /**
   * Adds a selection listener
   *
   * @param listener
   */
  public void addSelectionListener(ColorSelectionListener listener) {
    selectionListeners.add(listener);
  }

  /**
   * removes a selection listener
   *
   * @param listener
   */
  public void removeSelectionListener(ColorSelectionListener listener) {
    selectionListeners.remove(listener);
  }

  /** Listener that receives events when a new color has been selected. */
  public interface ColorSelectionListener {

    /**
     * Gets called whenever the user has selected a new color.
     *
     * @param colorId new color ID
     */
    void selectionChanged(int colorId);
  }
}
