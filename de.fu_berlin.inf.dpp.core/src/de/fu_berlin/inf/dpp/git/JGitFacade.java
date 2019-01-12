package de.fu_berlin.inf.dpp.git;

import de.fu_berlin.inf.dpp.monitoring.NullProgressMonitor;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Ref;
import javax.swing.ProgressMonitor;
import org.apache.log4j.Logger;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.BundleWriter;
import org.eclipse.jgit.transport.URIish;

public class JGitFacade {
    private static final Logger LOG = Logger.getLogger(JGitFacade.class);

    /**
     * Create a bundle file with all commits from basis to actual of the Git
     * repo.
     *
     * @param workDir
     *            The directory that contains the .git directory
     * @param actual
     *            the name of the ref to lookup. Must not be a short-handform;
     *            e.g., "master" is not automatically expanded
     *            to"refs/heads/master". Include the object the ref is point at
     *            in the bundle and (if the basis is an empty string) everything
     *            reachable from it.
     * @param basis
     *            assume that the recipient have at least the commit the basis
     *            is pointing to. In order to fetch from a bundle the recipient
     *            must have the commit the basis is pointing to.
     * @throws IOException
     *             if the workDir can't be accessed
     * @throws IllegalArgumentException
     *             if actual or the basis can't be resolved
     */
    public static File createBundle(File workDir, String actual, String basis)
        throws IOException, IllegalArgumentException, NullPointerException {
        Git user = Git.open(workDir);
        Repository repo = user.getRepository();
        Ref actualRef = repo.exactRef(actual);
        if (actualRef == null)
            throw new IllegalArgumentException("actual can't be resolved");
        BundleWriter bundlewriter = new BundleWriter(repo);
        File bundle = File.createTempFile("file", ".bundle");
        OutputStream fos = new FileOutputStream(bundle);
        ProgressMonitor monitor = NullProgressMonitor.INSTANCE;
        bundlewriter.include(actualRef);

        if (basis != "") {
            RevWalk walk = new RevWalk(repo);
            try {
                RevCommit basisCommit = walk.parseCommit(repo.resolve(basis));
                bundlewriter.assume(basisCommit);
            } catch (Exception e) {
                throw new IllegalArgumentException("basis can't be resolved");
            }
        }
        bundlewriter.writeBundle(monitor, fos);
        return bundle;
    }

    /**
     * Fetching from a bundle file to an Git repo
     *
     * @param bundleFile
     * @param workDir
     *            The directory that contains the .git directory
     * @throws IOException
     * @throws GitAPIException
     */
    public static void fetchFromBundle(File bundleFile, File workDir)
        throws IOException, GitAPIException, InvalidRemoteException {
        Git git = Git.open(workDir);
        URIish bundleURI = new URIish().setPath(bundleFile.getCanonicalPath());
        git.remoteAdd().setUri(bundleURI).setName("bundle").call();
        git.fetch().setRemote("bundle").call();
    }

    public static void cloneFromRepo(File from, File to) throws IOException,
        InvalidRemoteException, TransportException, GitAPIException {
        CloneCommand cloneCommand = Git.cloneRepository();
        cloneCommand.setURI(getUrlByWorkDir(from));
        cloneCommand.setDirectory(to);
        cloneCommand.call();
    }

    static String getUrlByWorkDir(File gitRepo) throws IOException {
        Git git = Git.open(gitRepo);
        return git.getRepository().getDirectory().getCanonicalPath();
    }
}