package de.fu_berlin.inf.dpp.project.internal;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.awareness.AwarenessInformationCollector;
import de.fu_berlin.inf.dpp.communication.audio.AudioServiceManager;
import de.fu_berlin.inf.dpp.concurrent.watchdog.ConsistencyWatchdogClient;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.colorstorage.ColorIDSetStorage;
import de.fu_berlin.inf.dpp.feedback.FeedbackManager;
import de.fu_berlin.inf.dpp.feedback.SessionStatistic;
import de.fu_berlin.inf.dpp.feedback.StatisticCollectorTest;
import de.fu_berlin.inf.dpp.feedback.StatisticManager;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.SarosNet;
import de.fu_berlin.inf.dpp.net.business.ActivitiesHandler;
import de.fu_berlin.inf.dpp.net.business.DispatchThreadContext;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.net.internal.TransferModeDispatch;
import de.fu_berlin.inf.dpp.observables.FileReplacementInProgressObservable;
import de.fu_berlin.inf.dpp.observables.ProjectNegotiationObservable;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.optional.jdt.JDTFacade;
import de.fu_berlin.inf.dpp.preferences.PreferenceInitializer;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.synchronize.StopManager;
import de.fu_berlin.inf.dpp.test.fakes.synchonize.NonUISynchronizer;
import de.fu_berlin.inf.dpp.test.util.MemoryPreferenceStore;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.util.Utils;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Utils.class, StatisticManager.class, ResourcesPlugin.class })
public class SarosSessionTest {
    static private SarosNet createSarosNetMock() {
        SarosNet net = EasyMock.createMock(SarosNet.class);
        net.getMyJID();
        EasyMock.expectLastCall().andAnswer(new IAnswer<JID>() {

            @Override
            public JID answer() throws Throwable {
                return new JID("alice");
            }
        }).anyTimes();
        EasyMock.replay(net);
        return net;
    }

    static public Saros createSarosMock(IPreferenceStore store) {

        Saros saros = EasyMock.createMock(Saros.class);

        // REMOVE THIS MOCK METHOD !!!
        saros.getPreferenceStore();
        EasyMock.expectLastCall().andReturn(store).anyTimes();

        saros.getConfigPrefs();
        EasyMock.expectLastCall()
            .andReturn(new ConfigurationScope().getNode(Saros.SAROS))
            .anyTimes();
        saros.saveConfigPrefs();
        EasyMock.expectLastCall().anyTimes();
        saros.getVersion();
        EasyMock.expectLastCall().andReturn("JUNIT").anyTimes();
        saros.getAutoFollowEnabled();
        EasyMock.expectLastCall().andReturn(false).anyTimes();
        EasyMock.replay(saros);

        return saros;
    }

