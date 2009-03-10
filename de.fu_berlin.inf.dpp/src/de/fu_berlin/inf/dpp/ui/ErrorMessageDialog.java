package de.fu_berlin.inf.dpp.ui;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.util.Pair;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * Eclipse Dialog to show Exception Messages.
 * 
 * @author rdjemili
 * @author chjacob
 * 
 */
public class ErrorMessageDialog {

    private static final Logger log = Logger.getLogger(ErrorMessageDialog.class
        .getName());

    /**
     * show error message dialog.
     * 
     * @param exception
     */
    public static void showErrorMessage(final Exception exception) {
        Util.runSafeSWTSync(log, new Runnable() {
            public void run() {
                MessageDialog.openError(Display.getDefault().getActiveShell(),
                    exception.toString(), exception.getMessage());
            }
        });
    }

    /**
     * show error message dialog.
     * 
     * @param exception
     */
    public static void showErrorMessage(final String exceptionMessage) {
        Util.runSafeSWTSync(log, new Runnable() {
            public void run() {
                if ((exceptionMessage == null) || exceptionMessage.equals("")) {
                    MessageDialog.openError(Display.getDefault()
                        .getActiveShell(), "Exception", "Error occured.");
                } else {
                    MessageDialog.openError(Display.getDefault()
                        .getActiveShell(), "Exception", exceptionMessage);
                }
            }

        });
    }

    protected static HashMap<Pair<String, JID>, MessageDialog> actualChecksumErrorDialogs = new HashMap<Pair<String, JID>, MessageDialog>();

    /**
     * This method creates and opens an error message which informs the user
     * that inconsistencies are handled and he should wait until the
     * inconsistencies are resolved. The Message are saved in a HashMap with a
     * pair of JID of the user and a string representation of the paths of the
     * handled files as key. You can use <code>closeChecksumErrorMessage</code>
     * with the same arguments to close this message again.
     * 
     * @see #closeChecksumErrorMessage(String, JID)
     * 
     * @param pathes
     *            a string representation of the handled files.
     * @param from
     *            JID
     * 
     */
    public static void showChecksumErrorMessage(final String pathes,
        final JID from) {

        Util.runSafeSWTAsync(log, new Runnable() {
            public void run() {
                MessageDialog md = new MessageDialog(
                    Display.getDefault().getActiveShell(),
                    "Consistency Problem!",
                    null,
                    "Inconsitent file state has detected. File "
                        + pathes
                        + " from user "
                        + from.getBase()
                        + " has to be synchronized with project host. Please wait until the inconsistencies are resolved.",
                    MessageDialog.WARNING, new String[0], 0);
                actualChecksumErrorDialogs.put(new Pair<String, JID>(pathes,
                    from), md);
                md.open();
            }
        });
    }

    /**
     * Closes the ChecksumError message identified by the JID from the user who
     * had the inconsistencies and a string representation of the handled files.
     * The string representation must be the same which are used to show the
     * message with <code>showChecksumErrorMessage</code>.
     * 
     * @see #showChecksumErrorMessage(String, JID)
     * 
     * @param from
     *            JID of user who had the inconsistencies
     * @param pathsOfInconsistencies
     *            a string representation of the paths of handled files
     * 
     */
    public static void closeChecksumErrorMessage(final String paths,
        final JID from) {
        Util.runSafeSWTAsync(log, new Runnable() {
            Pair<String, JID> key = new Pair<String, JID>(paths, from);
            MessageDialog md = actualChecksumErrorDialogs.get(key);

            public void run() {
                if (md != null) {
                    md.close();
                    actualChecksumErrorDialogs.remove(key);
                }
            }
        });
    }
}
