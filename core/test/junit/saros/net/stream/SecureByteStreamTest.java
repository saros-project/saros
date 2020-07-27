package saros.net.stream;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;
import org.junit.Test;
import saros.test.util.TestThread;

public class SecureByteStreamTest {

  private static byte[] secretData =
      new String(
              "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis knostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.")
          .getBytes();

  private static class PipedByteStream implements ByteStream {

    private class RecordingOutputStream extends OutputStream {

      private final ByteArrayOutputStream recordedData = new ByteArrayOutputStream();

      private boolean record = false;

      @Override
      public void write(int b) throws IOException {
        if (record) recordedData.write(b);

        out.write(b);
      }
    }

    private final PipedInputStream in = new PipedInputStream();
    private final PipedOutputStream out = new PipedOutputStream();

    private final RecordingOutputStream recordingOut = new RecordingOutputStream();

    public void connect(PipedByteStream other) throws IOException {
      other.in.connect(out);
      other.out.connect(in);
    }

    @Override
    public InputStream getInputStream() throws IOException {
      return in;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
      return recordingOut;
    }

    @Override
    public void close() throws IOException {
      // NOP
    }

    @Override
    public int getReadTimeout() throws IOException {
      return 0;
    }

    @Override
    public void setReadTimeout(int timeout) throws IOException {
      // NOP
    }
  }

  @Test
  public void testEncryption() throws Exception {

    PipedByteStream a = new PipedByteStream();
    PipedByteStream b = new PipedByteStream();

    a.connect(b);

    // the whole assertion stuff must be done in a thread
    // if a threads terminates that was accessing the pipe then the pipe will become unusable
    TestThread t0 = new TestThread((TestThread.Runnable) () -> testOut(a));
    TestThread t1 = new TestThread((TestThread.Runnable) () -> testIn(b));

    t0.start();
    t1.start();

    t0.join(60000);
    t1.join(60000);

    t0.verify();
    t1.verify();
  }

  private void testOut(PipedByteStream s) throws IOException {
    ByteStream b = SecureByteStream.wrap(s, false);

    s.recordingOut.record = true;

    b.getOutputStream().write(secretData);
    b.getOutputStream().flush();

    byte[] recordedData = s.recordingOut.recordedData.toByteArray();

    // as we use a symmetric cipher compare the length
    if (secretData.length != recordedData.length || Arrays.equals(secretData, recordedData))
      throw new AssertionError(
          "data was not encrypted: "
              + Arrays.toString(secretData)
              + " matches "
              + Arrays.toString(recordedData));
  }

  private void testIn(PipedByteStream s) throws IOException {
    ByteStream b = SecureByteStream.wrap(s, true);

    for (int i = 0; i < secretData.length; i++)
      assertEquals(secretData[i], b.getInputStream().read());
  }
}