    static public DataTransferManager createDataTransferManagerMock() {
        DataTransferManager mock = EasyMock
            .createMock(DataTransferManager.class);
        mock.getTransferModeDispatch();
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {
            TransferModeDispatch dispatch = new TransferModeDispatch();

            @Override
            public Object answer() throws Throwable {
                return dispatch;
            }
        }).anyTimes();
        EasyMock.replay(mock);
        return mock;
    }

    private MutablePicoContainer container;

    @Before
    public void setUp() {

        PicoBuilder picoBuilder = new PicoBuilder(new CompositeInjection(
            new ConstructorInjection(), new AnnotatedFieldInjection()))
            .withCaching().withLifecycle();

        container = picoBuilder.build();

        IPreferenceStore store = new MemoryPreferenceStore();
        PreferenceInitializer.setPreferences(store);

        // Special/Proper mocks
        container.addComponent(SarosNet.class, createSarosNetMock());
        container.addComponent(Saros.class, createSarosMock(store));
        container.addComponent(DataTransferManager.class,
            createDataTransferManagerMock());

        // Mocks that stay in the replay state
        container.addComponent(ProjectNegotiationObservable.class,
            EasyMock.createMock(ProjectNegotiationObservable.class));
        container.addComponent(PreferenceUtils.class,
            EasyMock.createMock(PreferenceUtils.class));

        // Mock in replay state for child classes
        container.addComponent(ITransmitter.class,
            EasyMock.createMock(ITransmitter.class));
        container.addComponent(ISarosSessionManager.class,
            EasyMock.createMock(ISarosSessionManager.class));
        container.addComponent(SarosUI.class,
            EasyMock.createMock(SarosUI.class));
        container.addComponent(AwarenessInformationCollector.class,
            EasyMock.createMock(AwarenessInformationCollector.class));
        container.addComponent(AudioServiceManager.class,
            EasyMock.createMock(AudioServiceManager.class));
        container.addComponent(ConsistencyWatchdogClient.class,
            EasyMock.createMock(ConsistencyWatchdogClient.class));

        // Adding the real class here.

        container.addComponent(store);
        container.addComponent(ColorIDSetStorage.class);
        container.addComponent(NonUISynchronizer.class);
        container.addComponent(DispatchThreadContext.class);
        container.addComponent(SessionIDObservable.class);
        container.addComponent(FeedbackManager.class);
        container.addComponent(FileReplacementInProgressObservable.class);
        container.addComponent(JDTFacade.class);
        container.addComponent(ActivitiesHandler.class);
    }

    @After
    public void tearDown() {
        container.stop();
        container.dispose();
    }

    @Test
    public void testCreateSarosSession() {

        List<Object> editorListeners = new LinkedList<Object>();
        container.addComponent(EditorManager.class,
            StatisticCollectorTest.createEditorManagerMock(editorListeners));

        container.start();

        ISarosContext context = EasyMock.createMock(ISarosContext.class);
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

        PowerMock.mockStaticPartial(Utils.class, "getEclipsePlatformInfo");
        Utils.getEclipsePlatformInfo();
        EasyMock.expectLastCall().andReturn("JUnit-Test").anyTimes();
        PowerMock.replayAll(Utils.class);

        PowerMock.mockStaticPartial(StatisticManager.class,
            "createStatisticFile");
        StatisticManager.createStatisticFile(
            EasyMock.isA(SessionStatistic.class), EasyMock.isA(Saros.class),
            EasyMock.isA(String.class));
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {

            @Override
            public Object answer() throws Throwable {
                File file = File.createTempFile("saros-junit-test", "tst");
                file.deleteOnExit();
                return file;
            }
        }).anyTimes();
        PowerMock.replayAll(StatisticManager.class);

        final List<Object> workspaceListeners = new LinkedList<Object>();
        IWorkspace workspace = EasyMock.createMock(IWorkspace.class);
        workspace.addResourceChangeListener(
            EasyMock.isA(IResourceChangeListener.class), EasyMock.anyInt());
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {

            @Override
            public Object answer() throws Throwable {
                workspaceListeners.add(EasyMock.getCurrentArguments()[0]);
                return null;
            }
        }).anyTimes();
        workspace.removeResourceChangeListener(EasyMock
            .isA(IResourceChangeListener.class));
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {

            @Override
            public Object answer() throws Throwable {
                workspaceListeners.remove(EasyMock.getCurrentArguments()[0]);
                return null;
            }
        }).anyTimes();
        EasyMock.replay(workspace);
        PowerMock.mockStaticPartial(ResourcesPlugin.class, "getWorkspace");
        ResourcesPlugin.getWorkspace();
        EasyMock.expectLastCall().andReturn(workspace).anyTimes();
        PowerMock.replay(ResourcesPlugin.class);

        // Test creating, starting and stopping the session.
        SarosSession session = new SarosSession(0, new DateTime(), context);
        Assert.assertFalse(session.getSequencer().isStarted());
        Assert.assertEquals(0, session.getActivityProviderCount());
        Assert.assertTrue(workspaceListeners.isEmpty());
        session.start();

        StopManager stopManager1 = session.getStopManager();
        StopManager stopManager2 = session.getStopManager();
        Assert.assertSame(stopManager1, stopManager2);
        Assert.assertTrue(session.getSequencer().isStarted());
        Assert.assertTrue(session.getActivityProviderCount() > 0);
        Assert.assertFalse(workspaceListeners.isEmpty());

        session.stop();
        Assert.assertFalse(session.getSequencer().isStarted());
        Assert.assertTrue(editorListeners.isEmpty());
        Assert.assertEquals(0, session.getActivityProviderCount());
        Assert.assertTrue(workspaceListeners.isEmpty());

        PowerMock.verifyAll();
    }

    /**
     * This test is a stress test and only meant for running manually. It will
     * allocate and dispose sessions as fast as possible.
     */
    @Ignore
    @Test
    public void testInfiniteSessions() {
        for (;;) {
            try {
                testCreateSarosSession();
            } catch (Exception e) {
                //
                System.out.println("Foo");
            }
        }
    }
}