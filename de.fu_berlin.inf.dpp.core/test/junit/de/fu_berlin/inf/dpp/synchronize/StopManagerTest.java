package de.fu_berlin.inf.dpp.synchronize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CancellationException;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.StopActivity;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.session.IActivityListener;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;

public class StopManagerTest {

    private ISarosSession alicesSession;
    private User alicesAlice;
    private User alicesBob;
    private User alicesCarl;

    private ISarosSession bobsSession;
    private User bobsAlice;
    private User bobsBob;
    private User bobsCarl;

    private ISarosSession carlsSession;
    private User carlsAlice;
    private User carlsBob;
    private User carlsCarl;

    @Before
    public void createSessionMocks() {
        alicesSession = EasyMock.createMock(ISarosSession.class);
        alicesSession.addActivityProvider(EasyMock.isA(StopManager.class));
        alicesSession.removeActivityProvider(EasyMock.isA(StopManager.class));

        alicesAlice = new User(new JID("alice"), true, true, 1, -1);
        alicesBob = new User(new JID("bob"), false, false, 2, -1);
        alicesCarl = new User(new JID("carl"), false, false, 3, -1);

        alicesAlice.setInSession(true);
        alicesBob.setInSession(true);
        alicesCarl.setInSession(true);

        alicesSession.getLocalUser();
        EasyMock.expectLastCall().andStubReturn(alicesAlice);

        alicesSession.getUser(alicesBob.getJID());
        EasyMock.expectLastCall().andStubReturn(alicesBob);

        alicesSession.getUser(alicesAlice.getJID());
        EasyMock.expectLastCall().andStubReturn(alicesAlice);

        alicesSession.getUser(alicesCarl.getJID());
        EasyMock.expectLastCall().andStubReturn(alicesCarl);

        EasyMock.replay(alicesSession);

        bobsSession = EasyMock.createMock(ISarosSession.class);
        bobsSession.addActivityProvider(EasyMock.isA(StopManager.class));
        bobsSession.removeActivityProvider(EasyMock.isA(StopManager.class));

        bobsAlice = new User(new JID("alice"), true, false, 1, -1);
        bobsBob = new User(new JID("bob"), false, true, 2, -1);
        bobsCarl = new User(new JID("carl"), false, false, 3, -1);

        bobsAlice.setInSession(true);
        bobsBob.setInSession(true);
        bobsCarl.setInSession(true);

        bobsSession.getLocalUser();
        EasyMock.expectLastCall().andStubReturn(bobsBob);

        bobsSession.getUser(bobsAlice.getJID());
        EasyMock.expectLastCall().andStubReturn(bobsAlice);

        bobsSession.getUser(bobsBob.getJID());
        EasyMock.expectLastCall().andStubReturn(bobsBob);

        bobsSession.getUser(bobsCarl.getJID());
        EasyMock.expectLastCall().andStubReturn(bobsCarl);
        EasyMock.replay(bobsSession);

        carlsSession = EasyMock.createMock(ISarosSession.class);
        carlsSession.addActivityProvider(EasyMock.isA(StopManager.class));
        carlsSession.removeActivityProvider(EasyMock.isA(StopManager.class));

        carlsAlice = new User(new JID("alice"), true, false, 1, -1);
        carlsBob = new User(new JID("bob"), false, false, 2, -1);
        carlsCarl = new User(new JID("carl"), false, true, 3, -1);

        carlsAlice.setInSession(true);
        carlsBob.setInSession(true);
        carlsCarl.setInSession(true);

        carlsSession.getLocalUser();
        EasyMock.expectLastCall().andStubReturn(carlsCarl);

        carlsSession.getUser(carlsAlice.getJID());
        EasyMock.expectLastCall().andStubReturn(carlsAlice);

        carlsSession.getUser(carlsBob.getJID());
        EasyMock.expectLastCall().andStubReturn(carlsBob);

        carlsSession.getUser(carlsCarl.getJID());
        EasyMock.expectLastCall().andStubReturn(carlsCarl);
        EasyMock.replay(carlsSession);
    }

