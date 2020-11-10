package saros.intellij.ui;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import org.jetbrains.annotations.NotNull;
import saros.intellij.SarosComponent;
import saros.intellij.ui.views.SarosMainPanelView;

/** This factory is the starting point of UI of the Saros plugin. */
public class SarosToolWindowFactory implements ToolWindowFactory {

  @Override
  public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
    SarosMainPanelView sarosMainPanelView = new SarosMainPanelView(project);

    IdeaPluginDescriptor sarosPluginDescriptor =
        PluginManagerCore.getPlugin(PluginId.getId(SarosComponent.PLUGIN_ID));

    if (sarosPluginDescriptor == null) {
      throw new IllegalStateException(
          "Could not find Saros plugin using ID " + SarosComponent.PLUGIN_ID);
    }

    Content content =
        toolWindow
            .getContentManager()
            .getFactory()
            .createContent(sarosMainPanelView, sarosPluginDescriptor.getName(), false);
    toolWindow.getContentManager().addContent(content);
  }
}
