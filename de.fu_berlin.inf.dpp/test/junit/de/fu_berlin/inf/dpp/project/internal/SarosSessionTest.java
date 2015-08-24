package de.fu_berlin.inf.dpp.project.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.service.prefs.Preferences;
import org.picocontainer.BindKey;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;
import org.picocontainer.PicoContainer;
import org.picocontainer.injectors.AnnotatedFieldInjection;
import org.picocontainer.injectors.CompositeInjection;
import org.picocontainer.injectors.ConstructorInjection;
import org.picocontainer.injectors.Reinjector;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.fu_berlin.inf.dpp.ISarosContext;
import de.fu_berlin.inf.dpp.ISarosContextBindings;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SarosCoreContextFactory;
import de.fu_berlin.inf.dpp.concurrent.watchdog.ConsistencyWatchdogClient;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.internal.IEditorAPI;
import de.fu_berlin.inf.dpp.feedback.FeedbackManager;
import de.fu_berlin.inf.dpp.feedback.FeedbackPreferences;
import de.fu_berlin.inf.dpp.feedback.StatisticCollectorTest;
import de.fu_berlin.inf.dpp.feedback.StatisticManager;
import de.fu_berlin.inf.dpp.net.IConnectionManager;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.PacketCollector;
import de.fu_berlin.inf.dpp.net.internal.BinaryXMPPExtension;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import de.fu_berlin.inf.dpp.preferences.EclipsePreferenceInitializer;
import de.fu_berlin.inf.dpp.preferences.EclipsePreferenceStoreAdapter;
import de.fu_berlin.inf.dpp.preferences.EclipsePreferences;
import de.fu_berlin.inf.dpp.session.ISarosSessionContextFactory;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.synchronize.StopManager;
import de.fu_berlin.inf.dpp.test.fakes.synchonize.NonUISynchronizer;
import de.fu_berlin.inf.dpp.test.util.MemoryPreferenceStore;
import de.fu_berlin.inf.dpp.test.util.MemoryPreferences;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ StatisticManager.class, ResourcesPlugin.class })
public class SarosSessionTest {

    private static final String SAROS_SESSION_ID = "SAROS_SESSION_TEST";

    private static class CountingReceiver implements IReceiver {

        private int currentListeners;

        @Override
        public synchronized void addPacketListener(PacketListener listener,
            PacketFilter filter) {
            currentListeners++;
        }

        @Override
        public synchronized void removePacketListener(PacketListener listener) {
            currentListeners--;
        }

        @Override
        public void processPacket(Packet packet) {
            throw new UnsupportedOperationException();
        }

        @Override
        public PacketCollector createCollector(PacketFilter filter) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void processBinaryXMPPExtension(
            final BinaryXMPPExtension extension) {
            throw new UnsupportedOperationException();
        }

        public synchronized int getCurrentPacketListenersCount() {
            return currentListeners;
        }
    }

    private static XMPPConnectionService createConnectionServiceMock() {
        XMPPConnectionService srv = EasyMock
            .createNiceMock(XMPPConnectionService.class);

        EasyMock.expect(srv.getJID()).andStubAnswer(new IAnswer<JID>() {

            @Override
            public JID answer() throws Throwable {
                return new JID("alice");
            }
        });

        EasyMock.replay(srv);
        return srv;
    }

    public static Saros createSarosMock(IPreferenceStore store,
        Preferences preferences) {

        Saros saros = EasyMock.createNiceMock(Saros.class);

        saros.getPreferenceStore();
        EasyMock.expectLastCall().andStubReturn(store);

        saros.getGlobalPreferences();
        EasyMock.expectLastCall().andStubReturn(preferences);

        EasyMock.replay(saros);

        return saros;
    }

    public static IConnectionManager createDataTransferManagerMock() {
        IConnectionManager mock = EasyMock
            .createNiceMock(DataTransferManager.class);

        EasyMock.replay(mock);
        return mock;
    }

