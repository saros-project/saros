package saros.editor.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.junit.Before;
import org.junit.Test;
import saros.editor.annotations.ContributionAnnotation;
import saros.net.xmpp.JID;
import saros.session.User;

public class ContributionAnnotationHistoryTest {
  private ContributionAnnotationHistory history;
  private AnnotationModel testModel;
  private static final int MAX_HISTORY_LENGTH = 20;

  @Before
  public void setUp() {
    history = new ContributionAnnotationHistory(MAX_HISTORY_LENGTH);
    testModel = new AnnotationModel();
  }

  @Test
  public void testRemoveHistoryEntriesWithEmptyHistory() {
    Pair<IAnnotationModel, List<ContributionAnnotation>> annotationsToRemove =
        history.removeHistoryEntry();

    assertNull(annotationsToRemove);
  }

  @Test
  public void testAddEntries() {
    final int nrOfEnries = 3;
    fillHistory(nrOfEnries, testModel);

    assertEquals(nrOfEnries, history.queue.size());
  }

  @Test
  public void testRemoveHistoryEntriesWithPartiallyFilledHistory() {
    final int nrOfEnries = 3;
    fillHistory(nrOfEnries, testModel);

    Pair<IAnnotationModel, List<ContributionAnnotation>> annotationsToRemove =
        history.removeHistoryEntry();

    assertNull(annotationsToRemove);
    assertEquals(nrOfEnries, history.queue.size());
  }

  @Test
  public void testRemoveHistoryEntriesWithFilledHistory() {
    final int nrOfEnries = MAX_HISTORY_LENGTH - 1;
    fillHistory(nrOfEnries, testModel);

    Pair<IAnnotationModel, List<ContributionAnnotation>> annotationsToRemove =
        history.removeHistoryEntry();

    assertNull(annotationsToRemove);
    assertEquals(nrOfEnries, history.queue.size());
  }

  @Test
  public void testRemoveHistoryEntriesWithOverfilledHistory() {
    final int nrOfEnries = MAX_HISTORY_LENGTH;
    fillHistory(nrOfEnries, testModel);

    Pair<IAnnotationModel, List<ContributionAnnotation>> annotationsToRemove =
        history.removeHistoryEntry();

    assertEquals(1, annotationsToRemove.getRight().size());
    assertEquals(MAX_HISTORY_LENGTH - 1, history.queue.size());
  }

  @Test
  public void testRemoveHistoryEntriesWithMultipleModels() {
    AnnotationModel testModel2 = new AnnotationModel();

    fillHistory(1, testModel2);
    fillHistory(MAX_HISTORY_LENGTH - 1, testModel);

    Pair<IAnnotationModel, List<ContributionAnnotation>> annotationsToRemove =
        history.removeHistoryEntry();

    assertEquals(testModel2, annotationsToRemove.getLeft());

    fillHistory(1, testModel);
    annotationsToRemove = history.removeHistoryEntry();

    assertEquals(testModel, annotationsToRemove.getLeft());
  }

  private void fillHistory(int numberOfEntries, AnnotationModel model) {
    for (int i = 0; i < numberOfEntries; i++) {
      history.addNewEntry(createDummyAnnotation(model));
    }
  }

  private List<ContributionAnnotation> createDummyAnnotation(AnnotationModel model) {
    User dummyUser = new User(new JID("alice@test"), false, false, null);
    ContributionAnnotation dummyAnnotation = new ContributionAnnotation(dummyUser, model);
    return Arrays.asList(dummyAnnotation);
  }
}
