/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package de.fu_berlin.inf.dpp.ui;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;

import de.fu_berlin.inf.dpp.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.ISharedProject;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SessionManager;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.listeners.ISessionListener;
import de.fu_berlin.inf.dpp.listeners.ISharedProjectListener;
import de.fu_berlin.inf.dpp.ui.actions.GiveDriverRoleAction;
import de.fu_berlin.inf.dpp.ui.actions.JumpToDriverPositionAction;
import de.fu_berlin.inf.dpp.ui.actions.LeaveSessionAction;
import de.fu_berlin.inf.dpp.ui.actions.TakeDriverRoleAction;
import de.fu_berlin.inf.dpp.xmpp.JID;

public class SessionView extends ViewPart implements ISessionListener {
    private TableViewer          viewer;

    private ISharedProject       sharedProject;


    private class SessionContentProvider implements IStructuredContentProvider, 
        ISharedProjectListener {
        
        private TableViewer    tableViewer;
        
        public void inputChanged(Viewer v, Object oldInput, Object newInput) {
            if (oldInput != null) {
                ISharedProject oldProject = (ISharedProject)oldInput;
                oldProject.removeListener(this);
            }
            
            sharedProject = (ISharedProject)newInput;
            if (sharedProject != null) {
                sharedProject.addListener(this);
            }
            
            tableViewer = (TableViewer)v;
            tableViewer.refresh();
            
            updateEnablement();
        }

        public Object[] getElements(Object parent) {
            if (sharedProject != null) {
                return sharedProject.getParticipants().toArray();
            }

            return new Object[]{};
        }

        public void driverChanged(JID driver, boolean replicated) {
            refreshTable();
        }
        
        public void driverPathChanged(IPath path, boolean replicated) {
            // ignore
        }
        
        public void userJoined(JID user) {
            refreshTable();
        }

        public void userLeft(JID user) {
            refreshTable();
        }

        public void dispose() {
        }

        private void refreshTable() {
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    tableViewer.refresh();
                }
            });
        }
    }

    private class SessionLabelProvider extends LabelProvider implements ITableLabelProvider {
        private Image userImage   = SarosUI.getImage("icons/user.png");
        private Image driverImage = SarosUI.getImage("icons/user_edit.png");
        
        public String getColumnText(Object obj, int index) {
            User participant = (User)obj;
            
            StringBuffer sb = new StringBuffer(participant.getJid().getName());
            if (participant.equals(sharedProject.getDriver())) {
                sb.append(" (Driver)");
            }
            
            return sb.toString();
        }

        @Override
        public Image getImage(Object obj) {
            User user = (User)obj;
            return user.equals(sharedProject.getDriver()) ? driverImage : userImage;  
        }
        
        public Image getColumnImage(Object obj, int index) {
            return getImage(obj);
        }
    }

    /**
     * The constructor.
     */
    public SessionView() {
    }

    public void sessionStarted(final ISharedProject session) {
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                viewer.setInput(session);
            }
        });
    }

    public void sessionEnded(ISharedProject session) {
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                viewer.setInput(null);
            }
        });
    }

    public void invitationReceived(IIncomingInvitationProcess process) {
        // ignore
    }

    /**
     * This is a callback that will allow us to create the viewer and initialize
     * it.
     */
    public void createPartControl(Composite parent) {
        viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        viewer.setContentProvider(new SessionContentProvider());
        viewer.setLabelProvider(new SessionLabelProvider());
        viewer.setInput(null);

        contributeToActionBars();
        hookContextMenu();
        attachSessionListener();
        updateEnablement();
        
        setPartName("Shared Project Session");
    }
    
    /**
     * Passing the focus request to the viewer's control.
     */
    public void setFocus() {
        viewer.getControl().setFocus();
    }
    
    /**
     * Needs to called from the UI thread.
     */
    private void updateEnablement() {
        viewer.getControl().setEnabled(sharedProject != null);
    }
    
    private void attachSessionListener() {
        SessionManager sessionManager = Saros.getDefault().getSessionManager();

        sessionManager.addSessionListener(this);
        if (sessionManager.getSharedProject() != null) {
            viewer.setInput(sessionManager.getSharedProject());
        }
    }
    
    private void contributeToActionBars() {
        IActionBars bars = getViewSite().getActionBars();
        IToolBarManager toolBar = bars.getToolBarManager();
        
        toolBar.add(new TakeDriverRoleAction());
        toolBar.add(new JumpToDriverPositionAction());
        toolBar.add(new LeaveSessionAction());
    }
    
    private void hookContextMenu() {
        MenuManager menuMgr = new MenuManager("#PopupMenu");
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                SessionView.this.fillContextMenu(manager);
            }
        });
        
        Menu menu = menuMgr.createContextMenu(viewer.getControl());
        
        viewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuMgr, viewer);
    }
    
    private void fillContextMenu(IMenuManager manager) {
        manager.add(new GiveDriverRoleAction(viewer));
        
        // Other plug-ins can contribute there actions here
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }
}