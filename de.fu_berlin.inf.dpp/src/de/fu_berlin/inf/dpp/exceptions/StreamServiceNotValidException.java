/**
 * 
 */
package de.fu_berlin.inf.dpp.exceptions;

import de.fu_berlin.inf.dpp.net.internal.StreamService;

/**
 * A {@link StreamService} is not valid. Restrictions are mentioned in
 * {@link StreamService}s methods.
 */
public class StreamServiceNotValidException extends StreamException {
    private static final long serialVersionUID = -1810937057344570536L;

    public StreamService invalidService;

    /**
     * 
     * @param message
     *            why {@link #invalidService} is not valid
     * @param invalidService
     *            which contains errors
     */
    public StreamServiceNotValidException(String message,
        StreamService invalidService) {
        super(message);
        this.invalidService = invalidService;
    }

}
