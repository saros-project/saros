package saros.net;

import saros.net.stream.StreamMode;

/**
 * Listener for tracking network traffic. <b>Note:</b> It is up to the implementation to do
 * synchronization as the methods offered by this listener may be called in parallel.
 */
public interface ITransferListener {

  /**
   * Gets called when data was sent. The actual size of the real data that was send is the
   * compressed size.
   *
   * @param mode the {@link StreamMode mode} used for sending
   * @param sizeCompressed the compressed size of the data
   * @param sizeUncompressed the uncompressed size of the data
   * @param duration time in milliseconds it took to send the data
   */
  public void sent(StreamMode mode, long sizeCompressed, long sizeUncompressed, long duration);

  /**
   * Gets called when data was received. The actual size of the real data that was received is the
   * compressed size.
   *
   * @param mode the {@link StreamMode mode} used for receiving
   * @param sizeCompressed the compressed size of the data
   * @param sizeUncompressed the uncompressed size of the data
   * @param duration time in milliseconds it took to receive the data
   */
  public void received(StreamMode mode, long sizeCompressed, long sizeUncompressed, long duration);
}
