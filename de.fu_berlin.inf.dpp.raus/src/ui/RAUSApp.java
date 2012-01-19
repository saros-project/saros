package ui;

import java.io.IOException;
import java.text.MessageFormat;

import main.Constants;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.SVNRevision;

import scm.Brancher;
import scm.ChangelogMaker;
import scm.SVNAccess;

/**
 * The main UI class.
 * 
 * @author Karl Beecher
 * 
 */
public class RAUSApp {

    private SVNRepository repo;

    private Shell shell;
    private Text outputText;

    private Button createBranchButton;
    private Text branchLabelText;

    private final String BRANCH_HELP_TEXT = "Enter branch label here";

    private Button createChangelogButton;

    private Button runJUnitTestsButton;

    public RAUSApp(Display display) {
        shell = new Shell(display);
        initUI();

        try {
            repo = SVNAccess.getRepo();
        } catch (SVNException e) {
            print("Error in Main: " + e);
            e.printStackTrace();
        }

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

    public void initUI() {

        shell.setLayout(new GridLayout(2, true));
        shell.setText("RAUS - Release Automation System");
        shell.setLocation(300, 300);
        shell.setSize(640, 480);

        GridData textGridData = new GridData();
        textGridData.horizontalSpan = 2;

        outputText = new Text(shell, SWT.MULTI | SWT.BORDER | SWT.WRAP
            | SWT.V_SCROLL);
        outputText.setSize(600, 100);
        outputText.setEditable(false);
        outputText.setLayoutData(textGridData);

        createBranchButton = new Button(shell, SWT.NONE);
        createBranchButton.setText("Create branch");
        createBranchButton.addSelectionListener(new CreateBranchSelection());

        branchLabelText = new Text(shell, SWT.SINGLE | SWT.BORDER);
        branchLabelText.addFocusListener(new BranchLabelTextFocus());
        branchLabelText.setText(BRANCH_HELP_TEXT);

        createChangelogButton = new Button(shell, SWT.NONE);
        createChangelogButton.setText("Create changelog");
        createChangelogButton
            .addSelectionListener(new CreateChangelogSelection());

        runJUnitTestsButton = new Button(shell, SWT.NONE);
        runJUnitTestsButton.setText("Run JUnit tests");
        runJUnitTestsButton.addSelectionListener(new RunTestsSelection());

        print("Welcome to RAUS!");
        print(MessageFormat.format("The repository root is: {0}",
            Constants.getRepoRoot()));
        print(MessageFormat.format("Your working copy is: {0}",
            Constants.getWorkingCopy()));

        shell.pack();
        shell.open();
    }

    public void print(String msg) {
        outputText.append(msg + "\n");
    }

    public void createBranch() throws SVNException {
        Brancher b = new Brancher();
        long latestRevision = repo.getLatestRevision();
        String version = branchLabelText.getText();

        boolean succeed = b.createBranch(version, latestRevision);

        if (succeed) {
            print("Created " + version + "." + latestRevision);
            branchLabelText.setText("");
        }
    }

    public void createChangelog() throws SVNException {
        ChangelogMaker c = new ChangelogMaker();

        boolean success = false;

        try {
            // TODO Make the first revision variable
            success = c.generateChangelog(SVNRevision.create(1),
                SVNRevision.create(repo.getLatestRevision()));

        } catch (IOException e) {
            MessageBox messageDialog = new MessageBox(shell, SWT.ERROR);
            messageDialog.setText("Can't write changelog");
            messageDialog
                .setMessage("There was an I/O error when trying to create the changelog file.");
            messageDialog.open();
            e.printStackTrace();
        }

        if (success) {
            MessageBox messageDialog = new MessageBox(shell,
                SWT.ICON_INFORMATION);
            messageDialog.setText("Changelog created");
            messageDialog.setMessage("Change was successfully created at: "
                + Constants.getChangelogFile());
            messageDialog.open();
        } else {
            MessageBox messageDialog = new MessageBox(shell, SWT.ERROR);
            messageDialog.setText("Failed to create changelog");
            messageDialog
                .setMessage("There was a problem with the repository.");
            messageDialog.open();
        }

    }

    private class CreateBranchSelection extends SelectionAdapter {

        @Override
        public void widgetSelected(SelectionEvent e) {

            if (branchLabelText.getText().isEmpty()) {
                MessageBox messageDialog = new MessageBox(shell, SWT.ERROR);
                messageDialog.setText("Need branch label");
                messageDialog
                    .setMessage("Please enter a label for the branch!");
                messageDialog.open();

            } else {
                try {
                    createBranch();

                } catch (SVNException exc) {
                    print("Error in Main: " + exc);
                    exc.printStackTrace();
                }
            }
        }
    }

    private class CreateChangelogSelection extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent e) {
            try {
                createChangelog();
            } catch (SVNException exc) {
                print("Error in Main: " + exc);
                exc.printStackTrace();
            }

        }
    }

    private class BranchLabelTextFocus extends FocusAdapter {
        @Override
        public void focusGained(FocusEvent e) {
            if (branchLabelText.getText().equals(BRANCH_HELP_TEXT)) {
                branchLabelText.setText("");
            }
        }

        @Override
        public void focusLost(FocusEvent e) {
            if (branchLabelText.getText().equals("")) {
                branchLabelText.setText(BRANCH_HELP_TEXT);
            }
        }
    }

    private class RunTestsSelection extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent e) {
            // JUnitLaunchShortcut shortcut = new JUnitLaunchShortcut();

            org.junit.runner.JUnitCore
                .runClasses(de.fu_berlin.inf.dpp.AllTestSuite.class);
        }
    }
}
