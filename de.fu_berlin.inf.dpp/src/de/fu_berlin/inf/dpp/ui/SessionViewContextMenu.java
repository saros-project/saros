package de.fu_berlin.inf.dpp.ui;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.ui.actions.ChangeColorAction;
import de.fu_berlin.inf.dpp.ui.actions.FollowThisPersonAction;
import de.fu_berlin.inf.dpp.ui.actions.GiveDriverRoleAction;
import de.fu_berlin.inf.dpp.ui.actions.GiveExclusiveDriverRoleAction;
import de.fu_berlin.inf.dpp.ui.actions.JumpToDriverPositionAction;
import de.fu_berlin.inf.dpp.ui.actions.RemoveDriverRoleAction;

/**
 * This is the ContextMenu for the SessionView
 */
@Component(module = "ui")
public class SessionViewContextMenu {

    public SessionViewContextMenu(ViewPart sessionView, TableViewer viewer,
        final ChangeColorAction changedColourAction,
        final JumpToDriverPositionAction jumpAction,
        final RemoveDriverRoleAction removeDriverAction,
        final GiveExclusiveDriverRoleAction exclusiveDriverAction,
        final GiveDriverRoleAction giveDriverRoleAction,
        final FollowThisPersonAction followAction,
        final PreferenceUtils preferences) {

        MenuManager manager = new MenuManager("#PopupMenu");
        manager.setRemoveAllWhenShown(true);
        manager.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                manager.add(exclusiveDriverAction);
                if (preferences.isMultiDriverEnabled()) {
                    manager.add(giveDriverRoleAction);
                    manager.add(removeDriverAction);
                }
                manager.add(new Separator());
                manager.add(followAction);
                manager.add(jumpAction);

                manager.add(new Separator());
                manager.add(changedColourAction);
                
                // Other plug-ins can contribute their actions here
                manager.add(new Separator(
                    IWorkbenchActionConstants.MB_ADDITIONS));
            }
        });

        Menu menu = manager.createContextMenu(viewer.getControl());
        viewer.getControl().setMenu(menu);

        sessionView.getSite().registerContextMenu(manager, viewer);

        viewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                if (jumpAction.isEnabled()) {
                    jumpAction.run();
                }
            }
        });
    }
}
