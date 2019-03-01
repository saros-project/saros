package saros.intellij.ui;

import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import saros.intellij.SarosComponent;
import saros.intellij.ui.views.SarosMainPanelView;

/** This factory is the starting point of UI of the Saros plugin. */
public class SarosToolWindowFactory implements ToolWindowFactory {

  private SarosMainPanelView sarosMainPanelView;

  @Override
  public void createToolWindowContent(Project project, ToolWindow toolWindow) {
    sarosMainPanelView = new SarosMainPanelView();

    Content content =
        toolWindow
            .getContentManager()
            .getFactory()
            .createContent(
                sarosMainPanelView,
                PluginManager.getPlugin(PluginId.getId(SarosComponent.PLUGIN_ID)).getName(),
                false);
    toolWindow.getContentManager().addContent(content);
  }
}
