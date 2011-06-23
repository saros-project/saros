package de.fu_berlin.inf.dpp.stf.server.util;

import static de.fu_berlin.inf.dpp.stf.shared.Constants.SRC;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.SUFFIX_JAVA;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

public class Util {

    public enum OperatingSystem {
        MAC, WINDOW, UNIX
    }

    private static final OperatingSystem OS_TYPE;

    static {
        String osName = System.getProperty("os.name");
        if (osName.matches("Windows.*"))
            OS_TYPE = OperatingSystem.WINDOW;
        else if (osName.matches("Mac OS X.*")) {
            OS_TYPE = OperatingSystem.MAC;
        } else
            OS_TYPE = OperatingSystem.UNIX;
    }

    public static OperatingSystem getOperatingSystem() {
        return OS_TYPE;
    }

    public static String getClassPath(String projectName, String pkg,
        String className) {
        return projectName + "/src/" + pkg.replaceAll("\\.", "/") + "/"
            + className + ".java";
    }

    public static String getPkgPath(String projectName, String pkg) {
        return projectName + "/src/" + pkg.replaceAll("\\.", "/");
    }

    public static String[] getClassNodes(String projectName, String pkg,
        String className) {
        String[] nodes = { projectName, SRC, pkg, className + SUFFIX_JAVA };
        return nodes;
    }

    public static String[] getPkgNodes(String projectName, String pkg) {
        String[] nodes = { projectName, SRC, pkg };
        return nodes;
    }

    public static String getPath(String... nodes) {
        StringBuilder builder = new StringBuilder();
        int i = 0;

        for (; i < nodes.length - 1; i++)
            builder.append(nodes[i]).append('/');

        builder.append(nodes[i]);

        return builder.toString();
    }

    public static String changeToRegex(String text) {
        // the name of project in SVN_control contains special characters, which
        // should be filtered.
        String[] names = text.split(" ");
        if (names.length > 1) {
            text = names[0];
        }
        return text + ".*";
    }

    public static String[] changeToRegex(String... texts) {
        String[] matchTexts = new String[texts.length];
        for (int i = 0; i < texts.length; i++) {
            matchTexts[i] = texts[i] + "( .*)?";
        }
        return matchTexts;
    }

    public static String convertStreamToString(InputStream is)
        throws IOException {
        if (is != null) {
            Writer writer = new StringWriter();
            char[] buffer = new char[8192];
            try {
                Reader reader = new InputStreamReader(is, "UTF-8");
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                is.close();
                writer.close();
            }
            return writer.toString();
        } else {
            return "";
        }
    }
}
