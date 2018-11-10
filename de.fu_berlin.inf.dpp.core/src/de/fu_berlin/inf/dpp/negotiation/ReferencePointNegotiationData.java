package de.fu_berlin.inf.dpp.negotiation;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * This class contains all the information that the remote user needs during a
 * reference point negotiation. The {@link FileList} of the whole reference
 * point, the reference point name and the session wide reference point id.
 */

@XStreamAlias("PJNGDATA")
public class ReferencePointNegotiationData {

    @XStreamAlias("name")
    @XStreamAsAttribute
    private final String referencePointName;

    @XStreamAlias("pid")
    @XStreamAsAttribute
    private final String referencePointID;

    @XStreamAlias("partial")
    @XStreamAsAttribute
    private final boolean partial;

    @XStreamAlias("filelist")
    private final FileList fileList;

    /**
     * 
     * @param referencePointID
     *            Session wide ID of the referencePoint. This ID is the same for
     *            all users.
     * @param referencePointName
     *            Name of the source, which is point by the referencePoint on
     *            inviter side.
     * @param fileList
     *            complete list of all files that are part of the sharing for
     *            the given referencePoint
     */
    public ReferencePointNegotiationData(String referencePointID,
        String referencePointName, boolean partial, FileList fileList) {

        this.fileList = fileList;
        this.referencePointName = referencePointName;
        this.referencePointID = referencePointID;
        this.partial = partial;
    }

    public FileList getFileList() {
        return fileList;
    }

    public String getReferencePointName() {
        return referencePointName;
    }

    public String getReferencePointID() {
        return referencePointID;
    }

    public boolean isPartial() {
        return partial;
    }
}