    private static ISarosContext createContextMock(
        final MutablePicoContainer container) {

        final ISarosContext context = EasyMock.createMock(ISarosContext.class);

        context.initComponent(EasyMock.isA(Object.class));

        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {

            @Override
            public Object answer() throws Throwable {
                Object session = EasyMock.getCurrentArguments()[0];
                MutablePicoContainer dummyContainer = container
                    .makeChildContainer();
                dummyContainer.addComponent(session.getClass(), session);
                new Reinjector(dummyContainer).reinject(session.getClass(),
                    new AnnotatedFieldInjection());
                container.removeChildContainer(dummyContainer);
                return null;
            }
        }).times(2);

        EasyMock.expect(context.getComponent(EasyMock.isA(Class.class)))
            .andStubAnswer(new IAnswer<Object>() {

                @Override
                public Object answer() throws Throwable {
                    return container.getComponent(EasyMock
                        .getCurrentArguments()[0]);
                }
            });

        context.createSimpleChildContainer();
        EasyMock.expectLastCall().andReturn(container.makeChildContainer());
        context.removeChildContainer(EasyMock.isA(MutablePicoContainer.class));
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {

            @Override
            public Object answer() throws Throwable {
                container.removeChildContainer((PicoContainer) EasyMock
                    .getCurrentArguments()[0]);
                return true;
            }
        });

        EasyMock.replay(context);

        return context;
    }

    private static IWorkspace createWorkspaceMock(
        final List<Object> workspaceListeners) {

        final IWorkspace workspace = EasyMock.createMock(IWorkspace.class);

        workspace.addResourceChangeListener(
            EasyMock.isA(IResourceChangeListener.class), EasyMock.anyInt());

        EasyMock.expectLastCall().andStubAnswer(new IAnswer<Object>() {

            @Override
            public Object answer() throws Throwable {
                workspaceListeners.add(EasyMock.getCurrentArguments()[0]);
                return null;
            }
        });

        workspace.removeResourceChangeListener(EasyMock
            .isA(IResourceChangeListener.class));

        EasyMock.expectLastCall().andStubAnswer(new IAnswer<Object>() {

            @Override
            public Object answer() throws Throwable {
                workspaceListeners.remove(EasyMock.getCurrentArguments()[0]);
                return null;
            }
        });

        EasyMock.replay(workspace);

        PowerMock.mockStaticPartial(ResourcesPlugin.class, "getWorkspace");
        ResourcesPlugin.getWorkspace();
        EasyMock.expectLastCall().andReturn(workspace).anyTimes();
        PowerMock.replay(ResourcesPlugin.class);

        return workspace;
    }

    private MutablePicoContainer container;

    private CountingReceiver countingReceiver;

    private final List<Object> editorListeners = new LinkedList<Object>();

    private final List<Object> workspaceListeners = new LinkedList<Object>();

    @Before
    public void setUp() {

        PicoBuilder picoBuilder = new PicoBuilder(new CompositeInjection(
            new ConstructorInjection(), new AnnotatedFieldInjection()))
            .withCaching().withLifecycle();

        container = picoBuilder.build();

        new SarosCoreContextFactory().createComponents(container);

        /*
         * components needed for runtime but currently not present or not
         * available in the core
         */

        container.addComponent(BindKey.bindKey(String.class,
            ISarosContextBindings.SarosVersion.class), "0815");

        container.addComponent(BindKey.bindKey(String.class,
            ISarosContextBindings.PlatformVersion.class), "4711");

        container.addComponent(ISarosSessionContextFactory.class,
            SarosEclipseSessionContextFactory.class);

        final IPreferenceStore store = new MemoryPreferenceStore();
        EclipsePreferenceInitializer.setPreferences(store);

        final Preferences preferences = new MemoryPreferences();
        EclipsePreferenceInitializer.setPreferences(preferences);

        container
            .addComponent(Saros.class, createSarosMock(store, preferences));
        container.addComponent(EclipsePreferences.class);

        // Eclipse store interface
        container.addComponent(store);
        // Saros core store interface
        container.addComponent(new EclipsePreferenceStoreAdapter(store));

        container.addComponent(NonUISynchronizer.class);
        container.addComponent(FeedbackManager.class);

        // Init Feedback
        FeedbackPreferences.setPreferences(preferences);

        /*
         * REPLACEMENTS
         */
        container.removeComponent(XMPPConnectionService.class);

        container.addComponent(XMPPConnectionService.class,
            createConnectionServiceMock());

        container.removeComponent(IConnectionManager.class);
        container.addComponent(IConnectionManager.class,
            createDataTransferManagerMock());

        container.removeComponent(ITransmitter.class);
        container.addComponent(ITransmitter.class,
            EasyMock.createMock(ITransmitter.class));

        countingReceiver = new CountingReceiver();

        IReceiver receiver = EasyMock.createMock(IReceiver.class);

        receiver.addPacketListener(EasyMock.anyObject(PacketListener.class),
            EasyMock.anyObject(PacketFilter.class));

        EasyMock.expectLastCall().andStubDelegateTo(countingReceiver);

        receiver.removePacketListener(EasyMock.anyObject(PacketListener.class));
        EasyMock.expectLastCall().andStubDelegateTo(countingReceiver);

        EasyMock.replay(receiver);

        container.addComponent(receiver);

        final ISarosSessionManager sessionManager = EasyMock
            .createNiceMock(ISarosSessionManager.class);

        EasyMock.replay(sessionManager);
        container.addComponent(ISarosSessionManager.class, sessionManager);

        final IEditorAPI editorAPI = EasyMock.createNiceMock(IEditorAPI.class);

        EasyMock.replay(editorAPI);

        container.addComponent(IEditorAPI.class, editorAPI);

        final ConsistencyWatchdogClient clientWatchdog = EasyMock
            .createNiceMock(ConsistencyWatchdogClient.class);

        EasyMock.replay(clientWatchdog);

        container.addComponent(ConsistencyWatchdogClient.class, clientWatchdog);

        container.addComponent(EditorManager.class,
            StatisticCollectorTest.createEditorManagerMock(editorListeners));

        container.start();

    }

    @After
    public void tearDown() {
        container.stop();
        container.dispose();
        editorListeners.clear();
    }

    @Test
    public void testCreateSarosSession() {

        createWorkspaceMock(workspaceListeners);

        final ISarosContext context = createContextMock(container);

        // Test creating, starting and stopping the session.
        SarosSession session = new SarosSession(SAROS_SESSION_ID, null, 0,
            context);

        assertFalse(session.hasActivityConsumers());
        assertFalse(session.hasActivityProducers());
        assertEquals(0, countingReceiver.getCurrentPacketListenersCount());

        assertTrue(workspaceListeners.isEmpty());

        StopManager stopManager3 = (StopManager) session
            .getComponent(StopManager.class);

        assertNull(
            "component must not be available before the session is started",
            stopManager3);

        session.start();

        StopManager stopManager1 = session.getStopManager();
        StopManager stopManager2 = session.getStopManager();

        assertSame(stopManager1, stopManager2);

        stopManager3 = (StopManager) session.getComponent(StopManager.class);

        assertSame(stopManager2, stopManager3);

        assertTrue(session.hasActivityConsumers());
        assertTrue(session.hasActivityProducers());
        assertFalse(workspaceListeners.isEmpty());

        session.stop();

        stopManager3 = (StopManager) session.getComponent(StopManager.class);
        assertNull(
            "component must not be available after the session is stopped",
            stopManager3);

        assertTrue(editorListeners.isEmpty());
        assertFalse(session.hasActivityConsumers());
        assertFalse(session.hasActivityProducers());
        assertTrue(workspaceListeners.isEmpty());
        assertEquals(SAROS_SESSION_ID, session.getID());
        assertEquals("not all packet listeners were removed from the receiver",
            0, countingReceiver.getCurrentPacketListenersCount());

        PowerMock.verifyAll();
    }

    /**
     * This test is a stress test and only meant for running manually. It will
     * allocate and dispose sessions as fast as possible.
     */
    @Ignore
    @Test
    public void testInfiniteSessions() throws Exception {
        for (;;)
            testCreateSarosSession();
    }
}
