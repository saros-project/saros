package saros.net;

import java.util.EnumSet;

/**
 *
 *
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

  /** Valid next states: CONNECTING (usually triggered by an user action to connect) */
  NOT_CONNECTED {
    @Override
    public EnumSet<ConnectionState> getAllowedFollowState() {
      return EnumSet.of(ConnectionState.CONNECTING);
    }
  },

  /**
   * Valid next states:
   *
   * <p>- ERROR (if the attempt to connect failed)
   *
   * <p>- CONNECTED (if the attempt to connect was successful)
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
   * <p>- ERROR (if the connection broke)
   *
   * <p>- DISCONNECTING (if the user disconnected)
   */
  CONNECTED {
    @Override
    public EnumSet<ConnectionState> getAllowedFollowState() {
      return EnumSet.of(ConnectionState.DISCONNECTING, ConnectionState.ERROR);
    }
  },

  /**
   * Valid follow states:
   *
   * <p>- NOT_CONNECTED
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
   * <p>- NOT_CONNECTED
   *
   * <p>- CONNECTING
   */
  ERROR() {
    @Override
    public EnumSet<ConnectionState> getAllowedFollowState() {
      return EnumSet.of(ConnectionState.NOT_CONNECTED, ConnectionState.CONNECTING);
    }
  };

  public boolean isValidFollowState(ConnectionState newState) {
    return this.getAllowedFollowState().contains(newState);
  }

  public abstract EnumSet<ConnectionState> getAllowedFollowState();
}
