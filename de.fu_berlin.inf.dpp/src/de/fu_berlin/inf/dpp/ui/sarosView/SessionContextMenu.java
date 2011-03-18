package de.fu_berlin.inf.dpp.ui.sarosView;

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
import de.fu_berlin.inf.dpp.ui.actions.GiveWriteAccessAction;
import de.fu_berlin.inf.dpp.ui.actions.JumpToUserWithWriteAccessPositionAction;
import de.fu_berlin.inf.dpp.ui.actions.RestrictToReadOnlyAccessAction;
import de.fu_berlin.inf.dpp.ui.actions.SendFileAction;
import de.fu_berlin.inf.dpp.ui.actions.VideoSharingAction;
import de.fu_berlin.inf.dpp.ui.actions.VoIPAction;

/**
 * This is the ContextMenu for the SessionView
 */
@Component(module = "ui")
public class SessionContextMenu {

    /*
     * TODO Actions are created by PicoContainer for now. See TODO in SarosView
     */
    public SessionContextMenu(ViewPart sessionPart, TableViewer viewer,
        final ChangeColorAction changedColourAction,
        final JumpToUserWithWriteAccessPositionAction jumpAction,
        final RestrictToReadOnlyAccessAction restrictToReadOnlyAccessAction,
        final GiveWriteAccessAction giveWriteAccessAction,
        final FollowThisPersonAction followAction,
        final SendFileAction sendFileAction,
        final VideoSharingAction videoSharingAction,
        final VoIPAction voipAction, final PreferenceUtils preferences) {

        MenuManager manager = new MenuManager("#PopupMenu");
        manager.setRemoveAllWhenShown(true);
        manager.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                manager.add(giveWriteAccessAction);
                manager.add(restrictToReadOnlyAccessAction);
                manager.add(new Separator());
                manager.add(followAction);
                manager.add(jumpAction);
                manager.add(new Separator());
                manager.add(sendFileAction);
                manager.add(videoSharingAction);
                manager.add(voipAction);
                manager.add(new Separator());
                manager.add(changedColourAction);

                // Other plug-ins can contribute their actions here
                manager.add(new Separator(
                    IWorkbenchActionConstants.MB_ADDITIONS));
            }
        });

        Menu menu = manager.createContextMenu(viewer.getControl());
        viewer.getControl().setMenu(menu);

        sessionPart.getSite().registerContextMenu(manager, viewer);

        viewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                if (jumpAction.isEnabled()) {
                    jumpAction.run();
                }
            }
        });
    }
}
