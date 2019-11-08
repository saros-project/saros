package saros.session.internal;

import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.getCurrentArguments;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;
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
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import saros.awareness.AwarenessInformationCollector;
import saros.context.CoreContextFactory;
import saros.context.IContainerContext;
import saros.context.IContextKeyBindings;
import saros.editor.EditorManager;
import saros.feedback.FeedbackManager;
import saros.feedback.FeedbackPreferences;
import saros.feedback.StatisticManager;
import saros.filesystem.IPathFactory;
import saros.net.IConnectionManager;
import saros.net.IReceiver;
import saros.net.ITransmitter;
import saros.net.PacketCollector;
import saros.net.xmpp.JID;
import saros.net.xmpp.XMPPConnectionService;
import saros.preferences.EclipsePreferenceConstants;
import saros.preferences.EclipsePreferences;
import saros.preferences.PreferenceStore;
import saros.project.internal.SarosEclipseSessionContextFactory;
import saros.repackaged.picocontainer.BindKey;
import saros.repackaged.picocontainer.MutablePicoContainer;
import saros.repackaged.picocontainer.PicoBuilder;
import saros.repackaged.picocontainer.PicoContainer;
import saros.repackaged.picocontainer.injectors.AnnotatedFieldInjection;
import saros.repackaged.picocontainer.injectors.CompositeInjection;
import saros.repackaged.picocontainer.injectors.ConstructorInjection;
import saros.repackaged.picocontainer.injectors.Reinjector;
import saros.session.ISarosSessionContextFactory;
import saros.session.ISarosSessionManager;
import saros.session.SessionEndReason;
import saros.synchronize.StopManager;
import saros.test.fakes.synchonize.NonUISynchronizer;
import saros.test.mocks.EclipseMocker;
import saros.test.mocks.EditorManagerMock;

@RunWith(PowerMockRunner.class)
@PrepareForTest({StatisticManager.class, ResourcesPlugin.class})
@PowerMockIgnore({"javax.xml.*"})
public class SarosSessionTest {

  private static final String SAROS_SESSION_ID = "SAROS_SESSION_TEST";

  private static class CountingReceiver implements IReceiver {

    private int currentListeners;

    @Override
    public synchronized void addPacketListener(PacketListener listener, PacketFilter filter) {
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

    public synchronized int getCurrentPacketListenersCount() {
      return currentListeners;
    }
  }

  private static XMPPConnectionService createConnectionServiceMock() {
    XMPPConnectionService srv = createNiceMock(XMPPConnectionService.class);

    expect(srv.getJID())
        .andStubAnswer(
            new IAnswer<JID>() {

              @Override
              public JID answer() throws Throwable {
                return new JID("alice");
              }
            });

    replay(srv);
    return srv;
  }

  private static IContainerContext createContextMock(final MutablePicoContainer container) {

    final IContainerContext context = createMock(IContainerContext.class);

    context.initComponent(isA(Object.class));

    expectLastCall()
        .andAnswer(
            new IAnswer<Object>() {

              @Override
              public Object answer() throws Throwable {
                Object session = getCurrentArguments()[0];
                MutablePicoContainer dummyContainer = container.makeChildContainer();
                dummyContainer.addComponent(session.getClass(), session);
                new Reinjector(dummyContainer)
                    .reinject(session.getClass(), new AnnotatedFieldInjection());
                container.removeChildContainer(dummyContainer);
                return null;
              }
            })
        .times(2);

    expect(context.getComponent(isA(Class.class)))
        .andStubAnswer(
            new IAnswer<Object>() {

              @Override
              public Object answer() throws Throwable {
                return container.getComponent(getCurrentArguments()[0]);
              }
            });

    context.createChildContainer();
    expectLastCall().andReturn(container.makeChildContainer());
    context.removeChildContainer(isA(MutablePicoContainer.class));
    expectLastCall()
        .andAnswer(
            new IAnswer<Object>() {

              @Override
              public Object answer() throws Throwable {
                container.removeChildContainer((PicoContainer) getCurrentArguments()[0]);
                return true;
              }
            });

    replay(context);

    return context;
  }

  private static IWorkspace createWorkspaceMock(final List<Object> workspaceListeners) {

    final IWorkspace workspace = createMock(IWorkspace.class);

    workspace.addResourceChangeListener(isA(IResourceChangeListener.class), anyInt());

    expectLastCall()
        .andStubAnswer(
            new IAnswer<Object>() {

              @Override
              public Object answer() throws Throwable {
                workspaceListeners.add(getCurrentArguments()[0]);
                return null;
              }
            });

    workspace.removeResourceChangeListener(isA(IResourceChangeListener.class));

    expectLastCall()
        .andStubAnswer(
            new IAnswer<Object>() {

              @Override
              public Object answer() throws Throwable {
                workspaceListeners.remove(getCurrentArguments()[0]);
                return null;
              }
            });

    replay(workspace);

    PowerMock.mockStaticPartial(ResourcesPlugin.class, "getWorkspace");
    ResourcesPlugin.getWorkspace();
    expectLastCall().andReturn(workspace).anyTimes();
    PowerMock.replay(ResourcesPlugin.class);

    return workspace;
  }

