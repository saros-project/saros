/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2006
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
package de.fu_berlin.inf.dpp.xmpp;


import java.text.MessageFormat;
import java.util.List;

import org.jivesoftware.smack.packet.PacketExtension;

import de.fu_berlin.inf.dpp.activities.CursorLineActivity;
import de.fu_berlin.inf.dpp.activities.CursorOffsetActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.ResourceRemoveActivity;
import de.fu_berlin.inf.dpp.activities.RoleActivity;
import de.fu_berlin.inf.dpp.activities.TextEditActivity;
import de.fu_berlin.inf.dpp.activities.TextLoadActivity;

public class ActivitiesPacketExtension implements PacketExtension {
    public static final String NAMESPACE        = "de.fu_berlin.inf.dpp";
    public static final String ELEMENT          = "activities";
    public static final String TEXT_CHANGE_TAG  = "edit";

    private int                time;
    private List<IActivity>    activities;

    
    MessageFormat textChangeFormat = new MessageFormat(
        "<{0} offset=\"{1}\" replace=\"{2}\">{3}</{4}>"); // TODO extract into consts

    public ActivitiesPacketExtension() {
    }
    
    public ActivitiesPacketExtension(List<IActivity> activities, int time) {
        setActivities(activities);
        this.time = time;
    }

    /* (non-Javadoc)
     * @see org.jivesoftware.smack.packet.PacketExtension#getElementName()
     */
    public String getElementName() {
        return ELEMENT;
    }

    /* (non-Javadoc)
     * @see org.jivesoftware.smack.packet.PacketExtension#getNamespace()
     */
    public String getNamespace() {
        return NAMESPACE;
    }
    
    public void setActivities(List<IActivity> activities) {
        this.activities = activities;
    }
    
    public List<IActivity> getActivities() {
        return activities;
    }
    
    public int getTime() {
        return time;
    }
    
    /* (non-Javadoc)
     * @see org.jivesoftware.smack.packet.PacketExtension#toXML()
     */
    public String toXML() {
        StringBuffer buf = new StringBuffer();
        buf.append("<").append(getElementName());
        buf.append(" xmlns=\"").append(getNamespace()).append("\">");
        
        buf.append("<time>").append(time).append("</time>");
        
        for (IActivity activity : activities) {
            if (activity instanceof TextEditActivity) {
                TextEditActivity textEditActivity = (TextEditActivity)activity;
                buf.append("<edit " +
                    "offset=\""+textEditActivity.offset+"\" " +
                    "replace=\""+textEditActivity.replace+"\">");
                buf.append("<![CDATA[").append(textEditActivity.text).append("]]>");
                buf.append("</").append("edit").append(">");
                
            } else if (activity instanceof RoleActivity) {
                RoleActivity roleActivity = (RoleActivity)activity;
                buf.append("<driver " +
                    "id=\""+roleActivity.getDriver().getJID()+"\" />");
                
            } else if (activity instanceof TextLoadActivity) {
                TextLoadActivity textLoadActivity = (TextLoadActivity)activity;
                buf.append("<activated " +
                    "path=\""+textLoadActivity.getPath()+"\" />");
                
            } else if (activity instanceof CursorLineActivity) {
                CursorLineActivity cursorLine = (CursorLineActivity)activity;
                buf.append("<cursorLine " +
                    "startLine=\""+cursorLine.getStartLine()+"\" "+
                    "endLine=\""+cursorLine.getEndLine()+"\" />");
                
            } else if (activity instanceof CursorOffsetActivity) {
                CursorOffsetActivity cursorOffset = (CursorOffsetActivity)activity;
                buf.append("<cursorOffset " +
                    "offset=\""+cursorOffset.getOffset()+"\" "+
                    "length=\""+cursorOffset.getLength()+"\" />");
                
//            } else if (activity instanceof ResourceAddActivity) {
//                ResourceAddActivity resourceActivity = (ResourceAddActivity)activity;
//                buf.append("<resourceAdd " +
//                    "path=\""+resourceActivity.getPath()+"\" " +
//                    "content=\""+resourceActivity.getContent()+"\" />");
//                
//                if (resourceActivity.getContent() != null) {
//                    buf.append("<![CDATA[").append(resourceActivity.getContent()).append("]]>");
//                }
//                buf.append("</resource>");
                
            } else if (activity instanceof ResourceRemoveActivity) {
                ResourceRemoveActivity resourceActivity = (ResourceRemoveActivity)activity;
                buf.append("<resourceRemove " + 
                    "path=\""+resourceActivity.getPath()+"\" />");
            }
        }
        
        buf.append("</").append(getElementName()).append(">");
        return buf.toString();
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ActivitiesPacketExtension) {
            ActivitiesPacketExtension other = (ActivitiesPacketExtension)obj;
            
            return activities.equals(other.getActivities());
        }
        
        return false;
    }
}
