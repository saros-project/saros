package saros.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import saros.net.internal.IConnection;

public interface IStreamConnection extends IConnection {

  public InputStream getInputStream() throws IOException;

  public OutputStream getOutputStream() throws IOException;

  public int getReadTimeout() throws IOException;

  public void setReadTimeout(int timeout) throws IOException;
}
