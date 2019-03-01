package saros.annotations;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Arrays;

public class ComponentFinder {

  private static File startDirectory;

  public static void main(String... args) throws Exception {

    String className = ComponentFinder.class.getName().replace(".", "/").concat(".class");

    URL location = ClassLoader.getSystemClassLoader().getResource(className);

    File directory = new File(location.toURI());
    File file = new File(className);

    startDirectory =
        new File(
            directory
                .getPath()
                .substring(0, directory.getPath().length() - file.getPath().length()));

    readDirectory(startDirectory);
  }

  private static File makeRelative(File file) {
    return new File(file.getPath().substring(startDirectory.getPath().length() + 1));
  }

  private static void readDirectory(File directory) {
    File[] files = directory.listFiles();

    if (files == null) return;

    for (File file : files) {
      if (file.isDirectory()) readDirectory(file);

      String filename = file.getName();

      if (filename.endsWith(".class")) {
        filename = makeRelative(file).getPath();
        String className = filename.replace("\\", "/").replace("/", ".").replaceAll("\\.class", "");

        Class<?> clazz;

        try {
          clazz = ClassLoader.getSystemClassLoader().loadClass(className);
        } catch (Throwable t) {
          continue;
        }

        describeClass(clazz);
      }
    }
  }

  private static void describeClass(Class<?> clazz) {
    if (clazz.isAnnotationPresent(Component.class)) {
      Annotation annotation = clazz.getAnnotation(Component.class);
      boolean isStandAlone = false;
      System.out.println("#" + annotation);
      Class<?>[] interfaces = clazz.getInterfaces();
      if (interfaces.length == 1) System.out.print(interfaces[0].getName());
      else if (interfaces.length > 1)
        System.out.print("# FIX THIS -> " + Arrays.asList(interfaces));
      else isStandAlone = true;

      if (isStandAlone) System.out.println(clazz.getName());
      else System.out.println(" = " + clazz.getName());

      System.out.println();
    }
  }
}
