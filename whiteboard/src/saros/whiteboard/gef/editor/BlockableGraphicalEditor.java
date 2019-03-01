package saros.whiteboard.gef.editor;

import java.util.Iterator;
import org.eclipse.gef.Disposable;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.Tool;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.UpdateAction;
import org.eclipse.gef.ui.parts.GraphicalEditorWithPalette;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.widgets.Event;
import saros.whiteboard.gef.tools.PanningTool;

/**
 * An extension of the GEF graphical editor with a locking feature using the setEnabled() method.
 *
 * <p>It uses a custom EditDomain and a custom ActionRegistry to block all currently possible edits.
 * Zoom and panning tools will work though.
 *
 * @author jurke
 */
public abstract class BlockableGraphicalEditor extends GraphicalEditorWithPalette {

  private BlockableActionRegistry actionRegistry;
  private boolean enabled = true;
  private EditDomain previousEditDomain;
  private final EditDomain lockedEditDomain = new LockedEditDomain();

  @Override
  protected ActionRegistry getActionRegistry() {
    if (actionRegistry == null) actionRegistry = new BlockableActionRegistry();
    return actionRegistry;
  }

  @Override
  protected void setActionRegistry(ActionRegistry registry) {
    throw new UnsupportedOperationException("Cannot change the action registry");
  }

  /**
   * Disables editing for this graphical editor.
   *
   * <p>If set to false, it will disable all registered actions and replace the EditDomain with a
   * blocked one.
   *
   * @param enable
   */
  public void setEnabled(boolean enable) {
    if (enable == enabled) return;

    enabled = enable;

    if (previousEditDomain == null) previousEditDomain = getGraphicalViewer().getEditDomain();

    // Deselect on disable
    if (!enabled) {
      getGraphicalViewer().deselectAll();
      getGraphicalViewer().setEditDomain(lockedEditDomain);
    } else getGraphicalViewer().setEditDomain(previousEditDomain);

    // actually just used for select all, other actions auto-update
    actionRegistry.setEnabled(enable);
    // we have to update the registered actions
    updateActions();
  }

  public boolean isEnabled() {
    return enabled;
  }

  protected void updateActions() {
    updateActions(getSelectionActions());
    // to disable re/undo
    commandStackChanged(null);
  }

  /**
   * This custom EditDomain implementation does not return editing commands and the only tools
   * working are panning and the mouse wheel zoom.
   *
   * @author jurke
   */
  protected class LockedEditDomain extends EditDomain {
    @Override
    public Tool getActiveTool() {
      if (previousEditDomain != null && previousEditDomain.getActiveTool() instanceof PanningTool)
        return previousEditDomain.getActiveTool();
      return null;
    }

    @Override
    public CommandStack getCommandStack() {
      return null;
    }

    @Override
    public Tool getDefaultTool() {
      return null;
    }

    @Override
    public void mouseWheelScrolled(Event event, EditPartViewer viewer) {
      if (previousEditDomain != null) {
        Tool tool = previousEditDomain.getActiveTool();
        if (tool != null) tool.mouseWheelScrolled(event, viewer);
      }
    }
  }

  /**
   * This custom ActionRegistry can be disabled and by this it will block all registered actions.
   *
   * <p>This is done using delegation. We can do this because in the whole GEF API the only usage of
   * instanceof or a cast with actions is the {@link UpdateAction} that is incorporated here as
   * well.
   *
   * @author jurke
   */
  protected class BlockableActionRegistry extends ActionRegistry {

    @Override
    public void registerAction(IAction action) {
      super.registerAction(getBlockable(action));
    }

