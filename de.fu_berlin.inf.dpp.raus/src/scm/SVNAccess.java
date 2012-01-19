package scm;

import main.Constants;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

/**
 * This class is responsible for managing access to repositories.
 * 
 * @author Karl Beecher
 * 
 */
public class SVNAccess {

    public static final String USERNAME = "";

    public static final String PASSWORD = "";

    /**
     * @return An object with which to access your repository
     * @throws SVNException
     */
    public static SVNRepository getRepo() throws SVNException {

        SVNRepositoryFactoryImpl.setup();
        DAVRepositoryFactory.setup();// IHTTPConnectionFactory.DEFAULT);
        FSRepositoryFactory.setup();

        SVNRepository repo;

        repo = DAVRepositoryFactory.create(SVNURL.parseURIDecoded(Constants
            .getRepoRoot().toString()));
        /**
         * Alternative types of repository:
         * 
         * repo = SVNRepositoryFactory.create(SVNURL.parseURIDecoded(Constants.
         * getRepoRoot().toString())));
         * 
         * repo = FSRepositoryFactory.create(SVNURL.
         * parseURIDecoded(Constants.getRepoRoot().toString())));
         * 
         */

        repo.setAuthenticationManager(SVNWCUtil
            .createDefaultAuthenticationManager(USERNAME, PASSWORD));

        return repo;
    }
}
