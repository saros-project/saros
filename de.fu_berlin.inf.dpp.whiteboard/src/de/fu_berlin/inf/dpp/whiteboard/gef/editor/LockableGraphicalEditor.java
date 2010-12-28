package de.fu_berlin.inf.dpp.whiteboard.gef.editor;

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

import de.fu_berlin.inf.dpp.whiteboard.gef.tools.PanningTool;

public abstract class LockableGraphicalEditor extends
		GraphicalEditorWithPalette {

	private LockableActionRegistry actionRegistry;
	private boolean enabled = true;
	private EditDomain previousEditDomain;
	private final EditDomain lockedEditDomain = new LockedEditDomain();

	@Override
	protected ActionRegistry getActionRegistry() {
		if (actionRegistry == null)
			actionRegistry = new LockableActionRegistry();
		return actionRegistry;
	}

	@Override
	protected void setActionRegistry(ActionRegistry registry) {
		throw new UnsupportedOperationException(
				"Cannot change the action registry");
	}

	public void setEnabled(boolean enable) {
		if (enable == enabled)
			return;

		enabled = enable;

		if (previousEditDomain == null)
			previousEditDomain = getGraphicalViewer().getEditDomain();

		// Deselect on disable
		if (!enabled) {
			getGraphicalViewer().deselectAll();
			getGraphicalViewer().setEditDomain(lockedEditDomain);
		} else
			getGraphicalViewer().setEditDomain(previousEditDomain);

		// actually just used for select all
		actionRegistry.setEnabled(enable);
	}

	public boolean isEnabled() {
		return enabled;
	}

	private class LockedEditDomain extends EditDomain {
		@Override
		public Tool getActiveTool() {
			if (previousEditDomain != null
					&& previousEditDomain.getActiveTool() instanceof PanningTool)
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
				if (tool != null)
					tool.mouseWheelScrolled(event, viewer);
			}
		}
	}

	private class LockableActionRegistry extends ActionRegistry {

		@Override
		public void registerAction(IAction action) {
			super.registerAction(getLockable(action));
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
				LockableAction action = (LockableAction) actions.next();
				if (action.getDelegate() instanceof Disposable)
					((Disposable) action.getDelegate()).dispose();
			}
		}

		protected IAction getLockable(IAction action) {
			if (action instanceof UpdateAction)
				return new LockableUpdateAction((UpdateAction) action);
			return new LockableAction(action);
		}

		protected class LockableUpdateAction extends LockableAction implements
				UpdateAction {

			public LockableUpdateAction(UpdateAction action) {
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

		protected class LockableAction implements IAction {

			private final IAction delegate;

			public LockableAction(IAction action) {
				this.delegate = action;
			}

			protected IAction getDelegate() {
				return delegate;
			}

			@Override
			public void addPropertyChangeListener(
					IPropertyChangeListener listener) {
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
			public void removePropertyChangeListener(
					IPropertyChangeListener listener) {
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
