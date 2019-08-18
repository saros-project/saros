package saros.net.internal;

import saros.net.stream.StreamMode;

public interface IConnection {

  public Object getLocalAddress();

  public Object getRemoteAddress();

  public StreamMode getMode();

  public String getId();

  public void close();
}
