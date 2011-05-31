package de.fu_berlin.inf.dpp.stf.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.swtbot.swt.finder.utils.FileUtils;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.osgi.framework.Bundle;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.shared.Constants;

public class STFMessage implements Constants {

    public static final transient Logger log = Logger
        .getLogger(STFMessage.class);

    public final static Map<String, String> viewTitlesAndIDs = new HashMap<String, String>();

    static {
        viewTitlesAndIDs.put(VIEW_PACKAGE_EXPLORER, VIEW_PACKAGE_EXPLORER_ID);
        viewTitlesAndIDs.put(VIEW_REMOTE_SCREEN, VIEW_REMOTE_SCREEN_ID);
        viewTitlesAndIDs.put(VIEW_SAROS, VIEW_SAROS_ID);
        viewTitlesAndIDs.put(VIEW_SVN_REPOSITORIES, VIEW_SVN_REPOSITORIES_ID);
        viewTitlesAndIDs.put(VIEW_PROGRESS, VIEW_PROGRESS_ID);
    }

    public static Saros saros;

    // local JID
    public static JID localJID;

    /**********************************************
     * 
     * Common convenient functions
     * 
     **********************************************/
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

    public String getClassPath(String projectName, String pkg, String className) {
        return projectName + "/src/" + pkg.replaceAll("\\.", "/") + "/"
            + className + ".java";
    }

    public String getPkgPath(String projectName, String pkg) {
        return projectName + "/src/" + pkg.replaceAll("\\.", "/");
    }

    public String[] getClassNodes(String projectName, String pkg,
        String className) {
        String[] nodes = { projectName, SRC, pkg, className + SUFFIX_JAVA };
        return nodes;
    }

    public String[] getPkgNodes(String projectName, String pkg) {
        String[] nodes = { projectName, SRC, pkg };
        return nodes;
    }

    public String getPath(String... nodes) {
        String folderpath = "";
        for (int i = 0; i < nodes.length; i++) {
            if (i == nodes.length - 1) {

                folderpath += nodes[i];
            } else
                folderpath += nodes[i] + "/";
        }
        return folderpath;
    }

    public String changeToRegex(String text) {
        // the name of project in SVN_control contains special characters, which
        // should be filtered.
        String[] names = text.split(" ");
        if (names.length > 1) {
            text = names[0];
        }
        return text + ".*";
    }

    public String[] changeToRegex(String... texts) {
        String[] matchTexts = new String[texts.length];
        for (int i = 0; i < texts.length; i++) {
            matchTexts[i] = texts[i] + "( .*)?";
        }
        return matchTexts;
    }

    public String convertStreamToString(InputStream is) throws IOException {
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

    public boolean isSame(InputStream input1, InputStream input2)
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

    public String checkInputText(String inputText) {
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

    public boolean isValidClassPath(String projectName, String pkg,
        String className) {
        boolean isVailid = true;
        isVailid &= projectName.matches(PROJECT_REGEX);
        isVailid &= pkg.matches(PKG_REGEX);
        isVailid &= className.matches("\\w*");
        return isVailid;
    }

    public String[] getParentNodes(String... nodes) {
        String[] parentNodes = new String[nodes.length - 1];
        for (int i = 0; i < nodes.length - 1; i++) {
            parentNodes[i] = nodes[i];
        }
        return parentNodes;
    }

    public String getLastNode(String... nodes) {
        return nodes[nodes.length - 1];
    }

    public String[] splitPkg(String pkg) {
        return pkg.split("\\.");
    }

    public String getFileContentNoGUI(String filePath) {
        Bundle bundle = saros.getBundle();
        String content;
        try {
            content = FileUtils.read(bundle.getEntry(filePath));
        } catch (NullPointerException e) {
            throw new RuntimeException("Could not open " + filePath);
        }
        return content;
    }

}