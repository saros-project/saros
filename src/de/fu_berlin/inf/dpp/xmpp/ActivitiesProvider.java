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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.xmlpull.mxp1.MXParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import de.fu_berlin.inf.dpp.activities.CursorLineActivity;
import de.fu_berlin.inf.dpp.activities.CursorOffsetActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.ResourceAddActivity;
import de.fu_berlin.inf.dpp.activities.ResourceRemoveActivity;
import de.fu_berlin.inf.dpp.activities.RoleActivity;
import de.fu_berlin.inf.dpp.activities.TextEditActivity;
import de.fu_berlin.inf.dpp.activities.TextLoadActivity;

public class ActivitiesProvider implements PacketExtensionProvider { // TODO rename to ActivitiesExtensionProvider

    public ActivitiesPacketExtension parseExtension(XmlPullParser parser) 
            throws XmlPullParserException, IOException {

        List<IActivity> activities = new ArrayList<IActivity>();
        int time = -1;

        boolean done = false;
        while (!done) {
             int eventType = parser.next();
             if (eventType == XmlPullParser.START_TAG) {
                 
                 if (parser.getName().equals("time")) {
                     time = parseTime(parser);
                     
                 } if (parser.getName().equals(ActivitiesPacketExtension.TEXT_CHANGE_TAG)) {
                     parseTextEdit(parser, activities);
                     
                 } else if (parser.getName().equals("cursorLine")) {
                     parseCursorLine(parser, activities);
                     
                 } else if (parser.getName().equals("cursorOffset")) {
                     parseCursorOffset(parser, activities);
                     
                 } else if (parser.getName().equals("driver")) {
                     parseRole(parser, activities);
                     
                 } else if (parser.getName().equals("activated")) {
                     parseTextLoad(parser, activities);
                     
//                 } else if (parser.getName().equals("resourceAdd")) { 
//                     parseResourceAdd(parser, activities);
                 
                 } else if (parser.getName().equals("resourceRemove")) {
                     parseResourceRemove(parser, activities);
                 }
                 
             } else if (eventType == XmlPullParser.END_TAG) {
                 if (parser.getName().equals("activities")) {
                     done = true;
                 }
             }
        }
        
        return new ActivitiesPacketExtension(activities, time);
    }
    
    private int parseTime(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.next(); // read text
        int time = Integer.parseInt(parser.getText());
        parser.next(); // read end tag
        
        return time;
    }
    
//    private void parseResourceAdd(XmlPullParser parser, List<IActivity> activities) throws XmlPullParserException, IOException {
//        IPath path = new Path(parser.getAttributeValue(null, "path"));
//        
//        if (parser.next() == MXParser.TEXT) { // read text
//            String content = parser.getText();
//            activities.add(new ResourceAddActivity(path, content));
//            parser.next(); // read end tag
//        } else {
//            activities.add(new ResourceAddActivity(path));
//        }
//    }
    
    private void parseResourceRemove(XmlPullParser parser, List<IActivity> activities)
        throws XmlPullParserException, IOException {

        IPath path = new Path(parser.getAttributeValue(null, "path"));
        activities.add(new ResourceRemoveActivity(path));
    }

    private void parseTextLoad(XmlPullParser parser, List<IActivity> activities) {
        String path = parser.getAttributeValue(null, "path");
        activities.add(new TextLoadActivity(path));
    }

    private void parseTextEdit(XmlPullParser parser, List<IActivity> activities)
        throws XmlPullParserException, IOException {
        
        int offset = Integer.parseInt(parser.getAttributeValue(null, "offset"));  //TODO extract constants
        int replace = Integer.parseInt(parser.getAttributeValue(null, "replace"));
    
        String text = "";
        if (parser.next() == XmlPullParser.TEXT) {
            text = parser.getText();
        }
    
        activities.add(new TextEditActivity(offset, text, replace));
    }

    private void parseCursorLine(XmlPullParser parser, List<IActivity> activities) {
        int startLine = Integer.parseInt(parser.getAttributeValue(null, "startLine"));  //TODO extract constants
        int endLine = Integer.parseInt(parser.getAttributeValue(null, "endLine"));
        activities.add(new CursorLineActivity(startLine, endLine));
    }
    
    private void parseCursorOffset(XmlPullParser parser, List<IActivity> activities) {
        int offset = Integer.parseInt(parser.getAttributeValue(null, "offset"));  //TODO extract constants
        int length = Integer.parseInt(parser.getAttributeValue(null, "length"));
        activities.add(new CursorOffsetActivity(offset, length));
    }

    private void parseRole(XmlPullParser parser, List<IActivity> activities) {
        JID user = new JID(parser.getAttributeValue(null, "id"));
        activities.add(new RoleActivity(user));
    }
}
