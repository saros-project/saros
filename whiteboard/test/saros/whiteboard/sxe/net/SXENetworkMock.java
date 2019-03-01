package saros.whiteboard.sxe.net;

public class SXENetworkMock {

  public ISXETransmitter getClientMock() {
    return new MockedSXETransmitter(this);
  }

  public void sendMessage(String raw, String to) {
    // TODO Auto-generated method stub
  }

  // public ISXETransmitter getHostMock();

}
