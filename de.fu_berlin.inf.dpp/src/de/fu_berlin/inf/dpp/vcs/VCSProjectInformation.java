package de.fu_berlin.inf.dpp.vcs;

public class VCSProjectInformation {
    /**
     * URL of the repository. repositoryURL+projectPath is supposed to be a
     * valid URL.
     */
    public String repositoryURL;
    /** Path of the project in repository. */
    public String projectPath;
    /** Revision of the project. */
    protected String baseRevision;
}