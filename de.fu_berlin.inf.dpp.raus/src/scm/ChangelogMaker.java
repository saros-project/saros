package scm;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import main.Constants;

import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNLogClient;
import org.tmatesoft.svn.core.wc.SVNRevision;

/**
 * This class controls the creation of changelogs.
 * 
 * @author Karl Beecher
 * 
 */
public class ChangelogMaker {

    private SVNLogClient logClient;

    FileWriter changelogStream;
    BufferedWriter changelogFile;

    private ISVNLogEntryHandler logReporter = new ISVNLogEntryHandler() {

        @Override
        public void handleLogEntry(SVNLogEntry entry) throws SVNException {
            try {
                changelogFile.write(entry.getMessage() + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    };

    public ChangelogMaker() {

        SVNClientManager clientManager = SVNClientManager.newInstance();
        logClient = clientManager.getLogClient();

        // logClient = new SVNLogClient(
        // SVNWCUtil.createDefaultAuthenticationManager(SVNAccess.USERNAME,
        // SVNAccess.PASSWORD), SVNWCUtil.createDefaultOptions(false));

    }

    /**
     * Generates a new changelog.
     * 
     * @param startRevision
     *            The first revision to include in the changelog
     * @param endRevision
     *            The last revision to include in the changelog
     * @return True if the process completed without error
     * @throws IOException
     */
    public boolean generateChangelog(SVNRevision startRevision,
        SVNRevision endRevision) throws IOException {

        boolean noError = true;

        changelogStream = new FileWriter(Constants.getChangelogFile());
        changelogFile = new BufferedWriter(changelogStream);

        try {
            SVNURL url = SVNURL.parseURIDecoded(Constants.getRepoRoot()
                .toString());

            logClient.doLog(url, new String[] { "trunk" }, startRevision,
                startRevision, endRevision, false, false, 1000, logReporter);

            changelogFile.close();
        } catch (SVNException e) {
            e.printStackTrace();
            noError = false;
        }

        return noError;
    }
}
