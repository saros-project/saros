package saros.net.internal;

/** Listener interface for {@link IPacketConnection packet connections}. */
public interface IPacketConnectionListener {

  /**
   * Gets called when a new packet connection was established. The connection is <b>not</b>
   * initialized at this point.
   *
   * @param connection
   */
  public void connectionEstablished(IPacketConnection connection);
}
