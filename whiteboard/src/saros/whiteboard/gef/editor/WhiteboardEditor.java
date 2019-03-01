package saros.whiteboard.gef.editor;

import java.util.ArrayList;
import org.apache.batik.util.SVGConstants;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.KeyHandler;
import org.eclipse.gef.KeyStroke;
import org.eclipse.gef.MouseWheelHandler;
import org.eclipse.gef.MouseWheelZoomHandler;
import org.eclipse.gef.SharedImages;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.dnd.TemplateTransferDragSourceListener;
import org.eclipse.gef.dnd.TemplateTransferDropTargetListener;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.CreationToolEntry;
import org.eclipse.gef.palette.MarqueeToolEntry;
import org.eclipse.gef.palette.PaletteGroup;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.PaletteSeparator;
import org.eclipse.gef.palette.PanningSelectionToolEntry;
import org.eclipse.gef.palette.ToolEntry;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.tools.AbstractTool;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.actions.PrintAction;
import org.eclipse.gef.ui.actions.RedoAction;
import org.eclipse.gef.ui.actions.SaveAction;
import org.eclipse.gef.ui.actions.SelectAllAction;
import org.eclipse.gef.ui.actions.UndoAction;
import org.eclipse.gef.ui.actions.ZoomInAction;
import org.eclipse.gef.ui.actions.ZoomOutAction;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.widgets.Composite;
import saros.whiteboard.gef.actions.ChangeBackgroundColorAction;
import saros.whiteboard.gef.actions.ChangeForegroundColorAction;
import saros.whiteboard.gef.actions.CopyRecordAction;
import saros.whiteboard.gef.actions.ExportToImageAction;
import saros.whiteboard.gef.actions.PasteRecordAction;
import saros.whiteboard.gef.actions.SXEDeleteAction;
import saros.whiteboard.gef.commands.ElementRecordCreateCommand;
import saros.whiteboard.gef.editpolicy.ElementModelLayoutEditPolicy;
import saros.whiteboard.gef.part.RecordPartFactory;
import saros.whiteboard.gef.request.CreateTextBoxRequest;
import saros.whiteboard.gef.tools.ArrowCreationTool;
import saros.whiteboard.gef.tools.CreationToolWithoutSelection;
import saros.whiteboard.gef.tools.LineCreationTool;
import saros.whiteboard.gef.tools.PanningTool.PanningToolEntry;
import saros.whiteboard.gef.tools.PointlistCreationTool;
import saros.whiteboard.gef.tools.TextboxCreationTool;
import saros.whiteboard.gef.util.IconUtils;
import saros.whiteboard.net.WhiteboardManager;
import saros.whiteboard.standalone.WhiteboardContextMenuProvider;
import saros.whiteboard.sxe.ISXEMessageHandler.MessageAdapter;
import saros.whiteboard.sxe.ISXEMessageHandler.NotificationListener;
import saros.whiteboard.sxe.net.SXEMessage;
import saros.whiteboard.sxe.records.ElementRecord;

/**
 * The editor creates the GUI using the GEF API and initializes to listen to the WhiteboardManager.
 *
 * @author jurke
 */
public class WhiteboardEditor extends SarosPermissionsGraphicalEditor {

  public static final String ID = "saros.whiteboard.whiteboardeditor";

  private KeyHandler keyHandler;

  /**
   * Creates the editor with a custom command stack
   *
   * @see SXECommandStack
   */
  public WhiteboardEditor() {
    DefaultEditDomain editDomain = new DefaultEditDomain(this);
    editDomain.setCommandStack(new SXECommandStack());
    setEditDomain(editDomain);
    // initColors();
  }

