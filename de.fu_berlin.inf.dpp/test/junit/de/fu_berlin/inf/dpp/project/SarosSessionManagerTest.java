package de.fu_berlin.inf.dpp.project;

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

import de.fu_berlin.inf.dpp.ISarosContext;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import de.fu_berlin.inf.dpp.observables.ProjectNegotiationObservable;
import de.fu_berlin.inf.dpp.observables.SarosSessionObservable;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.observables.SessionNegotiationObservable;
import de.fu_berlin.inf.dpp.preferences.Preferences;
import de.fu_berlin.inf.dpp.project.internal.SarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionListener;
import de.fu_berlin.inf.dpp.session.NullSarosSessionListener;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ SarosSession.class, SarosSessionManager.class })
public class SarosSessionManagerTest {

    private class DummyError extends Error {
        private static final long serialVersionUID = 1L;
    }

    private class StateVerifyListener extends NullSarosSessionListener {
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
        public void sessionEnded(ISarosSession oldSarosSession) {
            checkAndSetState(2, -1);
        }

        private void checkAndSetState(int expectedState, int newState) {
            assertEquals("listener methods invoked in wrong order", state,
                expectedState);
            state = newState;
        }
    }

    private class ErrorThrowingListener extends NullSarosSessionListener {

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
        public void sessionEnded(ISarosSession oldSarosSession) {
            throw new DummyError();
        }
    }

    private SarosSessionManager manager;

    @Before
    public void setUp() throws Exception {
        SarosSession session = PowerMock.createNiceMock(SarosSession.class);
        XMPPConnectionService network = PowerMock
            .createNiceMock(XMPPConnectionService.class);

        Preferences preferences = PowerMock.createNiceMock(Preferences.class);

        PowerMock.expectNew(SarosSession.class, EasyMock.isNull(String.class),
            EasyMock.anyInt(), EasyMock.anyObject(ISarosContext.class))
            .andStubReturn(session);

        PowerMock.replayAll();

        manager = new SarosSessionManager(network,
            new SarosSessionObservable(), new SessionIDObservable(),
            new SessionNegotiationObservable(),
            new ProjectNegotiationObservable(), preferences);
    }

    @Test
    public void testStartStopListenerCallback() {
        manager.addSarosSessionListener(new StateVerifyListener());
        manager.startSession(new HashMap<IProject, List<IResource>>());
        manager.stopSarosSession();
    }

    @Test
    public void testMultipleStarts() {
        manager.addSarosSessionListener(new StateVerifyListener());
        manager.startSession(new HashMap<IProject, List<IResource>>());
        manager.startSession(new HashMap<IProject, List<IResource>>());
        manager.stopSarosSession();
    }

    @Test
    public void testMultipleStops() {
        manager.addSarosSessionListener(new StateVerifyListener());
        manager.startSession(new HashMap<IProject, List<IResource>>());
        manager.stopSarosSession();
        manager.stopSarosSession();
    }

    @Test(expected = DummyError.class)
    public void testListenerDispatchIsNotCatchingErrors() {
        manager.addSarosSessionListener(new ErrorThrowingListener());
        manager.startSession(new HashMap<IProject, List<IResource>>());
        manager.stopSarosSession();
    }

    @Test
    public void testRecursiveStop() {
        ISarosSessionListener listener = new NullSarosSessionListener() {
            int count = 0;

            @Override
            public void sessionEnding(ISarosSession oldSarosSession) {
                assertTrue("stopSession is executed recusive", count == 0);
                count++;
                manager.stopSarosSession();
            }

        };
        manager.addSarosSessionListener(listener);
        manager.startSession(new HashMap<IProject, List<IResource>>());
        manager.stopSarosSession();
    }

    @Test
    public void testRecursiveStart() {
        ISarosSessionListener listener = new NullSarosSessionListener() {
            int count = 0;

            @Override
            public void sessionStarting(ISarosSession oldSarosSession) {
                assertTrue("startSession is executed recusive", count == 0);
                count++;
                manager.startSession(new HashMap<IProject, List<IResource>>());
            }

        };
        manager.addSarosSessionListener(listener);
        manager.startSession(new HashMap<IProject, List<IResource>>());
    }

    @Test(expected = IllegalStateException.class)
    public void stopWhileStarting() {

        final AtomicReference<RuntimeException> exception = new AtomicReference<RuntimeException>();

        ISarosSessionListener listener = new NullSarosSessionListener() {
            @Override
            public void sessionStarting(ISarosSession oldSarosSession) {
                try {
                    manager.stopSarosSession();
                } catch (RuntimeException e) {
                    exception.set(e);
                }
            }

        };
        manager.addSarosSessionListener(listener);
        manager.startSession(new HashMap<IProject, List<IResource>>());

        RuntimeException rte = exception.get();

        if (rte != null)
            throw rte;
    }

    @Test(expected = IllegalStateException.class)
    public void startWhileStopping() {

        final AtomicReference<RuntimeException> exception = new AtomicReference<RuntimeException>();

        ISarosSessionListener listener = new NullSarosSessionListener() {
            @Override
            public void sessionEnding(ISarosSession oldSarosSession) {
                try {
                    manager
                        .startSession(new HashMap<IProject, List<IResource>>());
                } catch (RuntimeException e) {
                    exception.set(e);
                }
            }

        };
        manager.addSarosSessionListener(listener);
        manager.startSession(new HashMap<IProject, List<IResource>>());
        manager.stopSarosSession();

        RuntimeException rte = exception.get();

        if (rte != null)
            throw rte;
    }
}
