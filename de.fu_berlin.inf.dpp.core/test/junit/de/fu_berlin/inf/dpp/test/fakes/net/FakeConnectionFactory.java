package de.fu_berlin.inf.dpp.test.fakes.net;

import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FakeConnectionFactory {

  public static class FakeConnectionFactoryResult {

    private Map<JID, ITransmitter> transmitters;

    private Map<JID, IReceiver> receivers;

    private FakeConnectionFactoryResult(
        Map<JID, ITransmitter> transmitters, Map<JID, IReceiver> receivers) {
      this.transmitters = transmitters;
      this.receivers = receivers;
    }

    public ITransmitter getTransmitter(JID jid) {
      return transmitters.get(jid);
    }

    public IReceiver getReceiver(JID jid) {
      return receivers.get(jid);
    }
  }

  public static FakeConnectionFactory createConnections(JID... jids) {
    return new FakeConnectionFactory(jids);
  }

  private boolean useThreadedReceiver;
  private boolean strict;

  private Set<JID> jids;

  private FakeConnectionFactory(JID... jids) {
    this.jids = new HashSet<JID>(Arrays.asList(jids));
  }

  public FakeConnectionFactory withThreadedReceiver() {
    useThreadedReceiver = true;
    return this;
  }

  public FakeConnectionFactory withStrictJIDLookup() {
    strict = true;
    return this;
  }

  public FakeConnectionFactoryResult get() {

    Map<JID, ITransmitter> transmitters = new HashMap<JID, ITransmitter>();
    Map<JID, IReceiver> receivers = new HashMap<JID, IReceiver>();

    for (JID jid : jids) {
      receivers.put(jid, useThreadedReceiver ? new ThreadedReceiver() : new NonThreadedReceiver());
    }

    JID[] currentJIDs = jids.toArray(new JID[0]);

    // filter out the receiver for the local JID
    for (JID localJID : currentJIDs) {
      Map<JID, IReceiver> remoteReceivers = new HashMap<JID, IReceiver>();

      for (JID currentJID : currentJIDs) {
        if (localJID.equals(currentJID)) continue;

        remoteReceivers.put(currentJID, receivers.get(currentJID));
      }

      transmitters.put(localJID, new FakePacketTransmitter(localJID, remoteReceivers, strict));
    }

    return new FakeConnectionFactoryResult(transmitters, receivers);
  }
}
