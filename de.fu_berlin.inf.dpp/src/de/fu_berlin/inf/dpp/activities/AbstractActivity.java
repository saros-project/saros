package de.fu_berlin.inf.dpp.activities;

import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;
import org.xmlpull.v1.XmlPullParser;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.util.xstream.IPathConverter;
import de.fu_berlin.inf.dpp.util.xstream.UrlEncodingStringConverter;
import de.fu_berlin.inf.dpp.util.xstream.XppReader;

// TODO Add some information what needs to be done to add a new activity.
public abstract class AbstractActivity implements IActivity {

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger(AbstractActivity.class
        .getName());

    // TODO [MR] Move to ActivitiesPacketExtension.
    protected static final XStream xstream = new XStream();

    @XStreamAsAttribute
    @XStreamConverter(UrlEncodingStringConverter.class)
    protected final String source;

    /*
     * TODO [MR] Move to ActivityPacketExtension.
     * 
     * TODO [MR] Create a static getter for this.xstream create the field
     * lazily.
     */
    static {
        /*
         * Register converters and classes that will be (de)serialized with the
         * XStream instance.
         */
        xstream.registerConverter(new IPathConverter());
        xstream.processAnnotations(new Class[] { AbstractActivity.class,
            EditorActivity.class, FileActivity.class, FolderActivity.class,
            RoleActivity.class, TextEditActivity.class,
            TextSelectionActivity.class, ViewportActivity.class });
    }

    public AbstractActivity(String source) {
        if (source == null)
            throw new IllegalArgumentException("Source cannot be null");
        this.source = source;
    }

    public String getSource() {
        return this.source;
    }

    @Override
    public int hashCode() {
        return (source == null) ? 0 : source.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof AbstractActivity))
            return false;

        AbstractActivity other = (AbstractActivity) obj;
        return ObjectUtils.equals(this.source, other.source);
    }

    public User getUser() {
        return Saros.getDefault().getSessionManager().getSharedProject()
            .getParticipant(new JID(source));
    }

    public static IActivity parse(XmlPullParser parser) {
        return (IActivity) xstream.unmarshal(new XppReader(parser));
    }
}
