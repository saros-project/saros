package saros.ui.views;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuSeparator;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.viewers.ISelection;
import saros.net.xmpp.JID;
import saros.ui.Messages;
import saros.ui.util.CollaborationUtils;
import saros.ui.util.WizardUtils;
import saros.ui.util.selection.SelectionUtils;
import saros.ui.wizards.StartSessionWizard;

/**
 * Creates a context menu filled with entries, each of which represents a project that currently
 * exists in the workspace. Each entry will trigger a session negotiation with the given project.
 *
 * <p>In addition, if multiple projects are present in the workspace, an additional entry will be
 * displayed which will open a {@linkplain StartSessionWizard wizard} instead.
 */
public class StartSessionWithProjects {

  @Inject
  public StartSessionWithProjects(
      EModelService service,
      @Named(IServiceConstants.ACTIVE_SELECTION) ISelection activeSelection) {
    this.service = service;
    selection = activeSelection;
  }

  static class MultipleProjectMenuItemHandler {

    @Execute
    public void execute() {
      WizardUtils.openStartSessionWizard(null);
    }
  }

  static class ProjectMenuItemHandler {

    @Execute
    public void execute(MDirectMenuItem menuItem) {
      Map<String, Object> objectData = menuItem.getTransientData();
      IProject project = (IProject) objectData.get("project");
      List<JID> contacts = (List<JID>) objectData.get("contacts");

      CollaborationUtils.startSession(Collections.singleton(project), contacts);
    }
  }

  private List<MMenuElement> menuEntries;
  private EModelService service;

  private ISelection selection;

  public void createMenu(List<MMenuElement> menuEntries) {
    this.menuEntries = menuEntries;

    final List<JID> contacts = SelectionUtils.getAdaptableObjects(selection, JID.class);

    final IProject[] projects = getSortedWorkspaceProjects();

    if (projects.length == 0) {
      createNoProjectsMenuItem(0);
      return;
    }

    int idx;

    for (idx = 0; idx < projects.length; idx++) createProjectMenuItem(idx, projects[idx], contacts);

    if (idx > 1) {
      menuEntries.add(idx++, service.createModelElement(MMenuSeparator.class));
      createMultipleProjectMenuItem(idx);
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

  /** Creates a menu entry which shares projects with the given Contacts. */
  private void createProjectMenuItem(
      final int index, final IProject project, final List<JID> contacts) {

    MDirectMenuItem projectMenuItem = service.createModelElement(MDirectMenuItem.class);
    projectMenuItem.setLabel(project.getName());
    projectMenuItem.setContributionURI(
        "bundleclass://saros.eclipse/saros.ui.e4.views.StartSessionWithProjects$MultipleProjectMenuItemHandler");
    // TODO: set icon for menu element, old e3 variant : workbenchLabelProvider.getImage(project)

    Map<String, Object> objectData = projectMenuItem.getTransientData();
    objectData.put("project", project);
    objectData.put("contacts", contacts);

    menuEntries.add(index, projectMenuItem);
  }

  private void createMultipleProjectMenuItem(final int index) {
    MDirectMenuItem multipleProjectMenuItem = service.createModelElement(MDirectMenuItem.class);
    multipleProjectMenuItem.setLabel("Specific resource tree(s)...");
    multipleProjectMenuItem.setContributionURI(
        "bundleclass://saros.eclipse/saros.ui.e4.views.StartSessionWithProjects$MultipleProjectMenuItemHandler");

    menuEntries.add(index, multipleProjectMenuItem);
  }

  /** Creates a menu entry which indicates that no Saros enabled contacts are online. */
  private void createNoProjectsMenuItem(final int index) {
    MDirectMenuItem noProjectsMenuItem = service.createModelElement(MDirectMenuItem.class);
    noProjectsMenuItem.setLabel(Messages.SessionWithProjects_no_projects_in_workspace);
    noProjectsMenuItem.setEnabled(false);

    menuEntries.add(index, noProjectsMenuItem);
  }
}
