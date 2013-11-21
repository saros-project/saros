package de.fu_berlin.inf.dpp.feedback;

import java.util.LinkedList;
import java.util.List;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.service.prefs.Preferences;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;
import org.picocontainer.injectors.AnnotatedFieldInjection;
import org.picocontainer.injectors.CompositeInjection;
import org.picocontainer.injectors.ConstructorInjection;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.communication.audio.AudioServiceManager;
import de.fu_berlin.inf.dpp.communication.audio.IAudioServiceListener;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.ISharedEditorListener;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.preferences.PreferenceInitializer;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.project.internal.SarosSessionTest;
import de.fu_berlin.inf.dpp.test.util.MemoryPreferenceStore;
import de.fu_berlin.inf.dpp.test.util.MemoryPreferences;
import de.fu_berlin.inf.dpp.util.Utils;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Utils.class })
public class StatisticCollectorTest {

    private static ISarosSession createSessionMock(
        final List<Object> sessionListeners) {
        ISarosSession session = EasyMock.createMock(ISarosSession.class);
        final User bob = new User(new JID("bob"), false, false, 1, -1);
        final User alice = new User(new JID("alice"), false, false, 2, -1);
        final List<User> participants = new LinkedList<User>();
        participants.add(bob);
        participants.add(alice);
        session.addListener(EasyMock.isA(ISharedProjectListener.class));
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {

            @Override
            public Object answer() throws Throwable {
                sessionListeners.add(EasyMock.getCurrentArguments()[0]);
                return null;
            }
        }).anyTimes();
        session.removeListener(EasyMock.isA(ISharedProjectListener.class));
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {
            @Override
            public Object answer() throws Throwable {
                sessionListeners.remove(EasyMock.getCurrentArguments()[0]);
                return null;
            }
        }).anyTimes();

        EasyMock.expect(session.getLocalUser()).andStubReturn(bob);
        EasyMock.expect(session.getUsers()).andStubReturn(participants);
        EasyMock.expect(session.getHost()).andStubReturn(bob);
        EasyMock.expect(session.getID()).andStubReturn("0815");
        EasyMock.replay(session);
        return session;
    }

    public static EditorManager createEditorManagerMock(
        final List<Object> editorListeners) {
        EditorManager editorManager = EasyMock.createMock(EditorManager.class);
        editorManager.addSharedEditorListener(EasyMock
            .isA(ISharedEditorListener.class));
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {

            @Override
            public Object answer() throws Throwable {
                editorListeners.add(EasyMock.getCurrentArguments()[0]);
                return null;
            }
        }).anyTimes();
        editorManager.removeSharedEditorListener(EasyMock
            .isA(ISharedEditorListener.class));
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {

            @Override
            public Object answer() throws Throwable {
                editorListeners.remove(EasyMock.getCurrentArguments()[0]);
                return null;
            }
        }).anyTimes();
        EasyMock.replay(editorManager);
        return editorManager;
    }

    private static AudioServiceManager createAudioServiceManagerMock(
        final List<Object> listeners) {
        AudioServiceManager manager = EasyMock
            .createMock(AudioServiceManager.class);
        manager.addAudioListener(EasyMock.isA(IAudioServiceListener.class));
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {

            @Override
            public Object answer() throws Throwable {
                listeners.add(EasyMock.getCurrentArguments()[0]);
                return null;
            }
        }).anyTimes();
        manager.removeAudioListener(EasyMock.isA(IAudioServiceListener.class));
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {

            @Override
            public Object answer() throws Throwable {
                listeners.remove(EasyMock.getCurrentArguments()[0]);
                return null;
            }
        }).anyTimes();
        EasyMock.replay(manager);
        return manager;
    }

    @Test
    public void testCollectorRegistrationAndDestruction() {

        // Eclipse is not initialized. Create a mock for that method
        PowerMock.mockStaticPartial(Utils.class, "getEclipsePlatformInfo");
        Utils.getEclipsePlatformInfo();
        EasyMock.expectLastCall().andReturn("JUnit-Test").anyTimes();
        PowerMock.replayAll(Utils.class);

        // Create a container
        final MutablePicoContainer container = new PicoBuilder(
            new CompositeInjection(new ConstructorInjection(),
                new AnnotatedFieldInjection())).withCaching().withLifecycle()
            .build();

        // session
        final List<Object> sessionListeners = new LinkedList<Object>();
        ISarosSession session = createSessionMock(sessionListeners);
        container.addComponent(ISarosSession.class, session);

        // editor
        final List<Object> editorListeners = new LinkedList<Object>();
        EditorManager editorManager = createEditorManagerMock(editorListeners);
        container.addComponent(EditorManager.class, editorManager);

        // audo service manager
        final List<Object> audioListeners = new LinkedList<Object>();
        AudioServiceManager audioManager = createAudioServiceManagerMock(audioListeners);
        container.addComponent(AudioServiceManager.class, audioManager);

        final IPreferenceStore store = new MemoryPreferenceStore();
        final Preferences preferences = new MemoryPreferences();

        PreferenceInitializer.setPreferences(store);
        PreferenceInitializer.setPreferences(preferences);

        container.addComponent(Saros.class,
            SarosSessionTest.createSarosMock(store));

        container.addComponent(IPreferenceStore.class, store);

        FeedbackPreferences.setPreferences(preferences);

        container.addComponent(DataTransferManager.class,
            SarosSessionTest.createDataTransferManagerMock());

        // Components we want to create
        container.addComponent(StatisticManager.class);
        container.addComponent(FeedbackManager.class);

        container.addComponent(DataTransferCollector.class);
        container.addComponent(PermissionChangeCollector.class);
        container.addComponent(ParticipantCollector.class);
        container.addComponent(SessionDataCollector.class);
        container.addComponent(TextEditCollector.class);
        container.addComponent(JumpFeatureUsageCollector.class);
        container.addComponent(FollowModeCollector.class);
        container.addComponent(SelectionCollector.class);
        container.addComponent(VoIPCollector.class);
        container.getComponents();

        // Verify that the collectors are available
        StatisticManager manager = container
            .getComponent(StatisticManager.class);
        Assert.assertEquals(9, manager.getAvailableCollectorCount());
        Assert.assertEquals(0, manager.getActiveCollectorCount());

        // Verify that they are active now
        container.start();
        Assert.assertEquals(9, manager.getAvailableCollectorCount());
        Assert.assertEquals(9, manager.getActiveCollectorCount());

        // Verify that they are not active anymore
        container.stop();
        Assert.assertEquals(9, manager.getAvailableCollectorCount());
        Assert.assertEquals(0, manager.getActiveCollectorCount());
        Assert.assertTrue(sessionListeners.isEmpty());
        Assert.assertTrue(editorListeners.isEmpty());
        Assert.assertTrue(audioListeners.isEmpty());

        PowerMock.verifyAll();
    }
}
