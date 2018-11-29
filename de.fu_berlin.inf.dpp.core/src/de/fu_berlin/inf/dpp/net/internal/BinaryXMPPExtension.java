package de.fu_berlin.inf.dpp.net.internal;

import de.fu_berlin.inf.dpp.net.stream.StreamMode;

public final class BinaryXMPPExtension {

  private TransferDescription transferDescription;

  private int chunkCount;
  private long transferredSize;
  private long uncompressedSize;
  private byte[] payload;
  private long transferDuration;
  private StreamMode transferMode;

  public BinaryXMPPExtension(
      StreamMode transferMode, TransferDescription transferDescription, int chunkCount) {
    this.transferMode = transferMode;
    this.transferDescription = transferDescription;
    this.chunkCount = chunkCount;
    transferredSize = 0;
    uncompressedSize = 0;
    transferDuration = System.currentTimeMillis();
  }

  /**
   * Returns the XMPP packet extension in binary form. <b>Note:</b>The returned byte array <b>must
   * not</b> be modified directly.
   */
  byte[] getPayload() {
    return payload;
  }

  /** Returns the transfer description of this transfer object. */
  // public for STF
  public TransferDescription getTransferDescription() {
    return transferDescription;
  }

  /** Returns the {@link StreamMode} that was used to receive theXMPP packet extension. */
  StreamMode getTransferMode() {
    return transferMode;
  }

  /** Returns the size of the XMPP packet extension in bytes before decompression. */
  long getCompressedSize() {
    return transferredSize;
  }

  /** Returns the size of the XMPP packet extension in bytes after decompression. */
  long getUncompressedSize() {
    return uncompressedSize;
  }

  /** Returns the time in milliseconds until the transfer was completed. */
  long getTransferDuration() {
    return transferDuration;
  }

  /**
   * Sets the data for this XMPP packet extension.
   *
   * @param originalSize the original size of the received data
   * @param data the binary form of the XMPP packet extension
   * @throws IllegalStateException if there are still missing chunks, see also {@link #isLastChunk}
   */
  void setPayload(long originalSize, byte[] data) {

    if (chunkCount > 0)
      throw new IllegalStateException("there are chunks missing: " + chunkCount + " > 0");

    payload = data;
    transferredSize = originalSize;
    uncompressedSize = data.length;
  }

  /**
   * Checks if all outstanding chunks have arrived. This method <b>must</b> be called after a chunk
   * has been received.
   *
   * @return <code>true</code> if {@link #setPayload} can now be called, <code>false</code>
   *     otherwise
   */
  boolean isLastChunk() {
    boolean last = (--chunkCount <= 0);

    if (last) transferDuration = System.currentTimeMillis() - transferDuration;

    return last;
  }
}
