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
package de.fu_berlin.inf.dpp.net.internal;

import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.filter.PacketExtensionFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.PacketExtension;

import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.net.TimedActivity;

public class ActivitiesPacketExtension implements PacketExtension {

    private static final Logger log = Logger
        .getLogger(ActivitiesPacketExtension.class.getName());

    public static PacketFilter getFilter() {
        return new PacketExtensionFilter(ELEMENT, NAMESPACE);
    }

    public static final String NAMESPACE = "de.fu_berlin.inf.dpp";

    public static final String SESSION_ID = "sessionID";

    public static final String ELEMENT = "activities";

    private List<TimedActivity> activities;

    private String sessionID;

    public ActivitiesPacketExtension(String sessionID,
        List<TimedActivity> activities) {
        if (activities.size() == 0) {
            throw new IllegalArgumentException("activities are empty");
        }
        this.sessionID = sessionID;
        this.activities = activities;
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

    /*
     * (non-Javadoc)
     * 
     * @see org.jivesoftware.smack.packet.PacketExtension#toXML()
     */
    public String toXML() {
        StringBuilder buf = new StringBuilder();
        buf.append("<").append(getElementName());
        buf.append(" xmlns=\"").append(getNamespace()).append("\">");

        buf.append(sessionIdToXML());
        /*
         * Only the first sequence number is put into the message, which means
         * all given activities must have consecutive, increasing sequence
         * numbers.
         */
        int sequenceNumber = this.activities.get(0).getSequenceNumber();
        buf.append("<timestamp>").append(sequenceNumber).append("</timestamp>");

        for (TimedActivity timedActivity : this.activities) {
            if (timedActivity.getSequenceNumber() != sequenceNumber) {
                log.error("Sequence number in activity ("
                    + timedActivity.getSequenceNumber()
                    + ") does not match expected number: " + sequenceNumber);
            }
            sequenceNumber++;

            IActivity activity = timedActivity.getActivity();
            buf.append(activity.toXML());
        }

        buf.append("</").append(getElementName()).append(">");
        return buf.toString();
    }

    private String sessionIdToXML() {
        return "<" + ActivitiesPacketExtension.SESSION_ID + ">" + sessionID
            + "</" + ActivitiesPacketExtension.SESSION_ID + ">";
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
