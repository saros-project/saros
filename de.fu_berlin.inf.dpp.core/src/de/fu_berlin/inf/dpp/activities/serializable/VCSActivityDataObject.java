package de.fu_berlin.inf.dpp.activities.serializable;

import java.util.Vector;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.IResourceActivity;
import de.fu_berlin.inf.dpp.activities.business.VCSActivity;
import de.fu_berlin.inf.dpp.activities.business.VCSActivity.Type;
import de.fu_berlin.inf.dpp.filesystem.IPathFactory;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.session.ISarosSession;

@XStreamAlias("vcsActivity")
public class VCSActivityDataObject extends AbstractProjectActivityDataObject {

    protected String param1;
    protected String url;
    protected String directory;
    @XStreamAsAttribute
    protected Type type;
    public Vector<IActivityDataObject> containedActivity;

    public VCSActivityDataObject(JID source, VCSActivity.Type type, String url,
        SPath path, String directory, String param1,
        Vector<IActivityDataObject> containedActivity) {

        super(source, path);

        this.type = type;
        this.url = url;
        this.directory = directory;
        this.param1 = param1;
        this.containedActivity = containedActivity;
    }

    @Override
    public IActivity getActivity(ISarosSession sarosSession,
        IPathFactory pathFactory) {
        final VCSActivity vcsActivity = new VCSActivity(
            sarosSession.getUser(getSource()), type, getPath(), url, directory,
            param1);

        vcsActivity.containedActivity.ensureCapacity(containedActivity.size());
        for (IActivityDataObject ado : containedActivity) {
            vcsActivity.containedActivity.add((IResourceActivity) ado
                .getActivity(sarosSession, pathFactory));
        }
        return vcsActivity;
    }
}
