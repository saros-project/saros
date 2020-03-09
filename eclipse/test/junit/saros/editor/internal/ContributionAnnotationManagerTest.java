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
  private IAnnotationModel model;

  private Capture<ISessionListener> sessionListenerCapture;

  private static final int MAX_HISTORY_LENGTH = ContributionAnnotationManager.MAX_HISTORY_LENGTH;
  private static final User ALICE_TEST_USER =
      new User(new JID("ALICE_TEST_USER@test"), false, false, null);
  private static final User BOB_TEST_USER = new User(new JID("bob@test"), false, false, null);
  private static final User CARL_TEST_USER = new User(new JID("carl@test"), false, false, null);
  private static final User DAVE_TEST_USER = new User(new JID("dave@test"), false, false, null);

  @Before
  public void setUp() {
    store = new PreferenceStore();
    store.setValue(EclipsePreferenceConstants.SHOW_CONTRIBUTION_ANNOTATIONS, true);
    createListenerMocks();

    manager = new ContributionAnnotationManager(sessionMock, store);
    model = new AnnotationModel();
  }

  private void createListenerMocks() {
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
  }

  @Test
  public void testHistoryRemoval() {
    for (int i = 0; i <= MAX_HISTORY_LENGTH + 1; i++)
      manager.insertAnnotation(model, i, 1, ALICE_TEST_USER);

    assertEquals(MAX_HISTORY_LENGTH, getAnnotationCount(model));

    manager.insertAnnotation(model, MAX_HISTORY_LENGTH, 1, ALICE_TEST_USER);

    assertEquals(MAX_HISTORY_LENGTH, getAnnotationCount(model));

    assertFalse(
        "oldest annotation was not removed",
        getAnnotationPositions(model).contains(new Position(0, 1)));
  }

  @Test
  public void testHistoryRemovalAfterRefresh() {
    for (int i = 0; i <= MAX_HISTORY_LENGTH; i++)
      manager.insertAnnotation(model, i, 1, ALICE_TEST_USER);

    manager.refreshAnnotations(model);

    manager.insertAnnotation(model, MAX_HISTORY_LENGTH + 1, 1, ALICE_TEST_USER);

    assertFalse(
        "oldest annotation was not removed after refresh",
        getAnnotationPositions(model).contains(new Position(0, 1)));
  }

  @Test
  public void testHistoryRemovelWithLengthGreaterOne() {
    final int annotationLength = 3;

    for (int i = 0; i < MAX_HISTORY_LENGTH; i++) {
      manager.insertAnnotation(model, i * annotationLength, annotationLength, ALICE_TEST_USER);
    }

    assertEquals(
        "Inserted not as much annotations as expected",
        annotationLength * MAX_HISTORY_LENGTH,
        getAnnotationCount(model));

    manager.insertAnnotation(model, annotationLength * MAX_HISTORY_LENGTH + 1, 1, ALICE_TEST_USER);
    assertEquals(
        "Inserting an annotation of length 1 should have remove another annotation of length > 1",
        annotationLength * (MAX_HISTORY_LENGTH - 1) + 1,
        getAnnotationCount(model));
  }

  @Test
  public void testHistoryRemovelWithDifferentLengths() {
    final int annotationLength1 = 3;
    int offset = 0;
    final int numberOfEntries = 5;
    for (int i = 0; i < numberOfEntries; i++) {
      manager.insertAnnotation(model, offset, annotationLength1, ALICE_TEST_USER);
      offset += annotationLength1;
    }

    final int annotationLength2 = 5;

    for (int i = 0; i < numberOfEntries; i++) {
      manager.insertAnnotation(model, offset, annotationLength2, ALICE_TEST_USER);
      offset += annotationLength2;
    }

    final int annotationLength3 = 10;

    final int remainingNumberOfEntries = MAX_HISTORY_LENGTH - 2 * numberOfEntries;
    for (int i = 0; i < remainingNumberOfEntries; i++) {
      manager.insertAnnotation(model, offset, annotationLength3, ALICE_TEST_USER);
      offset += annotationLength3;
    }

    int expectedAnnotationCount =
        numberOfEntries * annotationLength1
            + numberOfEntries * annotationLength2
            + remainingNumberOfEntries * annotationLength3;
    assertEquals(expectedAnnotationCount, getAnnotationCount(model));

    final int newAnnotationLength = 1;
    for (int i = 0; i < numberOfEntries; i++) {
      manager.insertAnnotation(model, offset, newAnnotationLength, ALICE_TEST_USER);
      offset += annotationLength1;
    }

    expectedAnnotationCount =
        numberOfEntries * newAnnotationLength
            + numberOfEntries * annotationLength2
            + remainingNumberOfEntries * annotationLength3;
    assertEquals(expectedAnnotationCount, getAnnotationCount(model));
  }

  @Test
  public void testHistoryRemovalWithMultipleModels() {
    final AnnotationModel model2 = new AnnotationModel();
    final AnnotationModel model3 = new AnnotationModel();
    final int numberOfInsertions = 5;

    // Fill history
    for (int i = 0; i < numberOfInsertions; i++) {
      manager.insertAnnotation(model2, i, 1, ALICE_TEST_USER);
    }

    for (int i = 0; i < numberOfInsertions; i++) {
      manager.insertAnnotation(model3, i, 1, ALICE_TEST_USER);
    }

    int offset = 0;
    final int remainingInsertions = MAX_HISTORY_LENGTH - 2 * numberOfInsertions;
    for (int i = 0; i < remainingInsertions; i++, offset++) {
      manager.insertAnnotation(model, offset, 1, ALICE_TEST_USER);
    }

    assertEquals(remainingInsertions, getAnnotationCount(model));
    assertEquals(numberOfInsertions, getAnnotationCount(model2));
    assertEquals(numberOfInsertions, getAnnotationCount(model3));

    for (int i = 0; i < numberOfInsertions; i++, offset++) {
      manager.insertAnnotation(model, offset, 1, ALICE_TEST_USER);
    }

    assertEquals(remainingInsertions + numberOfInsertions, getAnnotationCount(model));
    assertEquals(
        "Insertions in another model should lead to removing all annotations in the model",
        0,
        getAnnotationCount(model2));
    assertEquals(numberOfInsertions, getAnnotationCount(model3));

    for (int i = 0; i < numberOfInsertions; i++, offset++) {
      manager.insertAnnotation(model, offset, 1, ALICE_TEST_USER);
    }

    assertEquals(remainingInsertions + 2 * numberOfInsertions, getAnnotationCount(model));
    assertEquals(0, getAnnotationCount(model2));
    assertEquals(
        "Insertions in another model should lead to removing all annotations in the model",
        0,
        getAnnotationCount(model3));
  }

  @Test
  public void testRemoveAllAnnotationsBySwitchingProperty() {

    final List<User> users =
        Arrays.asList(ALICE_TEST_USER, BOB_TEST_USER, CARL_TEST_USER, DAVE_TEST_USER);

    int idx = 0;

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

    users.add(ALICE_TEST_USER);
    users.add(BOB_TEST_USER);

    int idx = 0;

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

  @Test
  public void testInsertAnnotationWithLengthGreaterOne() {
    final int annotationLength = 3;

    manager.insertAnnotation(model, 5, annotationLength, ALICE_TEST_USER);

    final List<Position> annotationPositions = getAnnotationPositions(model);

    assertEquals(
        "Annotation was not split into multiple annotation of length 1",
        annotationLength,
        annotationPositions.size());
    assertTrue(annotationPositions.contains(new Position(5, 1)));
    assertTrue(annotationPositions.contains(new Position(6, 1)));
    assertTrue(annotationPositions.contains(new Position(7, 1)));
  }

  @Test
  public void testInsertAnnotationWithLengthZero() {
    manager.insertAnnotation(model, 3, 0, ALICE_TEST_USER);
    assertEquals("Annotation with length 0 was inserted", 0, getAnnotationCount(model));
  }

  public void testInsertWhileNotEnable() {
    store.setValue(EclipsePreferenceConstants.SHOW_CONTRIBUTION_ANNOTATIONS, false);

    manager.insertAnnotation(model, 5, 7, ALICE_TEST_USER);

    assertEquals(0, getAnnotationCount(model));
  }

  @Test
  public void testDispose() {

    final List<User> users = new ArrayList<>();

    users.add(ALICE_TEST_USER);
    users.add(BOB_TEST_USER);

    int idx = 0;

    for (final User user : users)
      for (int i = 0; i < MAX_HISTORY_LENGTH; i++, idx++)
        manager.insertAnnotation(model, idx, 1, user);

    assertEquals(MAX_HISTORY_LENGTH * users.size(), getAnnotationCount(model));

    manager.dispose();

    assertEquals(0, getAnnotationCount(model));
  }

  private int getAnnotationCount(final IAnnotationModel model) {
    int count = 0;

    final Iterator<Annotation> it = model.getAnnotationIterator();

    while (it.hasNext()) {
      count++;
      it.next();
    }

    return count;
  }

  private List<Position> getAnnotationPositions(final IAnnotationModel model) {

    final List<Position> positions = new ArrayList<Position>();

    final Iterator<Annotation> it = model.getAnnotationIterator();

    while (it.hasNext()) {
      final Annotation annotation = it.next();
      positions.add(model.getPosition(annotation));
    }

    return positions;
  }
}
