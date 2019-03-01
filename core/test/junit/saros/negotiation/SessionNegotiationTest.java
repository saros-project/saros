package saros.negotiation;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.fail;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import saros.communication.extensions.InvitationAcknowledgedExtension;
import saros.communication.extensions.InvitationOfferingExtension;
import saros.monitoring.NullProgressMonitor;
import saros.negotiation.Negotiation.Status;
import saros.negotiation.hooks.SessionNegotiationHookManager;
import saros.net.IConnectionManager;
import saros.net.ITransmitter;
import saros.net.PacketCollector;
import saros.net.xmpp.JID;
import saros.net.xmpp.discovery.DiscoveryManager;
import saros.preferences.IPreferenceStore;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.User;
import saros.test.fakes.net.FakeConnectionFactory;
import saros.test.fakes.net.FakeConnectionFactory.FakeConnectionFactoryResult;
import saros.test.fakes.net.ThreadedReceiver;
import saros.versioning.VersionManager;

public class SessionNegotiationTest {

  /*
   * the value must be higher than any real timeout, otherwise we would miss
   * exceptions
   */
  private static int TIMEOUT_IN_SECONDS = 180;

  private static final JID ALICE = new JID("alice@test/Saros");
  private static final JID BOB = new JID("bob@test/Saros");

  private static final User HOST = new User(ALICE, true, true, 0, 0);
  private static final User INVITEE = new User(BOB, true, true, 0, 0);

  private volatile Thread aliceNegotiationThread;
  private volatile Thread bobNegotiationThread;

  private ThreadedReceiver aliceReceiver;
  private ThreadedReceiver bobReceiver;

  private ITransmitter aliceTransmitter;
  private ITransmitter bobTransmitter;

  private VersionManager aliceVersionManager;

  @SuppressWarnings("unused")
  /*
   * needs to be present as it is indirectly accessed through fake network I/O
   */
  private VersionManager bobVersionManager;

  private IConnectionManager aliceConnectionManager;
  private IConnectionManager bobConnectionManager;

  private DiscoveryManager aliceDiscoveryManager;

  private ISarosSession aliceSession;
  private ISarosSession bobSession;

  private ISarosSessionManager aliceSessionManager;
  private ISarosSessionManager bobSessionManager;

  @Before
  public void setup() throws Exception {
    FakeConnectionFactoryResult result =
        FakeConnectionFactory.createConnections(ALICE, BOB)
            .withStrictJIDLookup()
            .withThreadedReceiver()
            .get();

    aliceReceiver = (ThreadedReceiver) result.getReceiver(ALICE);
    bobReceiver = (ThreadedReceiver) result.getReceiver(BOB);

    aliceTransmitter = result.getTransmitter(ALICE);
    bobTransmitter = result.getTransmitter(BOB);

    /*
     * we cut off at the highest layer of the network so there is currently
     * no use for this
     */
    aliceConnectionManager = createNiceMock(IConnectionManager.class);
    bobConnectionManager = createNiceMock(IConnectionManager.class);

    replay(aliceConnectionManager, bobConnectionManager);

    aliceVersionManager = new VersionManager("47.11.8015.TEST", aliceReceiver, aliceTransmitter);

    bobVersionManager = new VersionManager("47.11.8015.TEST", bobReceiver, bobTransmitter);

    aliceDiscoveryManager = discoveryManagerMockFor(BOB);

    aliceSession = createNiceMock(ISarosSession.class);

    aliceSession.addUser(eq(INVITEE), anyObject(IPreferenceStore.class));
    expectLastCall().once();

    bobSession = createNiceMock(ISarosSession.class);

    expect(aliceSession.getHost()).andStubReturn(HOST);

    replay(aliceSession, bobSession);

    aliceSessionManager = createNiceMock(ISarosSessionManager.class);

    bobSessionManager = createNiceMock(ISarosSessionManager.class);

    expect(
            bobSessionManager.joinSession(
                anyObject(String.class),
                eq(ALICE),
                anyObject(IPreferenceStore.class),
                anyObject(IPreferenceStore.class)))
        .andReturn(bobSession)
        .once();

    replay(aliceSessionManager, bobSessionManager);
  }

