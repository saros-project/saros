/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universitaet Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package de.fu_berlin.inf.dpp.net.internal.extensions;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.filter.PacketExtensionFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.PacketExtension;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import de.fu_berlin.inf.dpp.activities.AbstractActivity;
import de.fu_berlin.inf.dpp.activities.EditorActivity;
import de.fu_berlin.inf.dpp.activities.FileActivity;
import de.fu_berlin.inf.dpp.activities.FolderActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.RoleActivity;
import de.fu_berlin.inf.dpp.activities.TextEditActivity;
import de.fu_berlin.inf.dpp.activities.TextSelectionActivity;
import de.fu_berlin.inf.dpp.activities.ViewportActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Request;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.JupiterVectorTime;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.DeleteOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.InsertOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.NoOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.SplitOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.TimestampOperation;
import de.fu_berlin.inf.dpp.net.TimedActivity;
import de.fu_berlin.inf.dpp.util.xstream.IPathConverter;

public class ActivitiesPacketExtension implements PacketExtension {

    private static final Logger log = Logger
        .getLogger(ActivitiesPacketExtension.class.getName());

    protected static XStream xstream;

    public static PacketFilter getFilter() {
        return new PacketExtensionFilter(ELEMENT, NAMESPACE);
    }

    // TODO This string constant is defined several times throughout the source.
    public static final String NAMESPACE = "de.fu_berlin.inf.dpp";

    public static final String ELEMENT = "activities";

    private List<TimedActivity> activities;

    private String sessionID;

    /**
     * Simple helper class for (de)serializion with {@link XStream}.
     * 
     * Only the first sequence number of the contained activities is stored and
     * transmitted, which means all given activities must have consecutive,
     * increasing sequence numbers.
     */
    @XStreamAlias(ELEMENT)
    public static class Content {
        @XStreamAsAttribute
        protected String xmlns = NAMESPACE;

        @XStreamAsAttribute
        protected String sessionID;

        @XStreamAsAttribute
        protected int firstSequenceNumber;

        @XStreamImplicit
        protected List<IActivity> activities;

        public Content(String sessionID, List<TimedActivity> activities)
            throws IllegalArgumentException {

            this.sessionID = sessionID;
            this.firstSequenceNumber = activities.get(0).getSequenceNumber();
            int sequenceNumber = this.firstSequenceNumber;
            this.activities = new ArrayList<IActivity>(activities.size());
            for (TimedActivity timedActivity : activities) {
                if (timedActivity.getSequenceNumber() != sequenceNumber) {
                    log
                        .error("Sequence number in activity ("
                            + timedActivity.getSequenceNumber()
                            + ") does not match expected number: "
                            + sequenceNumber);
                    throw new IllegalArgumentException(
                        "sequence numbers must be consecutive");
                }
                sequenceNumber++;

                this.activities.add(timedActivity.getActivity());
            }
        }

        public String getSessionID() {
            return sessionID;
        }

        public List<TimedActivity> getTimedActivities() {
            ArrayList<TimedActivity> result = new ArrayList<TimedActivity>(
                activities.size());
            /*
             * There is only one sequence number in the message, so all
             * activities get increasing numbers based on that sequence number.
             */
            int sequenceNumber = firstSequenceNumber;
            for (IActivity activity : activities) {
                result.add(new TimedActivity(activity, sequenceNumber++));
            }
            return result;
        }
    }

    public ActivitiesPacketExtension(String sessionID,
        List<TimedActivity> activities) {
        if (activities.size() == 0) {
            throw new IllegalArgumentException("activities are empty");
        }
        this.sessionID = sessionID;
        this.activities = activities;
    }

    public static XStream getXStream() {
        if (xstream == null) {
            xstream = new XStream();
            /*
             * Register converters and classes that will be (de)serialized with
             * the XStream instance.
             */
            xstream.registerConverter(new IPathConverter());
            xstream.processAnnotations(Content.class);
            // Add new activities here:
            xstream.processAnnotations(new Class[] { AbstractActivity.class,
                EditorActivity.class, FileActivity.class, FolderActivity.class,
                RoleActivity.class, TextEditActivity.class,
                TextSelectionActivity.class, ViewportActivity.class });
            xstream.processAnnotations(new Class[] { Request.class,
                JupiterVectorTime.class, DeleteOperation.class,
                InsertOperation.class, NoOperation.class, SplitOperation.class,
                TimestampOperation.class });
        }
        return xstream;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jivesoftware.smack.packet.PacketExtension#getElementName()
     */
    public String getElementName() {
        return ActivitiesPacketExtension.ELEMENT;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jivesoftware.smack.packet.PacketExtension#getNamespace()
     */
    public String getNamespace() {
        return ActivitiesPacketExtension.NAMESPACE;
    }

    public List<TimedActivity> getActivities() {
        return this.activities;
    }

    public String toXML() {
        return getXStream().toXML(new Content(sessionID, activities));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
            + ((activities == null) ? 0 : activities.hashCode());
        result = prime * result
            + ((sessionID == null) ? 0 : sessionID.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof ActivitiesPacketExtension))
            return false;
        ActivitiesPacketExtension other = (ActivitiesPacketExtension) obj;

        if (!ObjectUtils.equals(this.sessionID, other.sessionID))
            return false;

        if (!ObjectUtils.equals(this.activities, other.activities))
            return false;

        return true;
    }

    public String getSessionID() {
        return sessionID;
    }
}
