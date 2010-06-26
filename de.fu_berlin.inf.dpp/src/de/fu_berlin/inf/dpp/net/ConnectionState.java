/**
 * 
 */
package de.fu_berlin.inf.dpp.net;

import java.util.EnumSet;

public enum ConnectionState {
    // . . . . . /--------------------------------\
    // . . . . . | . . . . . . . . . . . . . . .. |
    // . . . . . v . . . . . . . . . . . . . . .. |
    // o-> NOT_CONNECTED ----> CONNECTING <---> ERROR
    // . . . . . ^ . . . . . . . . | . . . . . .. ^
    // . . . . . | . . . . . . . . v . . . . . .. |
    // . . DISCONNECTING <---- CONNECTED ---------/
    /**
     * Saros not connected to a XMPP Server
     * 
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
     * Saros is in the process of connecting
     * 
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
     * Saros is successfully connected to an XMPP server
     * 
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
     * Saros is in the process of disconnecting
     * 
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
     * There is an error in the XMPP connection.
     * 
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