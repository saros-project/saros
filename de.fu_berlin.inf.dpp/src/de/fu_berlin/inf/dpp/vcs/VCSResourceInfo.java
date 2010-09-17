package de.fu_berlin.inf.dpp.vcs;

/**
 * A simple struct to hold VCS related information associated with a resource.
 */
// TODO Encapsulate fields?
public class VCSResourceInfo {
    /** @see VCSAdapter#getUrl(org.eclipse.core.resources.IResource) */
    public String url;
    /** @see VCSAdapter#getRevisionString(org.eclipse.core.resources.IResource) */
    public String revision;
}