  /**
   * Initializes the graphical viewer with the root element, from now it listens to applied remote
   * records to update action enablement and to document root changes to update the root.
   */
  @Override
  protected void initializeGraphicalViewer() {
    GraphicalViewer viewer = getGraphicalViewer();

    WhiteboardManager.getInstance()
        .getSXEMessageHandler()
        .addMessageListener(
            new MessageAdapter() {

              // obsolete because of notification listener
              // /*
              // * We have to check the action enablement after receiving
              // a
              // * remove message. I.e. maybe a selected item was deleted
              // * thus copy/delete have to be disabled.
              // */
              // @Override
              // public void sxeRecordMessageApplied(SXEMessage message) {
              // updateActions();
              // }

              @Override
              public void sxeStateMessageApplied(SXEMessage message, ElementRecord root) {
                updateViewerContents(root);
              }
            });

    viewer.setContents(
        WhiteboardManager.getInstance().getSXEMessageHandler().getDocumentRecord().getRoot());

    viewer.addDropTargetListener(
        new TemplateTransferDropTargetListener(viewer) {
          /**
           * Overridden by the superclass method because selecting the created object here does not
           * make sense as it differs from the one that will be created by the command (and finally
           * by the DocumentRecord as it should be).
           */
          @Override
          protected void handleDrop() {
            updateTargetRequest();
            updateTargetEditPart();

            if (getTargetEditPart() != null) {
              Command command = getCommand();

              // [FIXME] find a better solution to enable a proper
              // creation of text-based elements via drag & drop
              if (command instanceof ElementRecordCreateCommand) {
                String cName = ((ElementRecordCreateCommand) command).getChildName();
                if (cName.equals(SVGConstants.SVG_TEXT_VALUE)) {

                  // Open dialog to enter the text
                  InputDialog d =
                      new InputDialog(null, "Enter Text", "Enter the text", "text", null);
                  String val = "";
                  if (d != null) {
                    d.open();

                    val = d.getValue();
                    if (val == null || val.isEmpty()) val = "";
                  }

                  CreateRequest oldReq = ((CreateRequest) getTargetRequest());
                  CreateTextBoxRequest r = new CreateTextBoxRequest();
                  r.setSize(oldReq.getSize());
                  r.setLocation(oldReq.getLocation());
                  r.setFactory(new CombinedTargetRecordCreationFactory(cName));
                  r.setText(val);

                  ElementModelLayoutEditPolicy p =
                      ((ElementModelLayoutEditPolicy)
                          (getTargetEditPart().getEditPolicy(EditPolicy.LAYOUT_ROLE)));
                  command = p.getCommand(r);
                }
              }

              if (command != null && command.canExecute())
                getViewer().getEditDomain().getCommandStack().execute(command);
              else getCurrentEvent().detail = DND.DROP_NONE;
            } else getCurrentEvent().detail = DND.DROP_NONE;
          }
        });

    super.initializeGraphicalViewer();
  }

  protected void updateViewerContents(ElementRecord root) {
    if (root == null) return;
    getGraphicalViewer().setContents(root);
  }

  /**
   * Creates a custom <code>ScrollingGraphicalViewer</code> that does not update actions while
   * editparts are notified about applied records to recreate their figures.
   *
   * <p>This was added because performance decreases a lot if the whole selection infrastructure
   * plus every <code>SelectionAction</code> is recalculated on every selection handle removal
   * during <code>EditPart.refreshChildren()</code>.
   */
  /*
   * A delete of 7000 selected objects - they have to be selected to be
   * deleted - took more than 10 minutes, with this fix it is done in less
   * than half a minute. An updateActions() is now forced after processing a
   * bunch of records.
   */
  @Override
  protected void createGraphicalViewer(Composite parent) {

    final GraphicalViewer viewer =
        new ScrollingGraphicalViewer() {

          protected boolean isNotifying = false;

          {
            WhiteboardManager.getInstance()
                .getSXEMessageHandler()
                .addNotificationListener(
                    new NotificationListener() {

                      @Override
                      public void beforeNotification() {
                        isNotifying = true;
                      }

                      @Override
                      public void afterNotificaion() {
                        isNotifying = false;
                        fireSelectionChanged();
                        updateActions();
                      }
                    });
          }

          @Override
          protected void fireSelectionChanged() {
            if (isNotifying) return;
            super.fireSelectionChanged();
          }
        };

    viewer.createControl(parent);
    setGraphicalViewer(viewer);
    configureGraphicalViewer();
    hookGraphicalViewer();
    initializeGraphicalViewer();
  }

  @Override
  protected void initializePaletteViewer() {
    super.initializePaletteViewer();
    getPaletteViewer()
        .addDragSourceListener(new TemplateTransferDragSourceListener(getPaletteViewer()));
  }

