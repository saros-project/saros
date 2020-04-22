package saros.net.xmpp.filetransfer;

import java.io.IOException;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import org.jivesoftware.smackx.filetransfer.FileTransfer;

/** This class provides information about a XMPP file transfer. */
public class XMPPFileTransfer {

  private static final int CHECK_SLEEP_MILLIS = 200;

  /** Status of a file transfer. */
  public enum Status {
    NEGOTIATING,
    IN_PROGRESS,
    COMPLETED,
    CANCELED,
    ERROR
  }

  private final FileTransfer transfer;

  XMPPFileTransfer(FileTransfer transfer) {
    this.transfer = transfer;
  }

  /**
   * Get {@link Status} of current transfer.
   *
   * @return {@link Status} of current transfer
   */
  public Status getStatus() {
    switch (transfer.getStatus()) {
      case initial:
      case negotiating_stream:
      case negotiating_transfer:
      case negotiated:
        return Status.NEGOTIATING;
      case in_progress:
        return Status.IN_PROGRESS;
      case complete:
        return Status.COMPLETED;
      case cancelled:
      case refused:
        return Status.CANCELED;
      case error:
      default:
        return Status.ERROR;
    }
  }

  /**
   * This method provides an exception if the transfer failed.
   *
   * @return Optional with IOException if available
   */
  public Optional<IOException> getException() {
    Exception transferException = transfer.getException();
    if (transferException == null) return Optional.empty();

    return Optional.of(
        new IOException(
            "File transfer failed. Maybe the remote side canceled the transfer.",
            transferException));
  }

  /**
   * Wait till the transfer is in progress or is canceled.
   *
   * <p>Only useful for outgoing file transfers.
   *
   * @param additionalCancelCheck nullable an additional abort condition checked while waiting
   */
  public void waitForTransferStart(BooleanSupplier additionalCancelCheck) {
    try {
      while (getStatus() == Status.NEGOTIATING) {
        if (additionalCancelCheck != null && additionalCancelCheck.getAsBoolean()) {
          transfer.cancel();
          return;
        }

        Thread.sleep(CHECK_SLEEP_MILLIS);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      transfer.cancel();
      return;
    }
  }

  public boolean isDone() {
    return transfer.isDone();
  }

  public long getAmountWritten() {
    return transfer.getAmountWritten();
  }

  public long getFileSize() {
    return transfer.getFileSize();
  }

  public void cancel() {
    transfer.cancel();
  }

  @Override
  public String toString() {
    return String.format(
        "XMPPFileTransfer [Peer=%s, FileName=%s, Status=%s]",
        transfer.getPeer(), transfer.getFileName(), getStatus());
  }
}
