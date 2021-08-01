package saros.gradle.intellij;

import java.io.File;
import java.io.IOException;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.jetbrains.intellij.IntelliJPluginExtension;

/** Gradle task that configures the intellij plugin. */
class IntellijConfigurator {

  private static final String LOCKFILE_NAME = "saros_sandbox.lock";
  private static final String SANDBOX_DIR_PREFIX = "idea-sandbox-";
  private static final String RACE_CONDITION_WARNING =
      "\n"
          + "This issue might be caused by a race condition between two or more 'runIde'calls. In"
          + " order to avoid that race condition: Call 'runIde' and wait until the IntelliJ"
          + " instance startedbefore starting the next instance with 'runIde'.See issue #724 for"
          + " the corresponding discussion.";

  private Project project;

  IntellijConfigurator(Project project) {
    this.project = project;
  }
  /**
   * Configures the intellij plugin and creates tasks which are required in order to run multiple
   * intellij instances via gradle with the {@code runIde} task.
   */
  void configure(SarosIntellijExtension sarosExtension, IntelliJPluginExtension intellijExtension) {
    configureDefaultParams(intellijExtension);
    configureRunIdeIntellijInstallation(intellijExtension, sarosExtension);
  }

  /**
   * Configures the intellij plugin, but also adds actions to the task runIde (provided by the
   * intellij plugin) that lock the running intellij instance and use another intellij config dir if
   * the current dir is locked. This allows to run multiple intellij instances with runIde
   * simultaneously.
   */
  private void configureRunIdeIntellijInstallation(
      IntelliJPluginExtension intellijExtension, SarosIntellijExtension sarosExtension) {

    // Set intellij installation which is used by runIde
    File localIntellijHome = sarosExtension.getLocalIntellijHome();
    if (localIntellijHome != null
        && localIntellijHome.isDirectory()
        && localIntellijHome.exists()) {
      intellijExtension.setLocalPath(localIntellijHome.getAbsolutePath());
    } else {
      intellijExtension.setVersion(sarosExtension.getIntellijVersion());
    }

    // Determine sandbox directory which contains the intellij configurations and logs
    File sandboxDir = determineSandboxDir(sarosExtension);
    intellijExtension.setSandboxDirectory(sandboxDir.getAbsolutePath());

    configureLockFileActions(sandboxDir);
  }

  /** Sets the default parameters of the intellij plugin */
  private void configureDefaultParams(IntelliJPluginExtension intellijExtension) {
    intellijExtension.setUpdateSinceUntilBuild(false);
  }

  private void throwGradleException(String message, Throwable t) {
    throw new GradleException(message + RACE_CONDITION_WARNING, t);
  }

  /**
   * Add tasks that locks the runIde sandbox when the runIde task is started and unlocks the sandbox
   * when runIde is finished.
   */
  private void configureLockFileActions(File sandboxDir) {
    Task runIdeTask = project.getTasks().findByPath("runIde");
    if (runIdeTask == null)
      throwGradleException("Unable to find the task 'runIde' of the intellij plugin.", null);

    runIdeTask.doFirst(
        (Task t) -> {
          try {
            if (!sandboxDir.exists() && !sandboxDir.mkdirs()) {
              throwGradleException(
                  "Unable to create the sandbox directory: " + sandboxDir.getAbsolutePath(), null);
            }

            File lockFile = getLockFile(sandboxDir);
            boolean createdNewFile = lockFile.createNewFile();

            if (!createdNewFile)
              throwGradleException(
                  "Lock file " + lockFile.getAbsolutePath() + " already exists!", null);
          } catch (IOException e) {
            throwGradleException("Unable to lock sandbox: " + sandboxDir.getAbsolutePath(), e);
          }
        });

    runIdeTask.doLast(
        (Task t) -> {
          File lockFile = getLockFile(sandboxDir);

          if (!lockFile.delete())
            throwGradleException("Failed to delete lock file: " + lockFile.getAbsolutePath(), null);
        });
  }

  /** Find next currently unlocked sandbox directory. */
  private File determineSandboxDir(SarosIntellijExtension sarosExtension) {
    File sandboxBaseDir = sarosExtension.getSandboxBaseDir();
    if (sandboxBaseDir == null) {
      sandboxBaseDir = project.getBuildDir();
    }
    if (!sandboxBaseDir.exists() && !sandboxBaseDir.mkdirs()) {
      throwGradleException(
          "Unable to create the sandbox base directory: " + sandboxBaseDir.getAbsolutePath(), null);
    }

    Integer sandboxCount = 1;
    File currentSandboxDir = determineSandboxDirCandidate(sandboxBaseDir, sandboxCount);
    while (isSandboxLocked(currentSandboxDir)) {
      sandboxCount++;
      currentSandboxDir = determineSandboxDirCandidate(sandboxBaseDir, sandboxCount);
    }
    return currentSandboxDir;
  }

  private static File getLockFile(File sandboxDir) {
    return new File(sandboxDir, LOCKFILE_NAME);
  }

  private boolean isSandboxLocked(File sandboxDir) {
    return getLockFile(sandboxDir).exists();
  }

  private File determineSandboxDirCandidate(File sandboxDir, Integer sandboxCount) {
    return new File(sandboxDir, SANDBOX_DIR_PREFIX + sandboxCount);
  }
}
