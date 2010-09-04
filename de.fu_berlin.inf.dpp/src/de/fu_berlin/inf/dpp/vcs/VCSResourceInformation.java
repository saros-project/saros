package de.fu_berlin.inf.dpp.vcs;

// FIXME ndh encapsulate fields
public class VCSResourceInformation {
    // @Inv: repositoryURL+projectPath is a valid URL.
    /** URL of the repository. */
    public String repositoryRoot;
    /** Path of the resource relative to the repository root. */
    public String path;
    /** Revision of the resource. */
    public String revision;
}