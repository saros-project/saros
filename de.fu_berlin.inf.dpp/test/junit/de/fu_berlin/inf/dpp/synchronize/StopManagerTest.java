package de.fu_berlin.inf.dpp.synchronize;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.CancellationException;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
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

    private ISarosSession bobsSession;
    private User bobsAlice;
    private User bobsBob;

    @Before
    public void createSessionMocks() {
        alicesSession = EasyMock.createMock(ISarosSession.class);
        alicesSession.addActivityProvider(EasyMock.isA(StopManager.class));

        alicesAlice = new User(alicesSession, new JID("alice"), 1);
        alicesBob = new User(alicesSession, new JID("bob"), 2);
        alicesSession.getLocalUser();
        EasyMock.expectLastCall().andReturn(alicesAlice).anyTimes();
        alicesSession.getUser(alicesBob.getJID());
        EasyMock.expectLastCall().andReturn(alicesBob).anyTimes();
        alicesSession.getUser(alicesAlice.getJID());
        EasyMock.expectLastCall().andReturn(alicesAlice).anyTimes();
        EasyMock.replay(alicesSession);

        bobsSession = EasyMock.createMock(ISarosSession.class);
        bobsSession.addActivityProvider(EasyMock.isA(StopManager.class));

        bobsAlice = new User(bobsSession, new JID("alice"), 1);
        bobsBob = new User(bobsSession, new JID("bob"), 2);
        bobsSession.getLocalUser();
        EasyMock.expectLastCall().andReturn(bobsBob).anyTimes();
        bobsSession.getUser(bobsAlice.getJID());
        EasyMock.expectLastCall().andReturn(bobsAlice).anyTimes();
        bobsSession.getUser(bobsBob.getJID());
        EasyMock.expectLastCall().andReturn(bobsBob).anyTimes();
        EasyMock.replay(bobsSession);
    }

    /**
     * Verify that a StopManager can be created and registers with the
     * ISarosSession
     */
    @Test
    public void testCreation() {
        new StopManager(alicesSession);
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
        final StopManager bobsStopManager = new StopManager(bobsSession);

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

        EasyMock.verify(alicesSession);
        EasyMock.verify(bobsSession);
    }

    @Ignore
    @Test
    public void testStopMultipleUsers() {
        // TODO: Can't be tested right now due synchronizing on the SWT thread
    }

    private User rewriteUser(User user, ISarosSession target) {
        return new User(target, user.getJID(), user.getColorID());
    }

    private StopActivity rewriteStopActivity(StopActivity inActivity,
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
    private IActivityListener createForwarder(
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
}