  private IReceiver createReceiverMock() {
    countingReceiver = new CountingReceiver();

    IReceiver receiver = createMock(IReceiver.class);

    receiver.addPacketListener(anyObject(PacketListener.class), anyObject(PacketFilter.class));

    expectLastCall().andStubDelegateTo(countingReceiver);

    receiver.removePacketListener(anyObject(PacketListener.class));
    expectLastCall().andStubDelegateTo(countingReceiver);

    replay(receiver);
    return receiver;
  }

  private void addMockedComponent(Class<?> clazz) {
    Object mock = createNiceMock(clazz);
    replay(mock);
    container.addComponent(clazz, mock);
  }

  private MutablePicoContainer container;

  private CountingReceiver countingReceiver;

  private final List<Object> editorListeners = new LinkedList<Object>();

  private final List<Object> workspaceListeners = new LinkedList<Object>();

  @Before
  public void setUp() {
    PicoBuilder picoBuilder =
        new PicoBuilder(
                new CompositeInjection(new ConstructorInjection(), new AnnotatedFieldInjection()))
            .withCaching()
            .withLifecycle();

    container = picoBuilder.build();

    new CoreContextFactory().createComponents(container);

    /*
     * components needed for runtime but currently not present or not
     * available in the core
     */

    container.addComponent(
        BindKey.bindKey(String.class, IContextKeyBindings.SarosVersion.class), "0815");

    container.addComponent(
        BindKey.bindKey(String.class, IContextKeyBindings.PlatformVersion.class), "4711");

    container.addComponent(
        ISarosSessionContextFactory.class, SarosEclipseSessionContextFactory.class);

    final IPreferenceStore store = EclipseMocker.initPreferenceStore(container);

    final Preferences preferences = EclipseMocker.initPreferences();

    // triggers SWTUtils if not disabled and causes issues
    preferences.putInt(
        EclipsePreferenceConstants.FEEDBACK_SURVEY_DISABLED, FeedbackManager.FEEDBACK_DISABLED);

    // Init Feedback
    FeedbackPreferences.setPreferences(preferences);

    EclipseMocker.mockSarosWithPreferences(container, store, preferences);

    container.addComponent(AwarenessInformationCollector.class);
    container.addComponent(EclipsePreferences.class);
    container.addComponent(FeedbackManager.class);
    container.addComponent(NonUISynchronizer.class);

    /*
     * Replacements
     */
    container.removeComponent(XMPPConnectionService.class);
    container.addComponent(XMPPConnectionService.class, createConnectionServiceMock());

    container.removeComponent(IConnectionManager.class);
    addMockedComponent(IConnectionManager.class);

    container.removeComponent(ITransmitter.class);
    addMockedComponent(ITransmitter.class);

    container.removeComponent(IReceiver.class);
    addMockedComponent(IReceiver.class);

    /*
     * Additional components
     */
    container.addComponent(createReceiverMock());

    container.addComponent(EditorManager.class, EditorManagerMock.createMock(editorListeners));

    addMockedComponent(IPathFactory.class);
    addMockedComponent(ISarosSessionManager.class);

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

    final IContainerContext context = createContextMock(container);

    // Test creating, starting and stopping the session.
    SarosSession session = new SarosSession(SAROS_SESSION_ID, new PreferenceStore(), context);

    assertFalse(session.hasActivityConsumers());
    assertFalse(session.hasActivityProducers());
    assertEquals(0, countingReceiver.getCurrentPacketListenersCount());

    assertTrue(workspaceListeners.isEmpty());

    StopManager stopManager3 = session.getComponent(StopManager.class);

    assertNotNull("component must be available before the session is started", stopManager3);

    session.start();

    StopManager stopManager1 = session.getStopManager();
    StopManager stopManager2 = session.getStopManager();

    assertSame(stopManager1, stopManager2);

    stopManager3 = session.getComponent(StopManager.class);

    assertSame(stopManager2, stopManager3);

    assertTrue(session.hasActivityConsumers());
    assertTrue(session.hasActivityProducers());
    assertFalse(workspaceListeners.isEmpty());

    session.stop(SessionEndReason.LOCAL_USER_LEFT);

    stopManager3 = session.getComponent(StopManager.class);
    assertNull("component must not be available after the session is stopped", stopManager3);

    assertTrue(editorListeners.isEmpty());
    assertFalse(session.hasActivityConsumers());
    assertFalse(session.hasActivityProducers());
    assertTrue(workspaceListeners.isEmpty());
    assertEquals(SAROS_SESSION_ID, session.getID());
    assertEquals(
        "not all packet listeners were removed from the receiver",
        0,
        countingReceiver.getCurrentPacketListenersCount());

    PowerMock.verifyAll();
  }

  /**
   * This test is a stress test and only meant for running manually. It will allocate and dispose
   * sessions as fast as possible.
   */
  @Ignore
  @Test
  public void testInfiniteSessions() throws Exception {
    for (; ; ) testCreateSarosSession();
  }
}
