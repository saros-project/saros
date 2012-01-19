package scm;

import java.io.File;

import main.Constants;

import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNCopyClient;
import org.tmatesoft.svn.core.wc.SVNCopySource;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;

/**
 * This class controls branching operations.
 * 
 * @author Karl Beecher
 * 
 */
public class Brancher {

    private SVNCopyClient copyClient;
    private SVNUpdateClient updateClient;

    /**
     * Create a new Brancher based on the default configuration of Saros.
     */
    public Brancher() {
        SVNClientManager clientManager = SVNClientManager.newInstance();
        this.copyClient = clientManager.getCopyClient();
        this.updateClient = clientManager.getUpdateClient();
    }

    /**
     * Create a new branch in the repository.
     * 
     * @param version
     *            Version name of the new branch
     */
    public boolean createBranch(String version, long revision) {

        File rootFile = Constants.getWorkingCopy();
        String branchName = version + ".r" + Long.toString(revision);
        String commitMessage = "[BUILD] Opened release branch " + branchName;

        boolean success = false;

        try {
            updateClient.doUpdate(new File[] { rootFile }, SVNRevision.HEAD,
                SVNDepth.INFINITY, true, false);

            SVNURL source = SVNURL.parseURIDecoded(Constants.getRepoPath(
                "trunk").toString());
            SVNURL target = SVNURL.parseURIDecoded(Constants.getRepoPath(
                "branches").toString());
            SVNCopySource copySource = new SVNCopySource(SVNRevision.UNDEFINED,
                SVNRevision.HEAD, source);

            copyClient.doCopy(new SVNCopySource[] { copySource }, target,
                false, false, true, commitMessage, null);

            success = true;

        } catch (SVNException e) {
            System.out.print("Error: " + e);
        }

        return success;
    }
}
