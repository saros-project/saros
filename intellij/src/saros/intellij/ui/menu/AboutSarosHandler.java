package de.fu_berlin.inf.dpp.intellij.ui.menu;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

/** About menu action. */
public class AboutSarosHandler extends AnAction {
  @Override
  public void actionPerformed(AnActionEvent e) {
    Project project = e.getData(PlatformDataKeys.PROJECT);

    Messages.showMessageDialog(
        project,
        "Saros plugin for IntelliJ. \n Still under Development.",
        "About Saros",
        Messages.getInformationIcon());
  }
}
