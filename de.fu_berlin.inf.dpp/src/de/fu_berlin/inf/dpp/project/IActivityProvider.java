package de.fu_berlin.inf.dpp.project;

import org.xmlpull.v1.XmlPullParser;

import de.fu_berlin.inf.dpp.activities.IActivity;

public interface IActivityProvider extends ISessionListener {
    public void exec(IActivity activity);
    
    public void addActivityListener(IActivityListener listener);
    public void removeActivityListener(IActivityListener listener);
    
    public IActivity fromXML(XmlPullParser parser);
    public String toXML(IActivity activity);
}
