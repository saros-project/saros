package saros.net;

public interface IStreamConnectionListener {

  public boolean streamConnectionEstablished(String id, IStreamConnection connection);
}
