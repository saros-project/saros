package de.fu_berlin.inf.dpp.ui.widgets.viewer.project;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.ui.model.project.ProjectOnlyWorkbenchContentProvider;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.ViewerComposite;
import de.fu_berlin.inf.dpp.util.ArrayUtils;
import de.fu_berlin.inf.nebula.utils.LayoutUtils;

/**
 * This {@link Composite} displays all {@link IProject}s within the
 * {@link IWorkbench}.
 * <p>
 * This composite does <strong>NOT</strong> handle setting the layout.
 * 
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>NONE and those supported by {@link StructuredViewer}</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * 
 * @author bkahlert
 * 
 */
public class ProjectDisplayComposite<T extends TableViewer> extends
    ViewerComposite<T> {

    public ProjectDisplayComposite(Composite parent, int style) {
        super(parent, style);

        SarosPluginContext.initComponent(this);

        super.setLayout(LayoutUtils.createGridLayout());
        getViewer().getControl()
            .setLayoutData(LayoutUtils.createFillGridData());
        getViewer().setInput(ResourcesPlugin.getWorkspace());
    }

    @SuppressWarnings("unchecked")
    @Override
    protected T createViewer(int style) {
        return (T) new TableViewer(new Table(this, style));
    }

    @Override
    protected void configureViewer(T viewer) {
        viewer.setContentProvider(new ProjectOnlyWorkbenchContentProvider());
        viewer.setLabelProvider(WorkbenchLabelProvider
            .getDecoratingWorkbenchLabelProvider());
        viewer.setUseHashlookup(true);
    }

    /**
     * Returns the displayed {@link IProject}s.
     * 
     * @return
     */
    public List<IProject> getProjects() {
        WorkbenchContentProvider contentProvider = (WorkbenchContentProvider) getViewer()
            .getContentProvider();
        Object[] objects = contentProvider.getElements(getViewer().getInput());
        return ArrayUtils.getAdaptableObjects(objects, IProject.class,
            Platform.getAdapterManager());
    }

    @Override
    public void setLayout(Layout layout) {
        // ignore
    }
}