    /**
     * Verify that a StopManager can be created and registers with the
     * ISarosSession
     */
    @Test
    public void testCreation() {
        StopManager stopManager = new StopManager(alicesSession);
        stopManager.start();
        stopManager.stop();
        EasyMock.verify(alicesSession);
    }

    /**
     * Verify that canceling directly after sending out the activity does work.
     * 
     * @throws InterruptedException
     * @throws CancellationException
     */
    @Test
    public void testActivityCreationAndCancelation()
        throws CancellationException, InterruptedException {

        IActivityListener listener = EasyMock
            .createMock(IActivityListener.class);
        listener.activityCreated(EasyMock.isA(StopActivity.class));
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {

            @Override
            public Object answer() throws Throwable {
                StopActivity activity = (StopActivity) EasyMock
                    .getCurrentArguments()[0];
                Assert.assertEquals(alicesBob, activity.getAffected());
                Assert.assertEquals(alicesAlice, activity.getSource());
                Assert.assertEquals(alicesAlice, activity.getInitiator());
                Assert.assertEquals(StopActivity.Type.LOCKREQUEST,
                    activity.getType());
                Assert.assertEquals(StopActivity.State.INITIATED,
                    activity.getState());
                alicesBob.setInSession(false);
                return null;
            }
        }).once();

