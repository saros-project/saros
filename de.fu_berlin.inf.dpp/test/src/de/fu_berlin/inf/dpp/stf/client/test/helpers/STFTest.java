package de.fu_berlin.inf.dpp.stf.client.test.helpers;

import de.fu_berlin.inf.dpp.stf.client.Musician;

public class STFTest {

    /* Musicians */
    public static Musician alice;
    public static Musician bob;
    public static Musician carl;
    public static Musician dave;
    public static Musician edna;

    // Title of Buttons
    protected final static String YES = "Yes";
    protected final static String OK = "OK";
    protected final static String NO = "No";
    protected final static String CANCEL = "Cancel";
    protected final static String FINISH = "Finish";
    protected final static String NEXT = "Next >";

    /* Project name */
    public static final String PROJECT1 = "Foo_Saros";
    public static final String PROJECT2 = "Foo_Saros2";
    public static final String PROJECT3 = "Foo_Saros3";

    /* Folder name */
    public static final String FOLDER1 = "MyFolder";
    public static final String FOLDER2 = "MyFolder2";

    /* File name */
    public static final String FILE1 = "MyFile.xml";
    public static final String FILE2 = "MyFile2.xml";

    /* Package name */
    public static final String PKG1 = "my.pkg";
    public static final String PKG2 = "my.pkg2";
    public static final String PKG3 = "my.pkg3";

    /* class name */
    public static final String CLS1 = "MyClass";
    public static final String CLS2 = "MyClass2";
    public static final String CLS3 = "MyClass3";

    /* content path */
    public static final String CP1 = "test/STF/" + CLS1 + ".java";
    public static final String CP2 = "test/STF/" + CLS2 + ".java";
    public static final String CP3 = "test/STF/" + CLS3 + ".java";
    public static final String CP1_CHANGE = "test/STF/" + CLS1 + "Change"
        + ".java";
    public static final String CP2_CHANGE = "test/STF/" + CLS2 + "Change"
        + ".java";

    public static final String SRC = "src";
    public final static String SUFIX_JAVA = ".java";

    public static final String ROLE_NAME = " (Driver)";
    public static final String OWN_CONTACT_NAME = "You";

    /* SVN infos */
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
    public final static String CONTEXT_MENU_SHARE_PROJECT = "Share project...";

    public final static String ID_JAVA_EDITOR = "org.eclipse.jdt.ui.CompilationUnitEditor";
    public final static String ID_TEXT_EDITOR = "org.eclipse.ui.texteditor";
    public final static int CREATE_NEW_PROJECT = 1;
    public final static int USE_EXISTING_PROJECT = 2;
    public final static int USE_EXISTING_PROJECT_WITH_CANCEL_LOCAL_CHANGE = 3;
    public final static int USE_EXISTING_PROJECT_WITH_COPY = 4;

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
