package saros.intellij.ui.swt_browser;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SwtLibLoaderTest {

  private static final String JAR_FILENAME_OSX_64 = "swt-4.4-cocoa-macosx-x86_64.jar";
  private static final String JAR_FILENAME_OSX_32 = "swt-4.4-cocoa-macosx-x86.jar";
  private static final String JAR_FILENAME_LINUX_64 = "swt-4.4-gtk-linux-x86_64.jar";
  private static final String JAR_FILENAME_LINUX_32 = "swt-4.4-gtk-linux-x86.jar";
  private static final String JAR_FILENAME_WIN_64 = "swt-4.4-win32-x86_64.jar";
  private static final String JAR_FILENAME_WIN_32 = "swt-4.4-win32-x86.jar";

  @Test
  public void createJarFilename() {
    assertEquals(JAR_FILENAME_OSX_32, SwtLibLoader.getJarFilename("mac", "86"));
    assertEquals(JAR_FILENAME_OSX_64, SwtLibLoader.getJarFilename("mac", "64"));
    assertEquals(JAR_FILENAME_LINUX_32, SwtLibLoader.getJarFilename("linux", "86"));
    assertEquals(JAR_FILENAME_LINUX_64, SwtLibLoader.getJarFilename("linux", "64"));
    assertEquals(JAR_FILENAME_WIN_32, SwtLibLoader.getJarFilename("win", "86"));
    assertEquals(JAR_FILENAME_WIN_64, SwtLibLoader.getJarFilename("win", "64"));
  }
}
