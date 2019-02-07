package de.fu_berlin.inf.dpp.git;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Set;
import org.apache.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RemoteRemoveCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.lib.NullProgressMonitor;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.BundleWriter;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.TransportBundleStream;
import org.eclipse.jgit.transport.URIish;

public class JGitFacade {
  private static final Logger log = Logger.getLogger(JGitFacade.class);

  /**
   * Create a bundle with all commits from basis to actual of the Git repo.
   *
   * @param workDir The directory that contains the .git directory
   * @param actual The name of the ref to lookup. For example: "HEAD" or "refs/heads/master". Must
   *     not be a short-handform; e.g., "master" is not automatically expanded
   *     to"refs/heads/master". Include the object the ref is point at in the bundle and (if the
   *     basis is an empty string) everything reachable from it.
   * @param basis Assume that the recipient have at least the commit the basis is pointing to. In
   *     order to fetch from a bundle the recipient must have the commit the basis is pointing to.
   * @throws IllegalArgumentException workDir is null or if resolving actual leads to a not existing
   *     ref
   * @throws IOException failed while read/resolved parameters or while written to the bundle
   */
  public static byte[] createBundle(File workDir, String actual, String basis)
      throws IllegalArgumentException, IOException {

    // The method can be divided into 4 Steps. After the first, second and third step the
    // bundlewriter object is expanded with the parameters. The last step exists to write the
    // bundle.

    // Step 1
    Git git;
    try {
      if (workDir == null)
        throw new IllegalArgumentException("workDir is null and can't be resolved");
      git = Git.open(workDir);
    } catch (IOException e) {
      throw new IOException("failed while read from workDir", e);
    }
    Repository repo = git.getRepository();
    BundleWriter bundlewriter = new BundleWriter(repo);

    // Step 2
    Ref actualRef;
    try {
      actualRef = repo.exactRef(actual);
    } catch (IOException e) {
      throw new IOException("failed while resolved second parameter", e);
    }
    if (actualRef == null)
      throw new IllegalArgumentException(
          "failed to resolve the second parameter: not existing ref");

    bundlewriter.include("refs/heads/bundle", actualRef.getObjectId());

    // Step 3
    if (basis != "") {
      try {
        RevWalk walk = new RevWalk(repo);

        RevCommit basisCommit = walk.parseCommit(repo.resolve(basis));

        walk.close();

        bundlewriter.assume(basisCommit);
      } catch (NullPointerException | IOException e) {
        throw new IOException("failed while resolved third parameter", e);
      }
    }

    // Step 4
    NullProgressMonitor monitor = NullProgressMonitor.INSTANCE;
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    bundlewriter.writeBundle(monitor, byteArrayOutputStream);

    return byteArrayOutputStream.toByteArray();
  }

  /**
   * Fetching from a bundle to an Git repo
   *
   * @param workDir The directory that contains the .git directory
   * @param bundle
   * @throws IllegalArgumentException workDir or bundle is null and can't be resolved
   * @throws IOException failed while read from workDir, bundle handling or while fetch
   * @throws URISyntaxException
   */
  public static void fetchFromBundle(File workDir, byte[] bundle)
      throws IllegalArgumentException, IOException, URISyntaxException {
    if (workDir == null)
      throw new IllegalArgumentException("workDir is null and can't be resolved");
    if (bundle == null) throw new IllegalArgumentException("bundle is null and can't be resolved");

    Git git;
    try {
      if (workDir == null)
        throw new IllegalArgumentException("workDir is null and can't be resolved");
      git = Git.open(workDir);
    } catch (IOException e) {
      throw new IOException("failed while read from workDir", e);
    }

    try {
      RemoteRemoveCommand remoteRemoveCommand = git.remoteRemove();
      remoteRemoveCommand.setName("bundle");
      remoteRemoveCommand.call();
    } catch (GitAPIException e) {
      throw new IOException("failed to remove old bundle path", e);
    }

    final URIish uri = new URIish("in-memory://");
    final ByteArrayInputStream in = new ByteArrayInputStream(bundle);
    final RefSpec rs = new RefSpec("refs/heads/*:refs/heads/*");
    final Set<RefSpec> refs = Collections.singleton(rs);

    try (TransportBundleStream transport =
        new TransportBundleStream(git.getRepository(), uri, in)) {
      transport.fetch(NullProgressMonitor.INSTANCE, refs);
    } catch (org.eclipse.jgit.errors.TransportException e) {
      throw new IOException("fetch failed", e);
    }
  }

  /**
   * @throws IllegalArgumentException parameters can't be resolved. Maybe null
   * @throws IOException failed while cloned
   */
  public static void cloneFromRepo(File from, File to)
      throws IllegalArgumentException, IOException {
    try {
      if (from == null) {
        throw new IllegalArgumentException("first parameter is null and can't be resolved");
      } else if (to == null) {
        throw new IllegalArgumentException("second parameter is null and can't be resolved");
      } else {
        Git.cloneRepository().setURI(getUrlByWorkDir(from)).setDirectory(to).call();
      }
    } catch (InvalidRemoteException e) {
      throw new IllegalArgumentException("second parameter can't be resolved", e);
    } catch (GitAPIException | IOException e) {
      throw new IOException("clone failed", e);
    }
  }
  /**
   * @param workDir The directory that contains the .git directory
   * @return The URL to access/address directly the .git directory
   * @throws IllegalArgumentException workDir is null and can't be resolved
   * @throws IOException failed while read
   */
  static String getUrlByWorkDir(File workDir) throws IllegalArgumentException, IOException {
    if (workDir == null)
      throw new IllegalArgumentException("workDir is null and can't be resolved");
    Git git = Git.open(workDir);
    return git.getRepository().getDirectory().getCanonicalPath();
  }
  /**
   * @param workDir The directory that contains the .git directory
   * @param revString
   * @throws IllegalArgumentException workDir is null and can't be resolved
   * @throws IOException failed while read
   */
  public static String getSHA1HashByRevisionString(File workDir, String revString)
      throws IllegalArgumentException, IOException {
    if (workDir == null)
      throw new IllegalArgumentException("workDir is null and can't be resolved");
    return Git.open(workDir).getRepository().resolve(revString).name();
  }
}
