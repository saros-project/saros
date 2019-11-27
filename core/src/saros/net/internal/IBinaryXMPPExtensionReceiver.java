package saros.net.internal;

public interface IBinaryXMPPExtensionReceiver {

  /**
   * Gets called when a {@linkplain BinaryXMPPExtension} was received.
   *
   * @param extension
   */
  public void receive(final BinaryXMPPExtension extension);
}
