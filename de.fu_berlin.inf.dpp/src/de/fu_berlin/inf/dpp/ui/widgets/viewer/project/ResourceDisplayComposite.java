package de.fu_berlin.inf.dpp.ui.widgets.viewer.project;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.ViewerComposite;
import de.fu_berlin.inf.dpp.util.ArrayUtils;
import de.fu_berlin.inf.nebula.utils.LayoutUtils;

/**
 * This {@link Composite} displays all {@link IResource}s within the
 * {@link IWorkbench} as {@link TreeViewer}.
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
 * @author kheld
 * 
 */
public class ResourceDisplayComposite<T extends TreeViewer> extends
    ViewerComposite<T> {
    @Inject
    protected Saros saros;

    public ResourceDisplayComposite(Composite parent, int style) {
        super(parent, style);

        SarosPluginContext.initComponent(this);

        super.setLayout(LayoutUtils.createGridLayout());

        getViewer().getControl()
            .setLayoutData(LayoutUtils.createFillGridData());
        getViewer().setInput(ResourcesPlugin.getWorkspace().getRoot());
    }

    @SuppressWarnings("unchecked")
    @Override
    protected T createViewer(int style) {
        return (T) new TreeViewer(this, SWT.NONE);
    }

    @Override
    protected void configureViewer(T viewer) {
        viewer.setContentProvider(new WorkbenchContentProvider());
        viewer.setLabelProvider(new WorkbenchLabelProvider());
        viewer.setUseHashlookup(true);
        viewer.setSorter(new WorkbenchItemsSorter());
    }

    /**
     * Returns the displayed Project {@link IResource}s.
     * 
     * @return
     */
    public List<IResource> getResources() {
        WorkbenchContentProvider contentProvider = (WorkbenchContentProvider) getViewer()
            .getContentProvider();

        Object[] objects = contentProvider.getElements(getViewer().getInput());
        return ArrayUtils.getAdaptableObjects(objects, IResource.class,
            Platform.getAdapterManager());
    }

    /**
     * Returns the displayed {@link IProject}s.
     * 
     * @return
     */
    public int getProjectsCount() {
        WorkbenchContentProvider contentProvider = (WorkbenchContentProvider) getViewer()
            .getContentProvider();

        Object[] objects = contentProvider.getElements(getViewer().getInput());
        return ArrayUtils.getAdaptableObjects(objects, IProject.class,
            Platform.getAdapterManager()).size();
    }

    @Override
    public void setLayout(Layout layout) {
        // ignore
    }
}
