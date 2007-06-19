package de.fu_berlin.inf.dpp.editor.annotations;

import org.eclipse.jface.text.source.Annotation;
import de.fu_berlin.inf.dpp.*;
import de.fu_berlin.inf.dpp.net.JID;

/**
 * Marks text selected by both driver and observer.
 * 
 * Configuration of this annotation is done in the plugin-xml
 * 
 * @author coezbek
 */
public class SelectionAnnotation extends Annotation  {

	// base TYPE name, will be extended for different remote users
	public static final String TYPE = "de.fu_berlin.inf.dpp.annotations.selection";
	
	public SelectionAnnotation() {
		this(null);
	}

	public SelectionAnnotation(String username) {
		super(TYPE, false, username);

		// TODO: improve color assingment and dynamic handling 
		int colorid=getColorIdForUser(username) +1;
		String mytype=TYPE + "." + new Integer(colorid).toString();
		
		setType(mytype);
		
	}

	int getColorIdForUser(String username){
		User user= Saros.getDefault().getSessionManager().getSharedProject().
					getParticipant(new JID(username));
	
		int colorid=1;
		if (user!=null) {
			colorid=user.getColorID();
		}
		
		return colorid;
	}	
}
