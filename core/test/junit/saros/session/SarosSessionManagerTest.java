package saros.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import saros.context.IContainerContext;
import saros.filesystem.IProject;
import saros.filesystem.IResource;
import saros.net.IReceiver;
import saros.net.ITransmitter;
import saros.net.xmpp.XMPPConnectionService;
import saros.preferences.IPreferenceStore;
import saros.session.internal.SarosSession;

@RunWith(PowerMockRunner.class)
@PrepareForTest({SarosSession.class, SarosSessionManager.class})
public class SarosSessionManagerTest {

  private class DummyError extends Error {
    private static final long serialVersionUID = 1L;
  }

  private class StateVerifyListener implements ISessionLifecycleListener {
    int state = -1;

    @Override
    public void sessionStarting(ISarosSession newSarosSession) {
      checkAndSetState(-1, 0);
    }

    @Override
    public void sessionStarted(ISarosSession newSarosSession) {
      checkAndSetState(0, 1);
    }

    @Override
    public void sessionEnding(ISarosSession oldSarosSession) {
      checkAndSetState(1, 2);
    }

    @Override
    public void sessionEnded(ISarosSession oldSarosSession, SessionEndReason reason) {
      checkAndSetState(2, -1);
    }

    private void checkAndSetState(int expectedState, int newState) {
      assertEquals("listener methods invoked in wrong order", state, expectedState);
      state = newState;
    }
  }

  private class ErrorThrowingListener implements ISessionLifecycleListener {

    @Override
    public void sessionStarting(ISarosSession newSarosSession) {
      throw new DummyError();
    }

    @Override
    public void sessionStarted(ISarosSession newSarosSession) {
      throw new DummyError();
    }

    @Override
    public void sessionEnding(ISarosSession oldSarosSession) {
      throw new DummyError();
    }

    @Override
    public void sessionEnded(ISarosSession oldSarosSession, SessionEndReason reason) {
      throw new DummyError();
    }
  }

  private SarosSessionManager manager;

  @Before
  public void setUp() throws Exception {
    SarosSession session = PowerMock.createNiceMock(SarosSession.class);

    XMPPConnectionService network = PowerMock.createNiceMock(XMPPConnectionService.class);

    ITransmitter transmitter = PowerMock.createNiceMock(ITransmitter.class);
    IReceiver receiver = PowerMock.createNiceMock(IReceiver.class);

    IContainerContext context = PowerMock.createNiceMock(IContainerContext.class);

    PowerMock.expectNew(
            SarosSession.class,
            EasyMock.anyObject(String.class),
            EasyMock.anyObject(IPreferenceStore.class),
            EasyMock.anyObject(IContainerContext.class))
        .andStubReturn(session);

    PowerMock.replayAll();

    manager = new SarosSessionManager(context, null, null, network, transmitter, receiver);
  }

  @Test
  public void testStartStopListenerCallback() {
    manager.addSessionLifecycleListener(new StateVerifyListener());
    manager.startSession(new HashMap<IProject, List<IResource>>());
    manager.stopSession(SessionEndReason.LOCAL_USER_LEFT);
  }

  @Test
  public void testMultipleStarts() {
    manager.addSessionLifecycleListener(new StateVerifyListener());
    manager.startSession(new HashMap<IProject, List<IResource>>());
    manager.startSession(new HashMap<IProject, List<IResource>>());
    manager.stopSession(SessionEndReason.LOCAL_USER_LEFT);
  }

  @Test
  public void testMultipleStops() {
    manager.addSessionLifecycleListener(new StateVerifyListener());
    manager.startSession(new HashMap<IProject, List<IResource>>());
    manager.stopSession(SessionEndReason.LOCAL_USER_LEFT);
    manager.stopSession(SessionEndReason.LOCAL_USER_LEFT);
  }

  @Test(expected = DummyError.class)
  public void testListenerDispatchIsNotCatchingErrors() {
    manager.addSessionLifecycleListener(new ErrorThrowingListener());
    manager.startSession(new HashMap<IProject, List<IResource>>());
    manager.stopSession(SessionEndReason.LOCAL_USER_LEFT);
  }

  @Test
  public void testRecursiveStop() {
    ISessionLifecycleListener listener =
        new ISessionLifecycleListener() {
          int count = 0;

          @Override
          public void sessionEnding(ISarosSession oldSarosSession) {
            assertTrue("stopSession is executed recusive", count == 0);
            count++;
            manager.stopSession(SessionEndReason.LOCAL_USER_LEFT);
          }
        };
    manager.addSessionLifecycleListener(listener);
    manager.startSession(new HashMap<IProject, List<IResource>>());
    manager.stopSession(SessionEndReason.LOCAL_USER_LEFT);
  }

  @Test
  public void testRecursiveStart() {
    ISessionLifecycleListener listener =
        new ISessionLifecycleListener() {
          int count = 0;

          @Override
          public void sessionStarting(ISarosSession oldSarosSession) {
            assertTrue("startSession is executed recusive", count == 0);
            count++;
            manager.startSession(new HashMap<IProject, List<IResource>>());
          }
        };
    manager.addSessionLifecycleListener(listener);
    manager.startSession(new HashMap<IProject, List<IResource>>());
  }

  @Test(expected = IllegalStateException.class)
  public void stopWhileStarting() {

    final AtomicReference<RuntimeException> exception = new AtomicReference<RuntimeException>();

    ISessionLifecycleListener listener =
        new ISessionLifecycleListener() {
          @Override
          public void sessionStarting(ISarosSession oldSarosSession) {
            try {
              manager.stopSession(SessionEndReason.LOCAL_USER_LEFT);
            } catch (RuntimeException e) {
              exception.set(e);
            }
          }
        };
    manager.addSessionLifecycleListener(listener);
    manager.startSession(new HashMap<IProject, List<IResource>>());

    RuntimeException rte = exception.get();

    if (rte != null) throw rte;
  }

  @Test(expected = IllegalStateException.class)
  public void startWhileStopping() {

    final AtomicReference<RuntimeException> exception = new AtomicReference<RuntimeException>();

    ISessionLifecycleListener listener =
        new ISessionLifecycleListener() {
          @Override
          public void sessionEnding(ISarosSession oldSarosSession) {
            try {
              manager.startSession(new HashMap<IProject, List<IResource>>());
            } catch (RuntimeException e) {
              exception.set(e);
            }
          }
        };
    manager.addSessionLifecycleListener(listener);
    manager.startSession(new HashMap<IProject, List<IResource>>());
    manager.stopSession(SessionEndReason.LOCAL_USER_LEFT);

    RuntimeException rte = exception.get();

    if (rte != null) throw rte;
  }
}
