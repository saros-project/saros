package de.fu_berlin.inf.dpp.intellij.ui;

import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import de.fu_berlin.inf.dpp.intellij.ui.views.SarosMainPanelView;

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
                PluginManager.getPlugin(PluginId.getId("de.fu_berlin.inf.dpp.intellij")).getName(),
                false);
    toolWindow.getContentManager().addContent(content);
  }
}