    @SuppressWarnings("rawtypes")
    public void setEnabled(boolean enabled) {
      Iterator actions = getActions();
      while (actions.hasNext()) {
        IAction action = (IAction) actions.next();
        action.setEnabled(enabled);
      }
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void dispose() {
      Iterator actions = getActions();
      while (actions.hasNext()) {
        BlockableAction action = (BlockableAction) actions.next();
        if (action.getDelegate() instanceof Disposable)
          ((Disposable) action.getDelegate()).dispose();
      }
    }

    /**
     * @param action
     * @return the action encapsulated in a BlockableAction
     */
    protected IAction getBlockable(IAction action) {
      if (action instanceof UpdateAction) return new BlockableUpdateAction((UpdateAction) action);
      return new BlockableAction(action);
    }

    /**
     * UpdateAction is the only interface in the GEF API where instanceof/casting is used in context
     * with an action.
     *
     * @author jurke
     */
    protected class BlockableUpdateAction extends BlockableAction implements UpdateAction {

      public BlockableUpdateAction(UpdateAction action) {
        super((IAction) action);
      }

      @Override
      public void update() {
        if (!enabled) {
          setEnabled(false);
          return;
        }
        ((UpdateAction) getDelegate()).update();
      }
    }

    /**
     * Delegates an action to force a working blocking feature for observers.
     *
     * @author jurke
     */
    /*
     * The alternative would be to extend every single action overriding the
     * isEnabled() method, checking for blocking and update on change. This
     * would require much more difficult code and cause a lot of
     * duplication. But the main drawback would be that if any subclass
     * omits the additional check the action would remain executable for a
     * blocked editor.
     */
    protected class BlockableAction implements IAction {

      private final IAction delegate;

      public BlockableAction(IAction action) {
        this.delegate = action;
      }

      protected IAction getDelegate() {
        return delegate;
      }

      @Override
      public void addPropertyChangeListener(IPropertyChangeListener listener) {
        delegate.addPropertyChangeListener(listener);
      }

      @Override
      public int getAccelerator() {
        return delegate.getAccelerator();
      }

      @Override
      public String getActionDefinitionId() {
        return delegate.getActionDefinitionId();
      }

      @Override
      public String getDescription() {
        return delegate.getDescription();
      }

      @Override
      public ImageDescriptor getDisabledImageDescriptor() {
        return delegate.getDisabledImageDescriptor();
      }

      @Override
      public HelpListener getHelpListener() {
        return delegate.getHelpListener();
      }

      @Override
      public ImageDescriptor getHoverImageDescriptor() {
        return delegate.getHoverImageDescriptor();
      }

      @Override
      public String getId() {
        return delegate.getId();
      }

      @Override
      public ImageDescriptor getImageDescriptor() {
        return delegate.getImageDescriptor();
      }

      @Override
      public IMenuCreator getMenuCreator() {
        return delegate.getMenuCreator();
      }

      @Override
      public int getStyle() {
        return delegate.getStyle();
      }

      @Override
      public String getText() {
        return delegate.getText();
      }

      @Override
      public String getToolTipText() {
        return delegate.getToolTipText();
      }

      @Override
      public boolean isChecked() {
        return delegate.isChecked();
      }

      @Override
      public boolean isEnabled() {
        if (!enabled) {
          setEnabled(false);
          return false;
        }
        return delegate.isEnabled();
      }

      @Override
      public boolean isHandled() {
        return delegate.isHandled();
      }

      @Override
      public void removePropertyChangeListener(IPropertyChangeListener listener) {
        delegate.removePropertyChangeListener(listener);
      }

      @Override
      public void run() {
        delegate.run();
      }

      @Override
      public void runWithEvent(Event event) {
        delegate.runWithEvent(event);
      }

      @Override
      public void setActionDefinitionId(String id) {
        delegate.setActionDefinitionId(id);
      }

      @Override
      public void setChecked(boolean checked) {
        delegate.setChecked(checked);
      }

      @Override
      public void setDescription(String text) {
        delegate.setDescription(text);
      }

      @Override
      public void setDisabledImageDescriptor(ImageDescriptor newImage) {
        delegate.setDisabledImageDescriptor(newImage);
      }

      @Override
      public void setEnabled(boolean enabled) {
        delegate.setEnabled(enabled);
      }

      @Override
      public void setHelpListener(HelpListener listener) {
        delegate.setHelpListener(listener);
      }

      @Override
      public void setHoverImageDescriptor(ImageDescriptor newImage) {
        delegate.setHoverImageDescriptor(newImage);
      }

      @Override
      public void setId(String id) {
        delegate.setId(id);
      }

      @Override
      public void setImageDescriptor(ImageDescriptor newImage) {
        delegate.setImageDescriptor(newImage);
      }

      @Override
      public void setMenuCreator(IMenuCreator creator) {
        delegate.setMenuCreator(creator);
      }

      @Override
      public void setText(String text) {
        delegate.setText(text);
      }

      @Override
      public void setToolTipText(String text) {
        delegate.setToolTipText(text);
      }

      @Override
      public void setAccelerator(int keycode) {
        delegate.setAccelerator(keycode);
      }
    }
  }
}
