package de.fu_berlin.inf.dpp.synchronize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CancellationException;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.business.StopActivity;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.IActivityListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;

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

        alicesAlice = new User(alicesSession, new JID("alice"), 1, -1);
        alicesBob = new User(alicesSession, new JID("bob"), 2, -1);
        alicesCarl = new User(alicesSession, new JID("carl"), 3, -1);
        alicesSession.getLocalUser();
        EasyMock.expectLastCall().andReturn(alicesAlice).anyTimes();
        alicesSession.getUser(alicesBob.getJID());
        EasyMock.expectLastCall().andReturn(alicesBob).anyTimes();
        alicesSession.getUser(alicesAlice.getJID());
        EasyMock.expectLastCall().andReturn(alicesAlice).anyTimes();
        alicesSession.getUser(alicesCarl.getJID());
        EasyMock.expectLastCall().andReturn(alicesCarl).anyTimes();

        EasyMock.replay(alicesSession);

        bobsSession = EasyMock.createMock(ISarosSession.class);
        bobsSession.addActivityProvider(EasyMock.isA(StopManager.class));
        bobsSession.removeActivityProvider(EasyMock.isA(StopManager.class));

        bobsAlice = new User(bobsSession, new JID("alice"), 1, -1);
        bobsBob = new User(bobsSession, new JID("bob"), 2, -1);
        bobsCarl = new User(bobsSession, new JID("carl"), 3, -1);
        bobsSession.getLocalUser();
        EasyMock.expectLastCall().andReturn(bobsBob).anyTimes();
        bobsSession.getUser(bobsAlice.getJID());
        EasyMock.expectLastCall().andReturn(bobsAlice).anyTimes();
        bobsSession.getUser(bobsBob.getJID());
        EasyMock.expectLastCall().andReturn(bobsBob).anyTimes();
        bobsSession.getUser(bobsCarl.getJID());
        EasyMock.expectLastCall().andReturn(bobsCarl).anyTimes();
        EasyMock.replay(bobsSession);

        carlsSession = EasyMock.createMock(ISarosSession.class);
        carlsSession.addActivityProvider(EasyMock.isA(StopManager.class));
        carlsSession.removeActivityProvider(EasyMock.isA(StopManager.class));

        carlsAlice = new User(carlsSession, new JID("alice"), 1, -1);
        carlsBob = new User(carlsSession, new JID("bob"), 2, -1);
        carlsCarl = new User(carlsSession, new JID("carl"), 3, -1);
        carlsSession.getLocalUser();
        EasyMock.expectLastCall().andReturn(carlsCarl).anyTimes();
        carlsSession.getUser(carlsAlice.getJID());
        EasyMock.expectLastCall().andReturn(carlsAlice).anyTimes();
        carlsSession.getUser(carlsBob.getJID());
        EasyMock.expectLastCall().andReturn(carlsBob).anyTimes();
        carlsSession.getUser(carlsCarl.getJID());
        EasyMock.expectLastCall().andReturn(carlsCarl).anyTimes();
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
     * Verify that a canceled a monitor throws the right exception.
     */
    @Test
    public void testEarlyCancel() {
        // Create a progress monitor and cancel it
        IProgressMonitor canceledProgress = new NullProgressMonitor();
        canceledProgress.setCanceled(true);
        StopManager stopManager = new StopManager(alicesSession);
        stopManager.start();
        assertFalse(stopManager.getBlockedObservable().getValue());

        try {
            stopManager.stop(alicesBob, "testStop", canceledProgress);
            fail("Should not be reached");
        } catch (CancellationException e) {
            // this is what we want to have
        } catch (InterruptedException e) {
            fail("Should not be reached.");
        }

        assertFalse(stopManager.getBlockedObservable().getValue());
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
        final NullProgressMonitor monitor = new NullProgressMonitor();
        IActivityListener listener = EasyMock
            .createMock(IActivityListener.class);
        listener.activityCreated(EasyMock.isA(StopActivity.class));
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {

            @Override
            public Object answer() throws Throwable {
                StopActivity activity = (StopActivity) EasyMock
                    .getCurrentArguments()[0];
                Assert.assertEquals(alicesBob, activity.getUser());
                Assert.assertEquals(alicesAlice, activity.getSource());
                Assert.assertEquals(alicesAlice, activity.getInitiator());
                Assert.assertEquals(StopActivity.Type.LOCKREQUEST,
                    activity.getType());
                Assert.assertEquals(StopActivity.State.INITIATED,
                    activity.getState());
                monitor.setCanceled(true);
                return null;
            }
        }).once();
        listener.activityCreated(EasyMock.isA(StopActivity.class));
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {

            @Override
            public Object answer() throws Throwable {
                StopActivity activity = (StopActivity) EasyMock
                    .getCurrentArguments()[0];
                Assert.assertEquals(alicesBob, activity.getUser());
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
            manager.stop(alicesBob, "test", monitor);
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
        StartHandle handle = manager.stop(alicesAlice, "block myself",
            new NullProgressMonitor());
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
        StartHandle handle = alicesStopManager.stop(alicesBob, "test",
            new NullProgressMonitor());
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
        List<StartHandle> handles = alicesStopManager.stop(users, "test",
            new NullProgressMonitor());
        assertEquals(2, handles.size());
        assertFalse(alicesStopManager.getBlockedObservable().getValue());
        assertTrue(bobsStopManager.getBlockedObservable().getValue());
        assertTrue(carlsStopManager.getBlockedObservable().getValue());

        // now unlock
        for (StartHandle handle : handles) {
            handle.start();
            boolean res = handle.await(new NullProgressMonitor());
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
     * This tests the cancellation after a user has been stopped.
     */
    @Test
    public void testCancelStopMultipleUser() {
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

        // prepare to cancel early
        final IProgressMonitor monitor = new NullProgressMonitor();
        bobsStopManager.addBlockable(new Blockable() {
            @Override
            public void unblock() {
                // nothing to do
            }

            @Override
            public void block() {
                monitor.setCanceled(true);
            }
        });

        // now start to lock
        List<User> users = new LinkedList<User>();
        users.add(alicesBob);
        users.add(alicesCarl);

        try {
            alicesStopManager.stop(users, "test", monitor);
            Assert.fail("Should not be reached");
        } catch (CancellationException e) {
            //
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
    @Test
    public void testCancelAndLeaveSession() {
        final IProgressMonitor monitor = new NullProgressMonitor();

        // Create a special session that allows us to kick bob out of the
        // session and test the cancellation for that case.
        alicesSession = EasyMock.createMock(ISarosSession.class);
        alicesSession.addActivityProvider(EasyMock.isA(StopManager.class));
        alicesSession.removeActivityProvider(EasyMock.isA(StopManager.class));

        alicesAlice = new User(alicesSession, new JID("alice"), 1, -1);
        alicesBob = new User(alicesSession, new JID("bob"), 2, -1);
        alicesCarl = new User(alicesSession, new JID("carl"), 3, -1);
        alicesSession.getLocalUser();
        EasyMock.expectLastCall().andReturn(alicesAlice).anyTimes();
        alicesSession.getUser(alicesAlice.getJID());
        EasyMock.expectLastCall().andReturn(alicesAlice).anyTimes();
        alicesSession.getUser(alicesCarl.getJID());
        EasyMock.expectLastCall().andReturn(alicesCarl).anyTimes();
        alicesSession.getUser(alicesBob.getJID());
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {

            @Override
            public Object answer() throws Throwable {
                if (monitor.isCanceled())
                    return null;
                return alicesBob;
            }
        }).anyTimes();

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

        // prepare to cancel early
        bobsStopManager.addBlockable(new Blockable() {
            @Override
            public void unblock() {
                // nothing to do
            }

            @Override
            public void block() {
                monitor.setCanceled(true);
            }
        });

        // now start to lock
        List<User> users = new LinkedList<User>();
        users.add(alicesBob);
        users.add(alicesCarl);

        try {
            alicesStopManager.stop(users, "test", monitor);
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
    @Test
    public void testNoReplyAndLeave() {
        // Abuse the monitor so we can use it in the anonymous class.
        final IProgressMonitor activitySent = new NullProgressMonitor();

        // Create a special session that allows us to kick bob out of the
        // session and test the cancellation for that case.
        alicesSession = EasyMock.createMock(ISarosSession.class);
        alicesSession.addActivityProvider(EasyMock.isA(StopManager.class));
        alicesSession.removeActivityProvider(EasyMock.isA(StopManager.class));

        alicesAlice = new User(alicesSession, new JID("alice"), 1, -1);
        alicesBob = new User(alicesSession, new JID("bob"), 2, -1);
        alicesCarl = new User(alicesSession, new JID("carl"), 3, -1);
        alicesSession.getLocalUser();
        EasyMock.expectLastCall().andReturn(alicesAlice).anyTimes();
        alicesSession.getUser(alicesAlice.getJID());
        EasyMock.expectLastCall().andReturn(alicesAlice).anyTimes();
        alicesSession.getUser(alicesCarl.getJID());
        EasyMock.expectLastCall().andReturn(alicesCarl).anyTimes();
        alicesSession.getUser(alicesBob.getJID());
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {

            @Override
            public Object answer() throws Throwable {
                if (activitySent.isCanceled())
                    return null;
                return alicesBob;
            }
        }).anyTimes();

        EasyMock.replay(alicesSession);

        // Observe the activities created and remember a lock request.
        IActivityListener listener = EasyMock
            .createMock(IActivityListener.class);
        listener.activityCreated(EasyMock.isA(StopActivity.class));
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {

            @Override
            public Object answer() throws Throwable {
                StopActivity stop = (StopActivity) EasyMock
                    .getCurrentArguments()[0];
                if (stop.getType() == StopActivity.Type.LOCKREQUEST)
                    activitySent.setCanceled(true);

                return null;
            }
        }).anyTimes();
        EasyMock.replay(listener);

        List<User> users = new LinkedList<User>();
        users.add(alicesBob);

        StopManager stopManager = new StopManager(alicesSession);
        stopManager.addActivityListener(listener);
        stopManager.start();
        try {
            stopManager.stop(users, "test", new NullProgressMonitor());
            Assert.fail("Could not stop.");
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
    @Test
    public void testLockAndUserLeaveOnResume() {
        // Abuse the monitor so we can use it in the anonymous class.
        final IProgressMonitor activitySent = new NullProgressMonitor();

        // Create a special session that allows us to kick bob out of the
        // session and test the cancellation for that case.
        alicesSession = EasyMock.createMock(ISarosSession.class);
        alicesSession.addActivityProvider(EasyMock.isA(StopManager.class));
        alicesSession.removeActivityProvider(EasyMock.isA(StopManager.class));

        alicesAlice = new User(alicesSession, new JID("alice"), 1, -1);
        alicesBob = new User(alicesSession, new JID("bob"), 2, -1);
        alicesCarl = new User(alicesSession, new JID("carl"), 3, -1);
        alicesSession.getLocalUser();
        EasyMock.expectLastCall().andReturn(alicesAlice).anyTimes();
        alicesSession.getUser(alicesAlice.getJID());
        EasyMock.expectLastCall().andReturn(alicesAlice).anyTimes();
        alicesSession.getUser(alicesCarl.getJID());
        EasyMock.expectLastCall().andReturn(alicesCarl).anyTimes();
        alicesSession.getUser(alicesBob.getJID());
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {

            @Override
            public Object answer() throws Throwable {
                if (activitySent.isCanceled())
                    return null;
                return alicesBob;
            }
        }).anyTimes();

        EasyMock.replay(alicesSession);

        final StopManager alicesStopManager = new StopManager(alicesSession);
        final StopManager bobsStopManager = new StopManager(bobsSession);

        // Observe the activities created and remember a lock request.
        IActivityListener alicesListener = EasyMock
            .createMock(IActivityListener.class);
        alicesListener.activityCreated(EasyMock.isA(StopActivity.class));
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {

            @Override
            public Object answer() throws Throwable {
                StopActivity stop = (StopActivity) EasyMock
                    .getCurrentArguments()[0];
                if (stop.getType() == StopActivity.Type.UNLOCKREQUEST)
                    activitySent.setCanceled(true);
                else
                    bobsStopManager
                        .exec(rewriteStopActivity(stop, bobsSession));

                return null;
            }
        }).anyTimes();
        EasyMock.replay(alicesListener);

        // Start the managers now
        alicesStopManager.addActivityListener(alicesListener);
        alicesStopManager.start();

        IActivityListener bobsListener = createForwarder(alicesSession,
            alicesStopManager);
        bobsStopManager.addActivityListener(bobsListener);
        bobsStopManager.start();

        // Stop bob now
        List<User> users = new LinkedList<User>();
        users.add(alicesBob);
        List<StartHandle> handles = alicesStopManager.stop(users, "test",
            new NullProgressMonitor());

        Assert.assertEquals(1, handles.size());

        // Test that we resume
        for (StartHandle handle : handles) {
            boolean res = handle.startAndAwait(new NullProgressMonitor());
            Assert.assertFalse(res);
        }

        alicesStopManager.stop();
        bobsStopManager.stop();

        EasyMock.verify(alicesListener);
        EasyMock.verify(alicesSession);
        EasyMock.verify(bobsListener);
        EasyMock.verify(bobsSession);
    }

    private static User rewriteUser(User user, ISarosSession target) {
        return new User(target, user.getJID(), user.getColorID(), -1);
    }

    private static StopActivity rewriteStopActivity(StopActivity inActivity,
        ISarosSession target) {
        return new StopActivity(rewriteUser(inActivity.getUser(), target),
            rewriteUser(inActivity.getInitiator(), target), rewriteUser(
                inActivity.getUser(), target), inActivity.getType(),
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
                targetManager.exec(rewriteStopActivity(inActivity,
                    targetSession));
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
    private IActivityListener createForwarder(final StopManager bob,
        final StopManager carl) {
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
                if (inActivity.getRecipient().getJID().equals(new JID("bob")))
                    bob.exec(rewriteStopActivity(inActivity, bobsSession));
                else if (inActivity.getRecipient().getJID()
                    .equals(new JID("carl")))
                    carl.exec(rewriteStopActivity(inActivity, carlsSession));
                else
                    Assert.fail("Should not be reached");
                return null;
            }
        }).anyTimes();
        EasyMock.replay(listener);
        return listener;
    }
}
