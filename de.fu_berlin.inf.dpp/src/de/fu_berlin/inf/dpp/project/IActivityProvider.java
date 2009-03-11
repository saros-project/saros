package de.fu_berlin.inf.dpp.project;

import org.xmlpull.v1.XmlPullParser;

import de.fu_berlin.inf.dpp.activities.IActivity;

/**
 * Every activity provider is responsible for one or more activities. It handles
 * creating and executing the activity.
 * 
 * TODO Why does it extend {@link ISessionListener}? Just because every
 * {@link IActivityProvider} needs the methods from {@link ISessionListener}? I
 * think there is no semantic reason.
 * 
 * @author rdjemili
 */
public interface IActivityProvider extends ISessionListener {

    public void exec(IActivity activity);

    public void addActivityListener(IActivityListener listener);

    public void removeActivityListener(IActivityListener listener);

    /**
     * Converts the XML doc to an activity.
     */
    public IActivity fromXML(XmlPullParser parser);

}
