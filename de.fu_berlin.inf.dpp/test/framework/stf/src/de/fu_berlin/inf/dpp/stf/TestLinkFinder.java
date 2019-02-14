package de.fu_berlin.inf.dpp.stf;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/** @author Stefan Rossbach */
public class TestLinkFinder {

  private static final String STF_TEST_CASE_PACKAGE = "de.fu_berlin.inf.dpp.stf.test";
  private static ClassLoader classLoader;
  private static File baseDirectory;
  private static Class<?> testLinkAnnotation;
  private static Map<String, String> testCasesToJavaClass;

  public static void main(String... strings) throws IOException, ClassNotFoundException {

    classLoader = ClassLoader.getSystemClassLoader();

    testLinkAnnotation = Class.forName("de.fu_berlin.inf.dpp.stf.annotation.TestLink");

    String className = TestLinkFinder.class.getName().replace(".", "/").concat(".class");

    String classLocationInClassPath = classLoader.getResource(className).toString();

    int idx = classLocationInClassPath.indexOf('!');

    boolean isJarFile = false;

    File file;

    if (idx != -1) {
      // jar:file/...!de/fu_berlin/.../TestLinkFinder.class
      classLocationInClassPath = classLocationInClassPath.substring(4, idx);
      isJarFile = true;
      URI fileLocation = URI.create(classLocationInClassPath);
      file = new File(fileLocation);
    } else {
      // file:/.../de/fu_berlin/.../TestLinkFinder.class
      baseDirectory = new File(URI.create(classLocationInClassPath));
      File classFile = new File(className);

      baseDirectory =
          new File(
              baseDirectory
                  .getPath()
                  .substring(0, baseDirectory.getPath().length() - classFile.getPath().length()));

      file = baseDirectory;
    }

    testCasesToJavaClass = new HashMap<String, String>();

    if (isJarFile) readJarFile(file);
    else readDirectory(file);

    for (Map.Entry<String, String> entry : testCasesToJavaClass.entrySet())
      System.out.println(entry.getValue() + "|" + entry.getKey());
  }

  private static File makeRelative(File file) {
    return new File(file.getPath().substring(baseDirectory.getPath().length() + 1));
  }

  private static void readDirectory(File directory) {
    File[] files = directory.listFiles();

    if (files == null) return;

    for (File file : files) {
      if (file.isDirectory()) readDirectory(file);

      String filename = file.getName();

      if (filename.endsWith(".class")) {
        filename = makeRelative(file).getPath();
        String className =
            filename.replace("\\", "/").replace("/", ".").replaceAll("\\.class$", "");

        if (!className.contains(STF_TEST_CASE_PACKAGE)) continue;

        Class<?> clazz;

        try {
          clazz = classLoader.loadClass(className);
        } catch (Throwable t) {
          System.err.println("ERROR while loading class '" + className + "', " + t.getMessage());
          continue;
        }

        processAnnotation(clazz);
      }
    }
  }

  private static void readJarFile(File file) throws IOException {
    JarFile jarFile = new JarFile(file);

    for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements(); ) {
      JarEntry entry = entries.nextElement();

      if (entry.isDirectory()) continue;

      String entryName = entry.getName();

      if (!entryName.endsWith(".class")) continue;

      String className = entryName.replace("\\", "/").replace("/", ".").replaceAll("\\.class$", "");

      if (!className.contains(STF_TEST_CASE_PACKAGE)) continue;

      Class<?> clazz;

      try {
        clazz = classLoader.loadClass(className);
      } catch (Throwable t) {
        System.err.println("ERROR while loading class '" + className + "', " + t.getMessage());
        continue;
      }

      processAnnotation(clazz);
    }

    jarFile.close();
  }

  public static void processAnnotation(Class<?> clazz) {

    try {
      for (Annotation annotation : clazz.getAnnotations()) {

        if (!testLinkAnnotation.isAssignableFrom(annotation.getClass())) continue;

        Method id = annotation.getClass().getMethod("id");
        String className = clazz.getName();
        String testLinkId = id.invoke(annotation).toString();
        testLinkId = testLinkId.trim();

        if (testLinkId.trim().length() == 0) {
          System.err.println("ERROR class '" + className + "' contains an empty test link id");
        } else if (!testLinkId.matches("[-\\w]++")) {
          System.err.println(
              "ERROR test link id '"
                  + testLinkId
                  + "' of class '"
                  + className
                  + "' contains invalid character(s)");
        } else if (testCasesToJavaClass.containsKey(testLinkId)) {
          System.err.print(
              "ERROR found duplicate test link id + '" + testLinkId + "' on different classes: ");
          System.err.println(className + ", " + testCasesToJavaClass.get(testLinkId));
        } else testCasesToJavaClass.put(testLinkId, className);

        break;
      }
    } catch (Throwable t) {
      System.err.println(
          "ERROR while reading annotations of class '" + clazz + "', " + t.getMessage());
    }
  }
}