  private DiscoveryManager discoveryManagerMockFor(final JID jid) {

    final DiscoveryManager discoManagerMock = createMock(DiscoveryManager.class);

    expect(discoManagerMock.getSupportingPresence(eq(jid), anyObject(String.class)))
        .andStubReturn(jid);
    replay(discoManagerMock);

    return discoManagerMock;
  }

  @After
  public void teardown() {
    if (aliceReceiver != null) aliceReceiver.stop();

    if (bobReceiver != null) bobReceiver.stop();
  }

  @Test
  public void testGenericSessionNegotiation() throws Throwable {

    final OutgoingSessionNegotiation aliceOut =
        new OutgoingSessionNegotiation(
            BOB,
            "",
            aliceSessionManager,
            aliceSession,
            new SessionNegotiationHookManager(),
            aliceVersionManager,
            aliceDiscoveryManager,
            aliceTransmitter,
            aliceReceiver);

    final AtomicReference<Throwable> aliceThrowable = new AtomicReference<Throwable>();
    final AtomicReference<Throwable> bobThrowable = new AtomicReference<Throwable>();

    aliceNegotiationThread =
        new Thread(
            new Runnable() {

              @Override
              public void run() {
                try {
                  Status status = aliceOut.start(new NullProgressMonitor());

                  if (status != Status.OK)
                    throw new AssertionError(
                        "OSN failed: " + status + ", " + aliceOut.getErrorMessage());

                } catch (Throwable t) {
                  aliceThrowable.set(t);
                }
              }
            });

    final CountDownLatch bobNegotiationStart = new CountDownLatch(1);

    /*
     * The logic here is normally done by the SessionManager and
     * NegotiationPackerListener. As stubs cannot be used here (because this
     * should still be a unit test rather than an integration test),
     * re-implement it here (as easy/dirty as possible).
     */
    final Thread negotiationAwaitThread =
        new Thread(
            new Runnable() {

              @Override
              public void run() {
                PacketCollector collector =
                    bobReceiver.createCollector(
                        InvitationOfferingExtension.PROVIDER.getPacketFilter());

                bobNegotiationStart.countDown();
                Packet packet = collector.nextResult(30000);

                final InvitationOfferingExtension payload =
                    InvitationOfferingExtension.PROVIDER.getPayload(packet);

                final String sessionID = payload.getSessionID();
                final String negotiationID = payload.getNegotiationID();
                final String description = payload.getDescription();

                final IncomingSessionNegotiation bobIn =
                    new IncomingSessionNegotiation(
                        ALICE,
                        negotiationID,
                        sessionID,
                        "version not used by the negotiation itself",
                        description,
                        bobSessionManager,
                        new SessionNegotiationHookManager(),
                        bobConnectionManager,
                        bobTransmitter,
                        bobReceiver);

                bobNegotiationThread =
                    new Thread(
                        new Runnable() {
                          @Override
                          public void run() {
                            try {
                              PacketExtension response =
                                  InvitationAcknowledgedExtension.PROVIDER.create(
                                      new InvitationAcknowledgedExtension(negotiationID));

                              bobTransmitter.sendPacketExtension(ALICE, response);

                              Status status = bobIn.accept(new NullProgressMonitor());

                              if (status != Status.OK)
                                throw new AssertionError(
                                    "ISN failed: " + status + ", " + bobIn.getErrorMessage());

                            } catch (Throwable t) {
                              bobThrowable.set(t);
                            }
                          }
                        });

                bobNegotiationThread.start();
              }
            });

    negotiationAwaitThread.start();

    bobNegotiationStart.await(30000, TimeUnit.MILLISECONDS);

    aliceNegotiationThread.start();

    aliceNegotiationThread.join(TIMEOUT_IN_SECONDS * 10000);

    final Throwable aliceError = aliceThrowable.get();

    if (aliceError != null) throw aliceError;

    if (bobNegotiationThread == null) fail("bobs negotiation thread did not start");

    bobNegotiationThread.join(TIMEOUT_IN_SECONDS * 10000);

    /*
     * TODO unlikely to see an error here because the OSN will fail first in
     * such circumstances
     */
    final Throwable bobError = bobThrowable.get();

    if (bobError != null) throw bobError;

    verify(aliceSession, bobSession, bobSessionManager);
  }
}
