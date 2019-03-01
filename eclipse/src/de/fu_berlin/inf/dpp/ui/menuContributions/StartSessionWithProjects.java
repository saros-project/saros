package de.fu_berlin.inf.dpp.ui.menuContributions;

import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.util.CollaborationUtils;
import de.fu_berlin.inf.dpp.ui.util.WizardUtils;
import de.fu_berlin.inf.dpp.ui.util.selection.retriever.SelectionRetrieverFactory;
import de.fu_berlin.inf.dpp.ui.wizards.StartSessionWizard;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.jivesoftware.smack.RosterEntry;

/**
 * Creates a context menu filled with entries, each of which represents a project that currently
 * exists in the workspace. Each entry will trigger a session negotiation with the given project.
 *
 * <p>In addition, if multiple projects are present in the workspace, an additional entry will be
 * displayed which will open a {@linkplain StartSessionWizard wizard} instead.
 */
public class StartSessionWithProjects extends ContributionItem {

  private final WorkbenchLabelProvider workbenchLabelProvider = new WorkbenchLabelProvider();

  public StartSessionWithProjects() {
    this(null);
  }

  public StartSessionWithProjects(final String id) {
    super(id);
  }

  @Override
  public void fill(final Menu menu, final int index) {
    final List<JID> contacts =
        SelectionRetrieverFactory.getSelectionRetriever(JID.class).getSelection();

    final IProject[] projects = getSortedWorkspaceProjects();

    if (projects.length == 0) {
      createNoProjectsMenuItem(menu, 0);
      return;
    }

    int idx;

    for (idx = 0; idx < projects.length; idx++)
      createProjectMenuItem(menu, idx, projects[idx], contacts);

    if (idx > 1) {
      new MenuItem(menu, SWT.SEPARATOR, idx++);
      createMultipleProjectMenuItem(menu, idx);
    }
  }

  /** Returns a sorted list of all {@link IProject}s in the {@link IWorkspace}. */
  private IProject[] getSortedWorkspaceProjects() {
    final IProject[] workspaceProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();

    Arrays.sort(
        workspaceProjects,
        new Comparator<IProject>() {
          @Override
          public int compare(final IProject a, final IProject b) {
            return a.getName().compareToIgnoreCase(b.getName());
          }
        });

    return workspaceProjects;
  }

  /** Creates a menu entry which shares projects with the given {@link RosterEntry}. */
  private MenuItem createProjectMenuItem(
      final Menu parentMenu, final int index, final IProject project, final List<JID> contacts) {

    final MenuItem menuItem = new MenuItem(parentMenu, SWT.NONE, index);

    menuItem.setText(workbenchLabelProvider.getText(project));
    menuItem.setImage(workbenchLabelProvider.getImage(project));

    menuItem.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            CollaborationUtils.startSession(
                Collections.<IResource>singletonList(project), contacts);
          }
        });

    return menuItem;
  }

  private MenuItem createMultipleProjectMenuItem(final Menu parentMenu, final int index) {

    final MenuItem menuItem = new MenuItem(parentMenu, SWT.NONE, index);

    menuItem.setText("Multiple Projects...");
    menuItem.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            WizardUtils.openStartSessionWizard(null);
          }
        });

    return menuItem;
  }

  /** Creates a menu entry which indicates that no Saros enabled contacts are online. */
  private MenuItem createNoProjectsMenuItem(final Menu parentMenu, final int index) {

    final MenuItem menuItem = new MenuItem(parentMenu, SWT.NONE, index);
    menuItem.setText(Messages.SessionWithProjects_no_projects_in_workspace);
    menuItem.setEnabled(false);
    return menuItem;
  }
}
