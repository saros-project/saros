package saros.net.stream;

public interface IStreamServiceListener {

  /** Gets called when an incoming connection request was successfully performed. */
  public void connectionEstablished(ByteStream byteStream);
}
