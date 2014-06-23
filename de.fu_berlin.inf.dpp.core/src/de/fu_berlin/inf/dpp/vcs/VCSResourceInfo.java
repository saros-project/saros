package de.fu_berlin.inf.dpp.vcs;

import org.apache.commons.lang.ObjectUtils;

/**
 * POJO to hold VCS related information associated with a resource.
 */
public final class VCSResourceInfo {

    private final String url;
    private final String revision;

    /**
     * Creates a new VCSResourceInfo object with the given URL and revision.
     * 
     * @param url
     * @param revision
     */
    public VCSResourceInfo(final String url, final String revision) {
        this.url = url;
        this.revision = revision;
    }

    /**
     * Returns the URL of the remote resource in the repository.
     * 
     * @return the URL of the remote resource in the repository, or
     *         <code>null</code> if not available
     */
    public String getURL() {
        return url;
    }

    /**
     * Returns the revision of the resource as string.
     * 
     * @return the revision of the resource, or <code>null</code> if not
     *         available
     */
    public String getRevision() {
        return revision;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ObjectUtils.hashCode(revision);
        result = prime * result + ObjectUtils.hashCode(url);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof VCSResourceInfo))
            return false;

        VCSResourceInfo other = (VCSResourceInfo) obj;

        return ObjectUtils.equals(revision, other.revision)
            && ObjectUtils.equals(url, other.url);
    }

    @Override
    public String toString() {
        return url + "@" + revision;
    }
}