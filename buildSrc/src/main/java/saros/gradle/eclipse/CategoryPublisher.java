package saros.gradle.eclipse;

import com.diffplug.gradle.FileMisc;
import com.diffplug.gradle.eclipserunner.EclipseApp;
import com.diffplug.gradle.pde.EclipseRelease;
import com.diffplug.gradle.pde.PdeInstallation;
import java.io.File;

public class CategoryPublisher extends EclipseApp {

  private final String eclipseVersion;

  public CategoryPublisher(String eclipseVersion) {
    super("org.eclipse.equinox.p2.publisher.CategoryPublisher");
    this.eclipseVersion = eclipseVersion;
  }

  public void metadataRepository(File file) {
    addArg("metadataRepository", FileMisc.asUrl(file));
  }

  public void categoryDefinition(File file) {
    addArg("categoryDefinition", FileMisc.asUrl(file));
  }

  public void runUsingPdeInstallation() throws Exception {
    runUsing(PdeInstallation.from(EclipseRelease.official(eclipseVersion)));
  }
}
