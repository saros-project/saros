package de.fu_berlin.inf.dpp.editor.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.preferences.EclipsePreferenceConstants;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.test.util.EclipseMemoryPreferenceStore;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.easymock.EasyMock;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(User.class)
public class ContributionAnnotationManagerTest {

  private ContributionAnnotationManager manager;
  private ISarosSession sessionMock;
  private IPreferenceStore store;

  @Before
  public void setUp() {
    store = new EclipseMemoryPreferenceStore();
    store.setValue(EclipsePreferenceConstants.SHOW_CONTRIBUTION_ANNOTATIONS, true);

    sessionMock = EasyMock.createNiceMock(ISarosSession.class);
    PowerMock.replay(sessionMock);

    manager = new ContributionAnnotationManager(sessionMock, store);
  }

  @Test
  public void testHistoryRemoval() {

    User alice = new User(new JID("alice@test"), false, false, 0, 0);

    AnnotationModel model = new AnnotationModel();

    for (int i = 0; i <= ContributionAnnotationManager.MAX_HISTORY_LENGTH; i++)
      manager.insertAnnotation(model, i, 1, alice);

    assertEquals(ContributionAnnotationManager.MAX_HISTORY_LENGTH, getAnnotationCount(model));

    manager.insertAnnotation(model, ContributionAnnotationManager.MAX_HISTORY_LENGTH + 1, 1, alice);

    assertEquals(ContributionAnnotationManager.MAX_HISTORY_LENGTH, getAnnotationCount(model));

    assertFalse(
        "oldest annotation was not removed",
        getAnnotationPositions(model).contains(new Position(0, 1)));
  }

  @Test
  public void testHistoryRemovalAfterRefresh() {
    User alice = new User(new JID("alice@test"), false, false, 0, 0);

    AnnotationModel model = new AnnotationModel();

    for (int i = 0; i <= ContributionAnnotationManager.MAX_HISTORY_LENGTH; i++)
      manager.insertAnnotation(model, i, 1, alice);

    manager.refreshAnnotations(model);

    manager.insertAnnotation(model, ContributionAnnotationManager.MAX_HISTORY_LENGTH + 1, 1, alice);

    assertFalse(
        "oldest annotation was not removed after refresh",
        getAnnotationPositions(model).contains(new Position(0, 1)));
  }

  @Test
  public void testAnnotationSplit() {

    final User alice = new User(new JID("alice@test"), false, false, 0, 0);
    final User bob = new User(new JID("bob@test"), false, false, 0, 0);

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

  @SuppressWarnings("unchecked")
  private int getAnnotationCount(AnnotationModel model) {
    int count = 0;

    Iterator<Annotation> it = model.getAnnotationIterator();

    while (it.hasNext()) {
      count++;
      it.next();
    }

    return count;
  }

  @SuppressWarnings("unchecked")
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