  /*
   * Initializes/adds the EditPartFactory, RootEditPart, ZoomManager,
   * KeyHandler and ContextMenuProvider
   *
   * (non-Javadoc)
   *
   * @see org.eclipse.gef.ui.parts.GraphicalEditor#configureGraphicalViewer()
   */
  @Override
  protected void configureGraphicalViewer() {
    double[] zoomLevels;

    super.configureGraphicalViewer();

    GraphicalViewer viewer = getGraphicalViewer();
    viewer.setEditPartFactory(new RecordPartFactory());

    ScalableFreeformRootEditPart rootEditPart = new ScalableFreeformRootEditPart();
    viewer.setRootEditPart(rootEditPart);

    ZoomManager manager = rootEditPart.getZoomManager();
    getActionRegistry().registerAction(new ZoomInAction(manager));
    getActionRegistry().registerAction(new ZoomOutAction(manager));

    zoomLevels = new double[] {0.1, 0.25, 0.5, 0.75, 1, 1.5, 2.0, 2.5, 3, 4, 5, 10};
    manager.setZoomLevels(zoomLevels);
    manager.setZoom(1);
    ArrayList<String> zoomContributions = new ArrayList<String>();
    zoomContributions.add(ZoomManager.FIT_ALL);
    zoomContributions.add(ZoomManager.FIT_HEIGHT);
    zoomContributions.add(ZoomManager.FIT_WIDTH);
    manager.setZoomLevelContributions(zoomContributions);

    keyHandler = new KeyHandler();
    keyHandler.put(
        KeyStroke.getPressed('+', SWT.KEYPAD_ADD, 0),
        getActionRegistry().getAction(GEFActionConstants.ZOOM_IN));
    keyHandler.put(
        KeyStroke.getPressed('-', SWT.KEYPAD_SUBTRACT, 0),
        getActionRegistry().getAction(GEFActionConstants.ZOOM_OUT));

    viewer.setProperty(
        MouseWheelHandler.KeyGenerator.getKey(SWT.NONE), MouseWheelZoomHandler.SINGLETON);

    viewer.setKeyHandler(keyHandler);

    ContextMenuProvider provider = new WhiteboardContextMenuProvider(viewer, getActionRegistry());
    viewer.setContextMenu(provider);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void createActions() {
    ActionRegistry registry = getActionRegistry();
    IAction action;

    action = new UndoAction(this);
    registry.registerAction(action);
    getStackActions().add(action.getId());

    action = new RedoAction(this);
    registry.registerAction(action);
    getStackActions().add(action.getId());

    action = new ChangeBackgroundColorAction();
    registry.registerAction(action);
    getSelectionActions().add(action.getId());

    action = new ChangeForegroundColorAction();
    registry.registerAction(action);
    getSelectionActions().add(action.getId());

    action =
        new SelectAllAction(this) {
          {
            /*
             * Somehow it did not work to set a transparent color, let's use
             * the standard marquee icon for the moment
             */
            setHoverImageDescriptor(SharedImages.DESC_MARQUEE_TOOL_16);
            setImageDescriptor(SharedImages.DESC_MARQUEE_TOOL_16);
          }
        };
    registry.registerAction(action);

    action = new SXEDeleteAction(this);
    registry.registerAction(action);
    getSelectionActions().add(action.getId());

    action = new SaveAction(this);
    registry.registerAction(action);
    getPropertyActions().add(action.getId());

    registry.registerAction(new PrintAction(this));

    action = new CopyRecordAction(this);
    registry.registerAction(action);
    getSelectionActions().add(action.getId());

    action = new PasteRecordAction(this);
    registry.registerAction(action);
    getSelectionActions().add(action.getId());

    action = new ExportToImageAction(this);
    registry.registerAction(action);
    getStackActions().add(action.getId());
  }

  @Override
  public Object getAdapter(@SuppressWarnings("rawtypes") Class type) {
    if (type == ZoomManager.class)
      return ((ScalableFreeformRootEditPart) getGraphicalViewer().getRootEditPart())
          .getZoomManager();
    return super.getAdapter(type);
  }

  @Override
  protected PaletteRoot getPaletteRoot() {
    PaletteRoot root = new PaletteRoot();

    PaletteGroup manipGroup = new PaletteGroup("Manipulate elements");
    root.add(manipGroup);

    PanningSelectionToolEntry selectionToolEntry = new PanningSelectionToolEntry();
    manipGroup.add(selectionToolEntry);

    MarqueeToolEntry marqueeToolEntry = new MarqueeToolEntry();
    manipGroup.add(marqueeToolEntry);

    PanningToolEntry panningToolEntry = new PanningToolEntry();
    manipGroup.add(panningToolEntry);

    PaletteSeparator sep2 = new PaletteSeparator();
    root.add(sep2);

    PaletteGroup instGroup = new PaletteGroup("Create elements");
    root.add(instGroup);
    instGroup.add(createPolylineToolEntry());
    instGroup.add(createRectangleToolEntry());
    instGroup.add(createEllipseToolEntry());
    instGroup.add(createTextToolEntry());
    instGroup.add(createLineToolEntry());
    instGroup.add(createArrowToolEntry());

    PaletteSeparator sep3 = new PaletteSeparator();
    root.add(sep3);

    PaletteGroup colourGroup = new PaletteGroup("Choose colour");
    root.add(colourGroup);

    root.setDefaultEntry(selectionToolEntry);

    return root;
  }

  protected static ToolEntry createTextToolEntry() {

    // Note: same template for Drag and Drop as well as click and drag
    CombinedTargetRecordCreationFactory template =
        new CombinedTargetRecordCreationFactory(SVGConstants.SVG_TEXT_TAG);

    CreationToolEntry entry =
        new CombinedTemplateCreationEntry(
            "TextBox",
            "Creation of a textbox",
            template,
            template,
            ImageDescriptor.createFromImage(IconUtils.getTextBoxImage()),
            ImageDescriptor.createFromImage(IconUtils.getTextBoxImage()));
    entry.setToolProperty(AbstractTool.PROPERTY_UNLOAD_WHEN_FINISHED, false);
    entry.setToolClass(TextboxCreationTool.class);
    return entry;
  }

  protected static ToolEntry createEllipseToolEntry() {
    // Note: same template for Drag and Drop as well as click and drag
    CombinedTargetRecordCreationFactory template =
        new CombinedTargetRecordCreationFactory(SVGConstants.SVG_ELLIPSE_TAG);

    CreationToolEntry entry =
        new CombinedTemplateCreationEntry(
            "Ellipse",
            "Creation of an ellipse",
            template,
            template,
            ImageDescriptor.createFromImage(IconUtils.getEllipseImage()),
            ImageDescriptor.createFromImage(IconUtils.getEllipseImage()));
    entry.setToolProperty(AbstractTool.PROPERTY_UNLOAD_WHEN_FINISHED, false);
    entry.setToolClass(CreationToolWithoutSelection.class);
    return entry;
  }

  protected static ToolEntry createRectangleToolEntry() {
    // Note: same template for Drag and Drop as well as click and drag
    CombinedTargetRecordCreationFactory template =
        new CombinedTargetRecordCreationFactory(SVGConstants.SVG_RECT_TAG);
    CreationToolEntry entry =
        new CombinedTemplateCreationEntry(
            "Rectangle",
            "Creation of a rectangle",
            template,
            template,
            ImageDescriptor.createFromImage(IconUtils.getRectImage()),
            ImageDescriptor.createFromImage(IconUtils.getRectImage()));
    entry.setToolProperty(AbstractTool.PROPERTY_UNLOAD_WHEN_FINISHED, false);
    entry.setToolClass(CreationToolWithoutSelection.class);
    return entry;
  }

  protected static ToolEntry createPolylineToolEntry() {
    // Note: same template for Drag and Drop as well as click and drag
    CombinedTargetRecordCreationFactory template =
        new CombinedTargetRecordCreationFactory(SVGConstants.SVG_POLYLINE_TAG);

    CreationToolEntry entry =
        new CombinedTemplateCreationEntry(
            "Pencil",
            "Free hand drawing",
            template,
            template,
            ImageDescriptor.createFromImage(IconUtils.getPencilImage()),
            ImageDescriptor.createFromImage(IconUtils.getPencilImage()));
    entry.setToolProperty(AbstractTool.PROPERTY_UNLOAD_WHEN_FINISHED, false);
    entry.setToolClass(PointlistCreationTool.class);
    return entry;
  }

  protected static ToolEntry createLineToolEntry() {
    // Note: A normal Line is a Polyline with just 2 Points
    CombinedTargetRecordCreationFactory template =
        new CombinedTargetRecordCreationFactory(SVGConstants.SVG_LINE_TAG);

    CreationToolEntry entry =
        new CombinedTemplateCreationEntry(
            "Line",
            "Drawing a line",
            template,
            template,
            ImageDescriptor.createFromImage(IconUtils.getLineImage()),
            ImageDescriptor.createFromImage(IconUtils.getLineImage()));
    entry.setToolProperty(AbstractTool.PROPERTY_UNLOAD_WHEN_FINISHED, false);
    entry.setToolClass(LineCreationTool.class);
    return entry;
  }

  protected static ToolEntry createArrowToolEntry() {
    // Note: A normal Line is a Polyline with just 2 Points
    CombinedTargetRecordCreationFactory template =
        new CombinedTargetRecordCreationFactory(SVGConstants.SVG_ARROW_TAG);

    CreationToolEntry entry =
        new CombinedTemplateCreationEntry(
            "ArrowLine",
            "Drawing an arrow",
            template,
            template,
            ImageDescriptor.createFromImage(IconUtils.getArrowImage()),
            ImageDescriptor.createFromImage(IconUtils.getArrowImage()));
    entry.setToolProperty(AbstractTool.PROPERTY_UNLOAD_WHEN_FINISHED, false);
    entry.setToolClass(ArrowCreationTool.class);
    return entry;
  }

  // TODO Saving
  @Override
  public void doSave(IProgressMonitor monitor) {}

  @Override
  public void doSaveAs() {}

  @Override
  public boolean isDirty() {
    return false;
  }

  @Override
  public boolean isSaveAsAllowed() {
    return false;
  }

  /*
   * Updates the registered selection actions, needed for the copy command
   */
  public void updateSelectionActions() {
    updateActions(getSelectionActions());
  }

  // public void createPartControl(Composite parent) {
  //
  // /* the whole area*/
  // Composite displayArea = new Composite(parent, SWT.NONE);
  //
  // GridLayout layout = new GridLayout(1, false);
  // layout.marginBottom = layout.marginLeft = layout.marginRight =
  // layout.marginTop = layout.marginWidth = layout.marginHeight = 0;
  // displayArea.setLayout(layout);
  //
  // /* GEF surface */
  // Composite paintArea = new Composite(displayArea, SWT.NONE);
  // paintArea.setLayoutData(new GridData(GridData.FILL_BOTH));
  // paintArea.setLayout(new FillLayout());
  //
  // super.createPartControl(paintArea);
  //
  // /* palette and statusbar */
  // Composite south = new Composite(displayArea, SWT.NONE);
  // layout.marginBottom = layout.marginLeft = layout.marginRight =
  // layout.marginTop = layout.marginWidth = layout.marginHeight = 0;
  // displayArea.setLayout(layout);
  // south.setLayout(layout);
  // south.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
  // createColorPalette(south);
  // }

  /*
   * private Canvas activeForegroundColorCanvas; private Canvas
   * activeBackgroundColorCanvas; private Color paintColorBlack,
   * paintColorWhite; // alias for paintColors[0] and [1] private Color[]
   * paintColors; private static final int numPaletteRows = 2; private static
   * final int numPaletteCols = 30;
   *
   * protected void initColors() {
   *
   * Display display = Display.getDefault();
   *
   * paintColorWhite = new Color(display, 255, 255, 255); paintColorBlack =
   * new Color(display, 0, 0, 0);
   *
   * paintColors = new Color[numPaletteCols * numPaletteRows]; paintColors[0]
   * = paintColorBlack; paintColors[1] = paintColorWhite;
   *
   *
   * for (int i = 2; i < paintColors.length; i++) { paintColors[i] = new
   * Color(display, ((i*7)%255),((i*23)%255), ((i*51)%255)); } }
   *
   * public void setDefaults() { setForegroundColor(paintColorBlack);
   * setBackgroundColor(paintColorWhite); }
   *
   * protected void createColorPalette(Composite displayArea) { GridLayout
   * gridLayout; GridData gridData;
   *
   * // color selector frame final Composite colorFrame = new
   * Composite(displayArea, SWT.NONE); gridData = new
   * GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL);
   * colorFrame.setLayoutData(gridData);
   *
   * // status text final Text statusText = new Text(displayArea, SWT.BORDER |
   * SWT.SINGLE | SWT.READ_ONLY); gridData = new
   * GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL);
   * statusText.setLayoutData(gridData);
   *
   * // colorFrame gridLayout = new GridLayout(); gridLayout.numColumns = 3;
   * gridLayout.marginHeight = 0; gridLayout.marginWidth = 0;
   * colorFrame.setLayout(gridLayout);
   *
   * // activeForegroundColorCanvas, activeBackgroundColorCanvas
   * activeForegroundColorCanvas = new Canvas(colorFrame, SWT.BORDER);
   * gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
   * gridData.heightHint = 24; gridData.widthHint = 24;
   * activeForegroundColorCanvas.setLayoutData(gridData);
   *
   * activeBackgroundColorCanvas = new Canvas(colorFrame, SWT.BORDER);
   * gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
   * gridData.heightHint = 24; gridData.widthHint = 24;
   * activeBackgroundColorCanvas.setLayoutData(gridData);
   *
   * // paletteCanvas final Canvas paletteCanvas = new Canvas(colorFrame,
   * SWT.BORDER | SWT.NO_BACKGROUND); gridData = new
   * GridData(GridData.FILL_HORIZONTAL); gridData.heightHint = 24;
   * paletteCanvas.setLayoutData(gridData);
   * paletteCanvas.addListener(SWT.MouseDown, new Listener() { public void
   * handleEvent(Event e) { Rectangle bounds = paletteCanvas.getClientArea();
   * Color color = getColorAt(bounds, e.x, e.y);
   *
   * if (e.button == 1) setForegroundColor(color); else
   * setBackgroundColor(color); } private Color getColorAt(Rectangle bounds,
   * int x, int y) { if (bounds.height <= 1 && bounds.width <= 1) return
   * paintColorWhite; final int row = (y - bounds.y) * numPaletteRows /
   * bounds.height; final int col = (x - bounds.x) * numPaletteCols /
   * bounds.width; return paintColors[Math.min(Math.max(row * numPaletteCols +
   * col, 0), paintColors.length - 1)]; } }); Listener refreshListener = new
   * Listener() { public void handleEvent(Event e) { if (e.gc == null) return;
   * Rectangle bounds = paletteCanvas.getClientArea(); for (int row = 0; row <
   * numPaletteRows; ++row) { for (int col = 0; col < numPaletteCols; ++col) {
   * final int x = bounds.width * col / numPaletteCols; final int y =
   * bounds.height * row / numPaletteRows; final int width =
   * Math.max(bounds.width * (col + 1) / numPaletteCols - x, 1); final int
   * height = Math.max(bounds.height * (row + 1) / numPaletteRows - y, 1);
   * e.gc.setBackground(paintColors[row * numPaletteCols + col]);
   * e.gc.fillRectangle(bounds.x + x, bounds.y + y, width, height); } } } };
   * paletteCanvas.addListener(SWT.Resize, refreshListener);
   * paletteCanvas.addListener(SWT.Paint, refreshListener); setDefaults(); //
   * paletteCanvas.redraw();
   *
   * }
   *
   * public void setForegroundColor(Color color) { if
   * (activeForegroundColorCanvas != null)
   * activeForegroundColorCanvas.setBackground(color); //
   * toolSettings.commonForegroundColor = color; // updateToolSettings(); }
   *
   * /** Set the tool background color.
   *
   * @param color the new color to use
   *
   * public void setBackgroundColor(Color color) { if
   * (activeBackgroundColorCanvas != null)
   * activeBackgroundColorCanvas.setBackground(color); //
   * toolSettings.commonBackgroundColor = color; // updateToolSettings(); }
   */
}
