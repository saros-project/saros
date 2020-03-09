package saros.editor.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import saros.net.xmpp.JID;
import saros.preferences.EclipsePreferenceConstants;
import saros.session.ISarosSession;
import saros.session.ISessionListener;
import saros.session.User;
import saros.ui.util.SWTUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SWTUtils.class)
public class ContributionAnnotationManagerTest {

  private ContributionAnnotationManager manager;
  private ISarosSession sessionMock;
  private IPreferenceStore store;

  private Capture<ISessionListener> sessionListenerCapture;

  private static final int MAX_HISTORY_LENGTH = ContributionAnnotationManager.MAX_HISTORY_LENGTH;

  @Before
  public void setUp() {
    store = new PreferenceStore();
    store.setValue(EclipsePreferenceConstants.SHOW_CONTRIBUTION_ANNOTATIONS, true);

    sessionListenerCapture = EasyMock.newCapture();

    sessionMock = EasyMock.createNiceMock(ISarosSession.class);

    sessionMock.addListener(EasyMock.capture(sessionListenerCapture));

    EasyMock.expectLastCall().once();

    PowerMock.mockStatic(SWTUtils.class);

    final Capture<Runnable> capture = EasyMock.newCapture();

    SWTUtils.runSafeSWTAsync(EasyMock.anyObject(), EasyMock.capture(capture));

    EasyMock.expectLastCall()
        .andAnswer(
            () -> {
              capture.getValue().run();
              return null;
            })
        .anyTimes();

    PowerMock.replayAll(sessionMock);

    manager = new ContributionAnnotationManager(sessionMock, store);
  }

  @Test
  public void testHistoryRemoval() {

    User alice = new User(new JID("alice@test"), false, false, null);

    AnnotationModel model = new AnnotationModel();

    for (int i = 0; i <= MAX_HISTORY_LENGTH + 1; i++) manager.insertAnnotation(model, i, 1, alice);

    assertEquals(MAX_HISTORY_LENGTH, getAnnotationCount(model));

    manager.insertAnnotation(model, MAX_HISTORY_LENGTH, 1, alice);

    assertEquals(MAX_HISTORY_LENGTH, getAnnotationCount(model));

    assertFalse(
        "oldest annotation was not removed",
        getAnnotationPositions(model).contains(new Position(0, 1)));
  }

  @Test
  public void testHistoryRemovalAfterRefresh() {
    User alice = new User(new JID("alice@test"), false, false, null);

    AnnotationModel model = new AnnotationModel();

    for (int i = 0; i <= MAX_HISTORY_LENGTH; i++) manager.insertAnnotation(model, i, 1, alice);

    manager.refreshAnnotations(model);

    manager.insertAnnotation(model, MAX_HISTORY_LENGTH + 1, 1, alice);

    assertFalse(
        "oldest annotation was not removed after refresh",
        getAnnotationPositions(model).contains(new Position(0, 1)));
  }

  @Test
  public void testHistoryAfterSplit() {

    final User alice = new User(new JID("alice@test"), false, false, null);
    final User bob = new User(new JID("bob@test"), false, false, null);

    final AnnotationModel model = new AnnotationModel();

    manager.insertAnnotation(model, 5, 7, alice);
    manager.insertAnnotation(model, 2, 15, bob);

    manager.splitAnnotation(model, 9);

    assertEquals("split does not affected all annotations", 4, getAnnotationCount(model));

    int startIndex = 100;

    for (int i = 0; i < MAX_HISTORY_LENGTH - 1; i++, startIndex++)
      manager.insertAnnotation(model, startIndex, 1, alice);

    assertEquals(
        "splitted annotions should count as one annotion for the history",
        4 + MAX_HISTORY_LENGTH - 1,
        getAnnotationCount(model));

    manager.insertAnnotation(model, startIndex++, 1, alice);

    assertEquals(
        "splitted annotions are not correctly removed from the history",
        4 + MAX_HISTORY_LENGTH - 1 - 1,
        getAnnotationCount(model));

    for (int i = 0; i < MAX_HISTORY_LENGTH; i++, startIndex++)
      manager.insertAnnotation(model, startIndex, 1, bob);

    assertEquals(
        "splitted annotions are not correctly removed from the history",
        2 * MAX_HISTORY_LENGTH,
        getAnnotationCount(model));
  }

