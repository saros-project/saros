package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.eclipse.swtbot.swt.finder.utils.FileUtils;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.osgi.framework.Bundle;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.accountManagement.XMPPAccountStore;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.feedback.FeedbackManager;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.stf.server.STFController;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions.SarosSWTBotPreferences;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.WorkbenchImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets.BasicWidgetsImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets.Shell;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets.ShellImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets.TableImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.editor.EditorImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.menuBar.EditMImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.menuBar.FileMImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.menuBar.RefactorMImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.menuBar.SarosMImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.menuBar.WindowMImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.views.ProgressViewImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.views.SarosPEViewImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.views.sarosViews.ChatViewImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.views.sarosViews.RSViewImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.views.sarosViews.RosterViewImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.views.sarosViews.SessionViewImp;
import de.fu_berlin.inf.dpp.stf.server.sarosSWTBot.SarosSWTBot;

public abstract class EclipsePart {
    protected static final transient Logger log = Logger
        .getLogger(EclipsePart.class);

    // Title of Buttons
    protected final static String YES = "Yes";
    protected final static String OK = "OK";
    protected final static String NO = "No";
    protected final static String CANCEL = "Cancel";
    protected final static String FINISH = "Finish";
    protected final static String APPLY = "Apply";
    protected final static String NEXT = "Next >";

    public static final String SRC = "src";
    public final static String SUFIX_JAVA = ".java";

    // Shell
    protected final static String SHELL_PROGRESS_INFORMATION = "Progress Information";

    // Role:Driver
    public static final String OWN_CONTACT_NAME = "You";
    protected final static String ROLENAME = " (Driver)";

    // exported objects
    public static BasicWidgetsImp basic = BasicWidgetsImp.getInstance();
    public static TableImp table = TableImp.getInstance();

    public static EditorImp editor = EditorImp.getInstance();
    public static WorkbenchImp workbenchC = WorkbenchImp.getInstance();

    public static Shell shellC = ShellImp.getInstance();

    // Views
    public static RosterViewImp rosterV = RosterViewImp.getInstance();
    public static SessionViewImp sessionV = SessionViewImp.getInstance();
    public static RSViewImp remoteScreenV = RSViewImp.getInstance();
    public static ChatViewImp chatV = ChatViewImp.getInstance();
    public static SarosPEViewImp pEV = SarosPEViewImp.getInstance();
    public static ProgressViewImp progressV = ProgressViewImp.getInstance();

    // menus in menu bar
    public static FileMImp fileM = FileMImp.getInstance();
    public static EditMImp editM = EditMImp.getInstance();
    public static RefactorMImp refactorM = RefactorMImp.getInstance();
    public static SarosMImp sarosM = SarosMImp.getInstance();
    public static WindowMImp windowM = WindowMImp.getInstance();

    // Picocontainer initiated by STFController.
    public static Saros saros;
    public static SarosSessionManager sessionManager;
    public static DataTransferManager dataTransferManager;
    public static EditorManager editorManager;
    public static XMPPAccountStore xmppAccountStore;
    public static FeedbackManager feedbackManager;

    // local JID
    public static JID localJID;

    // SWTBot framework
    public static SarosSWTBot bot = SarosSWTBot.getInstance();
    public static int sleepTime = STFController.sleepTime;

    public final static String SCREENSHOTDIR = "test/STF/screenshot";

    protected void waitUntil(ICondition condition) {
        bot.waitUntil(condition, SarosSWTBotPreferences.SAROS_TIMEOUT);
    }

    protected void waitLongUntil(ICondition condition) {
        bot.waitUntil(condition, SarosSWTBotPreferences.SAROS_LONG_TIMEOUT);
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
        String[] nodes = { projectName, SRC, pkg, className + SUFIX_JAVA };
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

    public String getTestFileContents(String testFilePath) {
        Bundle bundle = saros.getBundle();
        String contents;
        try {
            contents = FileUtils.read(bundle.getEntry(testFilePath));
        } catch (NullPointerException e) {
            throw new RuntimeException("Could not open " + testFilePath);
        }
        return contents;
    }

    public String[] changeToRegex(String... texts) {
        String[] matchTexts = new String[texts.length];
        for (int i = 0; i < texts.length; i++) {
            matchTexts[i] = texts[i] + "( .*)?";
        }
        return matchTexts;
    }

    public String ConvertStreamToString(InputStream is) throws IOException {
        // BufferedReader bufferedReader = new BufferedReader(
        // new InputStreamReader(is));
        // StringBuilder stringBuilder = new StringBuilder();
        // String line = null;
        //
        // while ((line = bufferedReader.readLine()) != null) {
        // stringBuilder.append(line + "\n");
        // }
        //
        // bufferedReader.close();
        // return stringBuilder.toString();
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

    public enum TypeOfOS {
        MAC, WINDOW
    }

    public TypeOfOS getOS() {
        String osName = System.getProperty("os.name");
        if (osName.matches("Windows.*"))
            return TypeOfOS.WINDOW;
        else if (osName.matches("Mac OS X.*")) {
            return TypeOfOS.MAC;
        }
        return TypeOfOS.WINDOW;
    }

    public String checkInputText(String inputText) {
        char[] chars = inputText.toCharArray();
        String newInputText = "";
        log.debug("keyboard layout: " + SWTBotPreferences.KEYBOARD_LAYOUT);
        for (char c : chars) {
            if (c == 'y' && SWTBotPreferences.KEYBOARD_LAYOUT.equals("MAC_DE")) {
                newInputText += 'z';
            } else
                newInputText += c;
        }
        return newInputText;
    }
}