        listener.activityCreated(EasyMock.isA(StopActivity.class));
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {

            @Override
            public Object answer() throws Throwable {
                StopActivity activity = (StopActivity) EasyMock
                    .getCurrentArguments()[0];
                Assert.assertEquals(alicesBob, activity.getAffected());
                Assert.assertEquals(alicesAlice, activity.getSource());
                Assert.assertEquals(alicesAlice, activity.getInitiator());
                Assert.assertEquals(StopActivity.Type.UNLOCKREQUEST,
                    activity.getType());
                Assert.assertEquals(StopActivity.State.INITIATED,
                    activity.getState());
                return null;
            }
        }).once();

        EasyMock.replay(listener);

        StopManager manager = new StopManager(alicesSession);
        manager.start();
        manager.addActivityListener(listener);

        try {
            manager.stop(alicesBob, "test");
            Assert.fail("Failed to cancel");
        } catch (CancellationException e) {
            // expected
        } catch (InterruptedException e) {
            Assert.fail("Should not happen");
        }

        EasyMock.verify(listener);
        manager.stop();
        EasyMock.verify(alicesSession);
    }

    /**
     * Verify that local locking does not generate an activity.
     * 
     * @throws InterruptedException
     * @throws CancellationException
     */
    @Test
    public void testLocalCancel() throws CancellationException,
        InterruptedException {
        IActivityListener listener = EasyMock
            .createMock(IActivityListener.class);
        EasyMock.replay(listener);

        // create
        StopManager manager = new StopManager(alicesSession);
        manager.start();
        manager.addActivityListener(listener);
        assertFalse(manager.getBlockedObservable().getValue());

        // block/pause the local user
        StartHandle handle = manager.stop(alicesAlice, "block myself");
        assertTrue(manager.getBlockedObservable().getValue());

        // unblock
        handle.start();
        assertFalse(manager.getBlockedObservable().getValue());

        // verify that nothing has been called on the activity listener
        EasyMock.verify(listener);
        manager.stop();
        EasyMock.verify(alicesSession);
    }

    /**
     * Test two StopManagers talking to each other.
     * 
     * @throws InterruptedException
     * @throws CancellationException
     */
    @Test
    public void testStopStart() throws CancellationException,
        InterruptedException {
        final StopManager alicesStopManager = new StopManager(alicesSession);
        alicesStopManager.start();
        final StopManager bobsStopManager = new StopManager(bobsSession);
        bobsStopManager.start();

        // Now make both listeners data to each other
        IActivityListener alicesListener = createForwarder(bobsSession,
            bobsStopManager);

        alicesStopManager.addActivityListener(alicesListener);

        IActivityListener bobsListener = createForwarder(alicesSession,
            alicesStopManager);

        bobsStopManager.addActivityListener(bobsListener);

        // verify that everything is unlocked
        assertFalse(alicesStopManager.getBlockedObservable().getValue());
        assertFalse(bobsStopManager.getBlockedObservable().getValue());

        // now start to lock
        StartHandle handle = alicesStopManager.stop(alicesBob, "test");
        assertFalse(alicesStopManager.getBlockedObservable().getValue());
        assertTrue(bobsStopManager.getBlockedObservable().getValue());

        // now unlock
        handle.start();
        assertFalse(alicesStopManager.getBlockedObservable().getValue());
        assertFalse(bobsStopManager.getBlockedObservable().getValue());

        alicesStopManager.stop();
        EasyMock.verify(alicesSession);
        bobsStopManager.stop();
        EasyMock.verify(bobsSession);
    }

    @Test
    public void testStopMultipleUsers() throws CancellationException {
        final StopManager alicesStopManager = new StopManager(alicesSession);
        final StopManager bobsStopManager = new StopManager(bobsSession);
        final StopManager carlsStopManager = new StopManager(carlsSession);
        alicesStopManager.start();
        bobsStopManager.start();
        carlsStopManager.start();

        // Now make both listeners data to each other
        IActivityListener alicesListener = createForwarder(bobsStopManager,
            carlsStopManager);
        alicesStopManager.addActivityListener(alicesListener);
        IActivityListener bobsListener = createForwarder(alicesSession,
            alicesStopManager);
        bobsStopManager.addActivityListener(bobsListener);
        IActivityListener carlsListener = createForwarder(alicesSession,
            alicesStopManager);
        carlsStopManager.addActivityListener(carlsListener);

        // verify that everything is unlocked
        assertFalse(alicesStopManager.getBlockedObservable().getValue());
        assertFalse(bobsStopManager.getBlockedObservable().getValue());
        assertFalse(carlsStopManager.getBlockedObservable().getValue());

        // now start to lock
        List<User> users = new LinkedList<User>();
        users.add(alicesBob);
        users.add(alicesCarl);
        List<StartHandle> handles = alicesStopManager.stop(users, "test");
        assertEquals(2, handles.size());
        assertFalse(alicesStopManager.getBlockedObservable().getValue());
        assertTrue(bobsStopManager.getBlockedObservable().getValue());
        assertTrue(carlsStopManager.getBlockedObservable().getValue());

        // now unlock
        for (StartHandle handle : handles) {
            handle.start();
            boolean res = handle.await();
            assertTrue(res);
        }

        assertFalse(alicesStopManager.getBlockedObservable().getValue());
        assertFalse(bobsStopManager.getBlockedObservable().getValue());
        assertFalse(carlsStopManager.getBlockedObservable().getValue());

        alicesStopManager.stop();
        bobsStopManager.stop();
        carlsStopManager.stop();

        EasyMock.verify(alicesSession);
        EasyMock.verify(bobsSession);
        EasyMock.verify(carlsSession);
    }

    /**
     * This tests what happens when a user is leaving a session during the
     * stop/pause process is on.
     */
    @Test(timeout = 30000)
    public void testLeaveSession() {

        // Create a special session that allows us to kick bob out of the
        // session and test the cancellation for that case.
        alicesSession = EasyMock.createMock(ISarosSession.class);
        alicesSession.addActivityProvider(EasyMock.isA(StopManager.class));
        alicesSession.removeActivityProvider(EasyMock.isA(StopManager.class));

        alicesSession.getLocalUser();
        EasyMock.expectLastCall().andReturn(alicesAlice).anyTimes();

        alicesSession.getUser(alicesAlice.getJID());
        EasyMock.expectLastCall().andReturn(alicesAlice).anyTimes();

        alicesSession.getUser(alicesCarl.getJID());
        EasyMock.expectLastCall().andReturn(alicesCarl).anyTimes();

        alicesSession.getUser(alicesBob.getJID());
        EasyMock.expectLastCall().andReturn(alicesBob).anyTimes();

        EasyMock.replay(alicesSession);

        final StopManager alicesStopManager = new StopManager(alicesSession);
        final StopManager bobsStopManager = new StopManager(bobsSession);
        final StopManager carlsStopManager = new StopManager(carlsSession);

        alicesStopManager.start();
        bobsStopManager.start();
        carlsStopManager.start();

        // Now make both listeners data to each other
        IActivityListener alicesListener = createForwarder(bobsStopManager,
            carlsStopManager);

        alicesStopManager.addActivityListener(alicesListener);

        IActivityListener bobsListener = EasyMock
            .createMock(IActivityListener.class);
        bobsListener.activityCreated(EasyMock.isA(StopActivity.class));
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {

            @Override
            public Object answer() throws Throwable {
                System.err.println("received lock request");
                alicesBob.setInSession(false);
                return null;
            }
        }).anyTimes();

        EasyMock.replay(bobsListener);

        bobsStopManager.addActivityListener(bobsListener);

        IActivityListener carlsListener = createForwarder(alicesSession,
            alicesStopManager);

        carlsStopManager.addActivityListener(carlsListener);

        // verify that everything is unlocked
        assertFalse(alicesStopManager.getBlockedObservable().getValue());
        assertFalse(bobsStopManager.getBlockedObservable().getValue());
        assertFalse(carlsStopManager.getBlockedObservable().getValue());

        // now start to lock
        List<User> users = new LinkedList<User>();
        users.add(alicesBob);
        users.add(alicesCarl);

        try {
            alicesStopManager.stop(users, "test");
            Assert.fail("Should not be reached");
        } catch (CancellationException e) {
            //
        }

        // Only test alice and carl as bob is not part of the session anymore.
        assertFalse(alicesStopManager.getBlockedObservable().getValue());
        assertFalse(carlsStopManager.getBlockedObservable().getValue());

        alicesStopManager.stop();
        bobsStopManager.stop();
        carlsStopManager.stop();

        EasyMock.verify(alicesSession);
        EasyMock.verify(bobsSession);
        EasyMock.verify(carlsSession);
    }

    /**
     * This tests that after a StopManager has sent a request and the user is
     * leaving the session and we will not get a reply from it.
     */
    @Test(timeout = 30000)
    public void testNoReplyAndLeave() {

        // Observe the activities created and remember a lock request.
        IActivityListener listener = EasyMock
            .createMock(IActivityListener.class);

        listener.activityCreated(EasyMock.isA(StopActivity.class));
        EasyMock.expectLastCall().andStubAnswer(new IAnswer<Object>() {

            @Override
            public Object answer() throws Throwable {
                StopActivity stop = (StopActivity) EasyMock
                    .getCurrentArguments()[0];

                if (stop.getType() == StopActivity.Type.LOCKREQUEST)
                    alicesBob.setInSession(false);

                return null;
            }
        });

        EasyMock.replay(listener);

        StopManager stopManager = new StopManager(alicesSession);
        stopManager.addActivityListener(listener);
        stopManager.start();

        try {
            stopManager.stop(Collections.singletonList(alicesBob), "test");

            // Assert.fail("Could not stop."); will block forever see timeout;
        } catch (CancellationException e) {
            // What we wanted.
        }

        stopManager.stop();

        EasyMock.verify(listener);
        EasyMock.verify(alicesSession);
    }

    /**
     * This tests that starting of a handle might fail.
     */
    @Test(timeout = 30000)
    public void testLockAndUserLeaveOnResume() {

        final StopManager alicesStopManager = new StopManager(alicesSession);
        final StopManager bobsStopManager = new StopManager(bobsSession);

        // Observe the activities created and remember a lock request.
        IActivityListener alicesListener = EasyMock
            .createMock(IActivityListener.class);

        alicesListener.activityCreated(EasyMock.isA(StopActivity.class));
        EasyMock.expectLastCall().andStubAnswer(new IAnswer<Object>() {

            @Override
            public Object answer() throws Throwable {
                StopActivity stop = (StopActivity) EasyMock
                    .getCurrentArguments()[0];
                if (stop.getType() == StopActivity.Type.UNLOCKREQUEST)
                    alicesBob.setInSession(false);
                else
                    bobsStopManager.exec(rewriteStopActivity(stop));

                return null;
            }
        });

        EasyMock.replay(alicesListener);

        // Start the managers now
        alicesStopManager.addActivityListener(alicesListener);
        alicesStopManager.start();

        IActivityListener bobsListener = createForwarder(alicesSession,
            alicesStopManager);

        bobsStopManager.addActivityListener(bobsListener);
        bobsStopManager.start();

        List<StartHandle> handles = alicesStopManager.stop(
            Collections.singletonList(alicesBob), "test");

        Assert.assertEquals(1, handles.size());

        // Test that we resume
        for (StartHandle handle : handles) {
            boolean res = handle.startAndAwait();
            Assert.assertFalse(res);
        }

        alicesStopManager.stop();
        bobsStopManager.stop();

        EasyMock.verify(alicesListener);
        EasyMock.verify(alicesSession);
        EasyMock.verify(bobsListener);
        EasyMock.verify(bobsSession);
    }

    private static User rewriteUser(User user) {
        User copy = new User(user.getJID(), user.isHost(), !user.isLocal(),
            user.getColorID(), -1);
        copy.setInSession(true);
        return copy;
    }

    private static StopActivity rewriteStopActivity(StopActivity inActivity) {
        return new StopActivity(rewriteUser(inActivity.getAffected()),
            rewriteUser(inActivity.getInitiator()),
            rewriteUser(inActivity.getAffected()), inActivity.getType(),
            inActivity.getState(), inActivity.getActivityID());
    }

    /**
     * This method helps to forward messages from one session into the other.
     * 
     * @param targetSession
     * @param targetManager
     * @return
     */
    private static IActivityListener createForwarder(
        final ISarosSession targetSession, final StopManager targetManager) {
        IActivityListener listener = EasyMock
            .createMock(IActivityListener.class);
        listener.activityCreated(EasyMock.isA(StopActivity.class));
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {

            @Override
            public Object answer() throws Throwable {
                StopActivity inActivity = (StopActivity) EasyMock
                    .getCurrentArguments()[0];
                // TODO: This should be async but this method is ran from the
                // SWT thread and the StopManager tries to execute from the SWT
                // Thread.
                targetManager.exec(rewriteStopActivity(inActivity));
                return null;
            }
        }).anyTimes();
        EasyMock.replay(listener);
        return listener;
    }

    /**
     * This method helps to forward messages from one session into the other. It
     * is somehow special as it know which stop user to use depending on the
     * JID.
     * 
     */
    private IActivityListener createForwarder(final StopManager from,
        final StopManager carl) {

        /*
         * do not synchronize this listener or some test cases will end in a
         * deadlock ... this is mainly because we do not use a thread which
         * would be present in a real execution environment
         */
        return new IActivityListener() {
            @Override
            public void activityCreated(IActivity activityData) {
                if (!(activityData instanceof StopActivity))
                    return;

                StopActivity stop = (StopActivity) activityData;
                if (stop.getRecipient().getJID().equals(new JID("bob"))) {
                    from.exec(rewriteStopActivity(stop));
                } else if (stop.getRecipient().getJID().equals(new JID("carl"))) {
                    carl.exec(rewriteStopActivity(stop));
                } else
                    Assert.fail("Should not be reached");
            }
        };
    }
}
