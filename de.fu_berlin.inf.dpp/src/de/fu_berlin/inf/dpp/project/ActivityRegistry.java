package de.fu_berlin.inf.dpp.project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.project.internal.RoleManager;

public class ActivityRegistry {
	private List<IActivityProvider> activityProviders = new ArrayList<IActivityProvider>();
	
	private static ActivityRegistry instance;
	
	public static ActivityRegistry getDefault() {
		if (instance == null)
			instance = new ActivityRegistry();
		
		return instance;
	}
	
	private ActivityRegistry() {
		loadDefaultActivityProviders();
		loadExtensionPoints();
	}
	
	public void addProvider(IActivityProvider provider) {
		if (!activityProviders.contains(provider))
			activityProviders.add(provider);
	}
	
	public IActivity parseActivity(XmlPullParser parser) {
		IActivity activity = null;
		for (IActivityProvider provider : activityProviders) {
			activity = provider.fromXML(parser);
			
			if (activity != null)
				return activity;
		}
		
		return null;
	}
	
	public String toXML(IActivity activity) {
		String xml;
		for (IActivityProvider provider : activityProviders) {
			xml = provider.toXML(activity);
			
			if (xml != null)
				return xml;
		}
		
		return null;
	}
	
	private void loadDefaultActivityProviders() {
		addProvider(EditorManager.getDefault());
		addProvider(new SharedResourcesManager());
		addProvider(new RoleManager());
	}
	
	private void loadExtensionPoints() {
		// TODO
	}
}
