package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents;


/**
 * Some events e.g. popUp window "Create new XMPP Account"can be triggered by
 * different click_path like clicking mainMenu Saros-> Create Account or
 * clicking toolbarButton connect. So functions to handle such events would be
 * defined in this interface, so that all other saros_feature contained
 * components can use them.
 * 
 * 
 * @author lchen
 */
public interface SarosComponent extends EclipseComponent {

    /**********************************************
     * 
     * action
     * 
     **********************************************/

}
