package de.fu_berlin.inf.dpp.git;

import de.fu_berlin.inf.dpp.monitoring.NullProgressMonitor;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Ref;
import javax.swing.ProgressMonitor;
import org.apache.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RemoteRemoveCommand;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.BundleWriter;
import org.eclipse.jgit.transport.URIish;

public class JGitFacade {
    private static final Logger log = Logger.getLogger(JGitFacade.class);

    /**
     * Create a bundle file with all commits from basis to actual of the Git
     * repo.
     *
     * @param workDir
     *            The directory that contains the .git directory
     * @param actual
     *            The name of the ref to lookup. For example: "HEAD" or
     *            "refs/heads/master". Must not be a short-handform; e.g.,
     *            "master" is not automatically expanded to"refs/heads/master".
     *            Include the object the ref is point at in the bundle and (if
     *            the basis is an empty string) everything reachable from it.
     * @param basis
     *            Assume that the recipient have at least the commit the basis
     *            is pointing to. In order to fetch from a bundle the recipient
     *            must have the commit the basis is pointing to.
     * @throws IllegalArgumentException
     *             workDir, actual or basis can't be resolved
     * @throws IOException
     *             Error while writing bundle, after resolving parameters
     */
    public static File createBundle(File workDir, String actual, String basis)
        throws IllegalArgumentException, IOException {

        // The method can be divided into 4 Steps. After the first, second and
        // third step the
        // bundlewriter object is expanded with the parameters. The last step
        // exists to write the
        // bundle file.

        // Step 1
        Git git;
        try {
            git = Git.open(workDir);
        } catch (IOException e) {
            throw new IllegalArgumentException("workDir can't be resolved", e);
        }
        Repository repo = git.getRepository();
        BundleWriter bundlewriter = new BundleWriter(repo);

        // Step 2
        Ref actualRef;
        try {
            actualRef = repo.exactRef(actual);
        } catch (IOException e) {
            throw new IllegalArgumentException("actual can't be resolved", e);
        }
        if (actualRef == null)
            throw new IllegalArgumentException("actual can't be resolved");
        bundlewriter.include("refs/heads/bundle", actualRef.getObjectId());

        // Step 3
        if (basis != "") {
            try {
                RevWalk walk = new RevWalk(repo);
                RevCommit basisCommit = walk.parseCommit(repo.resolve(basis));
                bundlewriter.assume(basisCommit);
            } catch (Exception e) {
                throw new IllegalArgumentException("basis can't be resolved",
                    e);
            }
        }

        // Step 4
        File bundle;
        try {
            bundle = File.createTempFile("file", ".bundle");
            OutputStream fos = new FileOutputStream(bundle);

            ProgressMonitor monitor = NullProgressMonitor.INSTANCE;

            bundlewriter.writeBundle(monitor, fos);
        } catch (Exception e) {
            throw new IOException("failed to write bundle", e);
        }
        return bundle;
    }

    /**
     * Fetching from a bundle file to an Git repo
     *
     * @param workDir
     *            The directory that contains the .git directory
     * @param bundleFile
     * @throws IllegalArgumentException
     *             workDir or bundleFile can't be resolved
     * @throws Exception
     *             Error while adding the bundle as a remote or during fetch
     */
    public static void fetchFromBundle(File workDir, File bundleFile)
        throws IllegalArgumentException, Exception {

        Git git;
        try {
            git = Git.open(workDir);
        } catch (IOException e) {
            throw new IllegalArgumentException("workDir can't be resolved", e);
        }

        try {
            RemoteRemoveCommand rrc = git.remoteRemove();
            rrc.setName("bundle");
            rrc.call();
        } catch (Exception e) {
            throw new Exception("failed to remove old bundle path", e);
        }

        try {
            git.remoteAdd()
                .setUri(new URIish().setPath(bundleFile.getCanonicalPath()))
                .setName("bundle").call();
        } catch (IOException e) {
            throw new IllegalArgumentException("bundleFile can't be resolved",
                e);
        } catch (Exception e) {
            throw new Exception("failed to add bundle as remote", e);
        }

        try {
            git.fetch().setRemote("bundle").call();
        } catch (Exception e) {
            throw new Exception("failed to fetch from bundle", e);
        }
    }

    public static void cloneFromRepo(File from, File to)
        throws IllegalArgumentException, Exception {
        try {
            Git.cloneRepository().setURI(getUrlByWorkDir(from)).setDirectory(to)
                .call();
        } catch (IOException e) {
            throw new IllegalArgumentException(
                "first parameter can't be resolved", e);
        } catch (Exception e) {
            throw new Exception("failed to clone", e);
        }
    }

    /**
     * @param workDir
     *            The directory that contains the .git directory
     * @return The URL to access/address directly the .git directory
     * @throws IOException
     */
    static String getUrlByWorkDir(File workDir) throws IOException {
        Git git = Git.open(workDir);
        return git.getRepository().getDirectory().getCanonicalPath();
    }

    public static String getSHA1HashByRevisionString(File workDir,
        String revString) throws IOException {
        return Git.open(workDir).getRepository().resolve(revString).name();
    }
}