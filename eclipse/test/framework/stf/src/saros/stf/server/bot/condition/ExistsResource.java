package saros.stf.server.bot.condition;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

public class ExistsResource extends DefaultCondition {

  private String resourcePath;

  ExistsResource(String resourcePath) {
    this.resourcePath = resourcePath;
  }

  @Override
  public String getFailureMessage() {

    return "Waiting for resource \"" + resourcePath + "\"";
  }

  @Override
  public boolean test() throws Exception {
    IPath path = new Path(resourcePath);
    IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
    if (resource == null) return false;
    return true;
  }
}
