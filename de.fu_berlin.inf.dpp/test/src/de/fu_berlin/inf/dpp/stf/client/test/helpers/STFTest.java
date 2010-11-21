package de.fu_berlin.inf.dpp.stf.client.test.helpers;

import de.fu_berlin.inf.dpp.stf.server.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.server.SarosConstant;

public class STFTest {

    public static final String PROJECT = BotConfiguration.PROJECTNAME;
    public static final String PROJECT2 = BotConfiguration.PROJECTNAME2;
    public static final String PROJECT3 = BotConfiguration.PROJECTNAME3;

    public static final String FOLDER = BotConfiguration.FOLDERNAME;
    public static final String FOLDER2 = BotConfiguration.FOLDERNAME2;
    public static final String FILE = BotConfiguration.FILENAME;
    public static final String FILE2 = BotConfiguration.FILENAME2;

    public static final String PKG = BotConfiguration.PACKAGENAME;
    public static final String PKG2 = BotConfiguration.PACKAGENAME2;
    public static final String PKG3 = BotConfiguration.PACKAGENAME3;

    public static final String CLS = BotConfiguration.CLASSNAME;
    public static final String CLS2 = BotConfiguration.CLASSNAME2;
    public static final String CLS3 = BotConfiguration.CLASSNAME3;

    public static final String SRC = "src";
    public final static String SUFIX_JAVA = ".java";

    public static final String CP = BotConfiguration.CONTENTPATH;
    public static final String CP2 = BotConfiguration.CONTENTPATH2;
    public static final String CP3 = BotConfiguration.CONTENTPATH3;
    public static final String CP_CHANGE = BotConfiguration.CONTENTCHANGEPATH;
    public static final String CP2_CHANGE = BotConfiguration.CONTENTCHANGEPATH2;

    public static final String ROLENAME = SarosConstant.ROLENAME;
    public static final String OWNCONTACTNAME = SarosConstant.OWNCONTACTNAME;

    /*
     * SVN
     */
    protected static String SVN_PROJECT = "examples";
    protected static String SVN_PKG = "org.eclipsecon.swtbot.example";
    protected static String SVN_CLS = "MyFirstTest01";
    protected static String SVN_URL = "http://swtbot-examples.googlecode.com/svn";
    protected static String SVN_TAG_URL = "http://swtbot-examples.googlecode.com/svn/tags/eclipsecon2009";
    protected static final String SVN_CLS_PATH = SVN_PROJECT
        + "/src/org/eclipsecon/swtbot/example/MyFirstTest01.java";

    /*
     * Contextmenu "Saros"
     */
    public final static String CONTEXT_MENU_SHARE_PROJECT_WITH_VCS = "Share project with VCS support...";

    public String getClassPath(String projectName, String pkg, String className) {
        return projectName + "/src/" + pkg.replaceAll("\\.", "/") + "/"
            + className + ".java";
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

    public String[] getClassNodes(String projectName, String pkg,
        String className) {
        String[] nodes = { projectName, SRC, pkg, className + SUFIX_JAVA };
        return nodes;
    }

}
