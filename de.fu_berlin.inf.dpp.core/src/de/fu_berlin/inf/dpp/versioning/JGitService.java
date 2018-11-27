package de.fu_berlin.inf.dpp.versioning;



import java.io.File;

import java.io.FileOutputStream;

import java.io.IOException;

import java.io.OutputStream;



import org.eclipse.jgit.api.Git;

import org.eclipse.jgit.lib.NullProgressMonitor;

import org.eclipse.jgit.lib.ProgressMonitor;

import org.eclipse.jgit.lib.Ref;

import org.eclipse.jgit.lib.Repository;

import org.eclipse.jgit.revwalk.RevCommit;

import org.eclipse.jgit.revwalk.RevWalk;

import org.eclipse.jgit.transport.BundleWriter;



/**

 * This class serves as a facade for the JGit Library. It provide a methods for

 * creating a bundle File.

 * 

 */

public class JGitService {

    /**

     * Create a bundle File with all commits from commit at tag to commit at

     * HEAD of user.

     * 

     * @param user

     *            the one which have the commits for the bundle

     * @param tag

     *            assume that the recipient have at least the commit the tag is

     *            pointing to

     */

    public static File getBundleByTag(Git user, String tag) throws IOException {

        Repository repo = user.getRepository();

        Ref HEAD = repo.getRef("HEAD");

        Ref MASTER = repo.getRef("master");

        BundleWriter bundlewriter = new BundleWriter(repo);

        File bundle = File.createTempFile("file", ".bundle");

        OutputStream fos = new FileOutputStream(bundle);

        ProgressMonitor monitor = NullProgressMonitor.INSTANCE;

        bundlewriter.include(HEAD);

        bundlewriter.include(MASTER);

        RevWalk walk = new RevWalk(repo);

        RevCommit tagCommit = walk.parseCommit(repo.resolve(tag));

        bundlewriter.assume(tagCommit);

        bundlewriter.writeBundle(monitor, fos);

        return bundle;

    }

}