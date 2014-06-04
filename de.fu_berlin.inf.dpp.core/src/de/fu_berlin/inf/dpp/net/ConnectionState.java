package de.fu_berlin.inf.dpp.net;

import java.util.EnumSet;

/**
 * <pre>
 *    . . . . . /--------------------------------\
 *    . . . . . | . . . . . . . . . . . . . . .. |
 *    . . . . . v . . . . . . . . . . . . . . .. |
 *    o-> NOT_CONNECTED ----> CONNECTING <---> ERROR
 *    . . . . . ^ . . . . . . . . | . . . . . .. ^
 *    . . . . . | . . . . . . . . v . . . . . .. |
 *    . . DISCONNECTING <---- CONNECTED ---------/
 * </pre>
 */

public enum ConnectionState {

    /**
     * Valid next states: CONNECTING (usually triggered by an user action to
     * connect)
     */
    NOT_CONNECTED {
        @Override
        public EnumSet<ConnectionState> getAllowedFollowState() {
            return EnumSet.of(ConnectionState.CONNECTING);
        }
    },

    /**
     * Valid next states:
     * 
     * - ERROR (if the attempt to connect failed)
     * 
     * - CONNECTED (if the attempt to connect was successful)
     */
    CONNECTING {
        @Override
        public EnumSet<ConnectionState> getAllowedFollowState() {
            return EnumSet.of(ConnectionState.CONNECTED, ConnectionState.ERROR);
        }
    },

    /**
     * Valid follow states:
     * 
     * - ERROR (if the connection broke)
     * 
     * - DISCONNECTING (if the user disconnected)
     */
    CONNECTED {
        @Override
        public EnumSet<ConnectionState> getAllowedFollowState() {
            return EnumSet.of(ConnectionState.DISCONNECTING,
                ConnectionState.ERROR);
        }
    },

    /**
     * Valid follow states:
     * 
     * - NOT_CONNECTED
     */
    DISCONNECTING {
        @Override
        public EnumSet<ConnectionState> getAllowedFollowState() {
            return EnumSet.of(ConnectionState.NOT_CONNECTED);
        }
    },

    /**
     * Valid follow states:
     * 
     * - NOT_CONNECTED
     * 
     * - CONNECTING
     */
    ERROR() {
        @Override
        public EnumSet<ConnectionState> getAllowedFollowState() {
            return EnumSet.of(ConnectionState.NOT_CONNECTED,
                ConnectionState.CONNECTING);
        }
    };

    public boolean isValidFollowState(ConnectionState newState) {
        return this.getAllowedFollowState().contains(newState);
    }

    public abstract EnumSet<ConnectionState> getAllowedFollowState();

}