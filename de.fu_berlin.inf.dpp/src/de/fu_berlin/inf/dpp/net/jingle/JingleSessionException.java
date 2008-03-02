package de.fu_berlin.inf.dpp.net.jingle;

/**
 * this exception is throw if jingle session request is failed.
 * @author orieger
 *
 */
public class JingleSessionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4395402562918748941L;
	
	public JingleSessionException(String exception){
		super(exception);
	}

}