  @Test
  public void testAnnotationSplit() {

    final User alice = new User(new JID("alice@test"), false, false, null);
    final User bob = new User(new JID("bob@test"), false, false, null);

    final AnnotationModel model = new AnnotationModel();

    manager.insertAnnotation(model, 5, 7, alice);
    manager.insertAnnotation(model, 2, 15, bob);

    manager.splitAnnotation(model, 9);

    final List<Position> positions = getAnnotationPositions(model);

    assertEquals("split does not affected all annotations", 4, getAnnotationCount(model));

    final Position expectA0 = new Position(5, 4); // 9 = 5 + 4
    final Position expectA1 = new Position(9, 3); // 9 + 3 = 5 + 7
    final Position expectB0 = new Position(2, 7); // 9 = 2 + 7
    final Position expectB1 = new Position(9, 8); // 9 + 8 = 2 + 15

    assertTrue("expected annotation region not found: " + expectA0, positions.contains(expectA0));
    assertTrue("expected annotation region not found: " + expectA1, positions.contains(expectA1));
    assertTrue("expected annotation region not found: " + expectB0, positions.contains(expectB0));
    assertTrue("expected annotation region not found: " + expectB1, positions.contains(expectB1));
  }

  @Test
  public void testRemoveAllAnnotationsBySwitchingProperty() {

    final List<User> users =
        Arrays.asList(
            new User(new JID("alice@test"), false, false, null),
            new User(new JID("bob@test"), false, false, null),
            new User(new JID("carl@test"), false, false, null),
            new User(new JID("dave@test"), false, false, null));

    int idx = 0;

    final AnnotationModel model = new AnnotationModel();

    for (final User user : users)
      for (int i = 0; i < MAX_HISTORY_LENGTH; i++, idx++)
        manager.insertAnnotation(model, idx, 1, user);

    assertEquals(MAX_HISTORY_LENGTH * users.size(), getAnnotationCount(model));

    store.setValue(EclipsePreferenceConstants.SHOW_CONTRIBUTION_ANNOTATIONS, false);

    assertEquals(0, getAnnotationCount(model));
  }

  @Test
  public void testRemoveAnnotationsWhenUserLeaves() {

    final List<User> users = new ArrayList<>();

    users.add(new User(new JID("alice@test"), false, false, null));
    users.add(new User(new JID("bob@test"), false, false, null));

    int idx = 0;

    final AnnotationModel model = new AnnotationModel();

    for (final User user : users)
      for (int i = 0; i < MAX_HISTORY_LENGTH; i++, idx++)
        manager.insertAnnotation(model, idx, 1, user);

    assertEquals(MAX_HISTORY_LENGTH * users.size(), getAnnotationCount(model));

    final ISessionListener sessionListener = sessionListenerCapture.getValue();

    assertNotNull(sessionListener);

    while (!users.isEmpty()) {
      sessionListener.userLeft(users.remove(0));

      assertEquals(MAX_HISTORY_LENGTH * users.size(), getAnnotationCount(model));
    }
  }

  public void testInsertWhileNotEnable() {
    store.setValue(EclipsePreferenceConstants.SHOW_CONTRIBUTION_ANNOTATIONS, false);

    final User alice = new User(new JID("alice@test"), false, false, null);

    final AnnotationModel model = new AnnotationModel();

    manager.insertAnnotation(model, 5, 7, alice);

    assertEquals(0, getAnnotationCount(model));
  }

  @Test
  public void testDispose() {

    final List<User> users = new ArrayList<>();

    users.add(new User(new JID("alice@test"), false, false, null));
    users.add(new User(new JID("bob@test"), false, false, null));

    int idx = 0;

    final AnnotationModel model = new AnnotationModel();

    for (final User user : users)
      for (int i = 0; i < MAX_HISTORY_LENGTH; i++, idx++)
        manager.insertAnnotation(model, idx, 1, user);

    assertEquals(MAX_HISTORY_LENGTH * users.size(), getAnnotationCount(model));

    manager.dispose();

    assertEquals(0, getAnnotationCount(model));
  }

  private int getAnnotationCount(IAnnotationModel model) {
    int count = 0;

    Iterator<Annotation> it = model.getAnnotationIterator();

    while (it.hasNext()) {
      count++;
      it.next();
    }

    return count;
  }

  private List<Position> getAnnotationPositions(AnnotationModel model) {

    List<Position> positions = new ArrayList<Position>();

    Iterator<Annotation> it = model.getAnnotationIterator();

    while (it.hasNext()) {
      Annotation annotation = it.next();
      positions.add(model.getPosition(annotation));
    }

    return positions;
  }
}
