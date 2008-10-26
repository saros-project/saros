package de.fu_berlin.inf.dpp.project;

import org.xmlpull.v1.XmlPullParser;

import de.fu_berlin.inf.dpp.activities.IActivity;

/**
 * Every activity provider is responsible for one or more activities. It handles
 * creating and executing the activity.
 * 
 * @author rdjemili
 */
public interface IActivityProvider extends ISessionListener {
    public void addActivityListener(IActivityListener listener);

    public void exec(IActivity activity);

    /**
     * Converts the XML doc to an activity.
     */
    public IActivity fromXML(XmlPullParser parser);

    public void removeActivityListener(IActivityListener listener);

    /**
     * Converts the given activity to a XML format.
     */
    public String toXML(IActivity activity);
}
