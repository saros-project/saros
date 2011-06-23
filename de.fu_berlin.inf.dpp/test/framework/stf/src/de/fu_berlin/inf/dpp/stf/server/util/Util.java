package de.fu_berlin.inf.dpp.stf.server.util;

import static de.fu_berlin.inf.dpp.stf.shared.Constants.PKG_REGEX;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.PROJECT_REGEX;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.SRC;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.SUFFIX_JAVA;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;

import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;

public class Util {

    public static TypeOfOS getOS() {
        String osName = System.getProperty("os.name");
        if (osName.matches("Windows.*"))
            return TypeOfOS.WINDOW;
        else if (osName.matches("Mac OS X.*")) {
            return TypeOfOS.MAC;
        }
        return TypeOfOS.WINDOW;
    }

    public enum TypeOfOS {
        MAC, WINDOW
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
        String folderpath = "";
        for (int i = 0; i < nodes.length; i++) {
            if (i == nodes.length - 1) {

                folderpath += nodes[i];
            } else
                folderpath += nodes[i] + "/";
        }
        return folderpath;
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
            char[] buffer = new char[5024];
            try {
                Reader reader = new BufferedReader(new InputStreamReader(is,
                    "UTF-8"));
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

    public static boolean isSame(InputStream input1, InputStream input2)
        throws IOException {
        boolean error = false;
        try {
            byte[] buffer1 = new byte[1024];
            byte[] buffer2 = new byte[1024];
            try {
                int numRead1 = 0;
                int numRead2 = 0;
                while (true) {
                    numRead1 = input1.read(buffer1);
                    numRead2 = input2.read(buffer2);
                    if (numRead1 > -1) {
                        if (numRead2 != numRead1)
                            return false;
                        // Otherwise same number of bytes read
                        if (!Arrays.equals(buffer1, buffer2))
                            return false;
                        // Otherwise same bytes read, so continue ...
                    } else {
                        // Nothing more in stream 1 ...
                        return numRead2 < 0;
                    }
                }
            } finally {
                input1.close();
            }
        } catch (IOException e) {
            error = true;
            throw e;
        } catch (RuntimeException e) {
            error = true;
            throw e;
        } finally {
            try {
                input2.close();
            } catch (IOException e) {
                if (!error)
                    throw e;
            }
        }
    }

    public static String checkInputText(String inputText) {
        char[] chars = inputText.toCharArray();
        String newInputText = "";

        for (char c : chars) {
            if (c == 'y' && SWTBotPreferences.KEYBOARD_LAYOUT.equals("MAC_DE")) {
                newInputText += 'z';
            } else
                newInputText += c;
        }
        return newInputText;
    }

    public static boolean isValidClassPath(String projectName, String pkg,
        String className) {
        boolean isVailid = true;
        isVailid &= projectName.matches(PROJECT_REGEX);
        isVailid &= pkg.matches(PKG_REGEX);
        isVailid &= className.matches("\\w*");
        return isVailid;
    }

    public static String[] getParentNodes(String... nodes) {
        String[] parentNodes = new String[nodes.length - 1];
        for (int i = 0; i < nodes.length - 1; i++) {
            parentNodes[i] = nodes[i];
        }
        return parentNodes;
    }

    public static String getLastNode(String... nodes) {
        return nodes[nodes.length - 1];
    }

    public static String[] splitPkg(String pkg) {
        return pkg.split("\\.");
    }
}
