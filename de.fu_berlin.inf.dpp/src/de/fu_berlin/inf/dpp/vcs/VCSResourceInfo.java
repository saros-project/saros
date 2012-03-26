package de.fu_berlin.inf.dpp.vcs;

/**
 * A simple struct to hold VCS related information associated with a resource.
 */
// TODO Encapsulate fields?
public class VCSResourceInfo {
    /** The URL of the remote resource in the repository, or null. */
    public String url;
    /** The revision of the resource as a String, or null. */
    public String revision;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
            + ((revision == null) ? 0 : revision.hashCode());
        result = prime * result + ((url == null) ? 0 : url.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        VCSResourceInfo other = (VCSResourceInfo) obj;
        if (revision == null) {
            if (other.revision != null)
                return false;
        } else if (!revision.equals(other.revision))
            return false;
        if (url == null) {
            if (other.url != null)
                return false;
        } else if (!url.equals(other.url))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return url + "@" + revision;
    }
}