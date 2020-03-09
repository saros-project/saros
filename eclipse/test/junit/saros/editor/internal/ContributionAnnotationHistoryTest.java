package saros.editor.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

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
  public void testAddEntriesUntilMaxLength() {
    Pair<IAnnotationModel, List<ContributionAnnotation>> annotationsToRemove;

    for (int i = 0; i < MAX_HISTORY_LENGTH; i++) {
      annotationsToRemove = history.addNewEntry(createDummyAnnotation(testModel));
      assertNull(annotationsToRemove);
    }

    assertEquals(MAX_HISTORY_LENGTH, history.getSize());
  }

  @Test
  public void testAddEntryExceedingMaxLength() {
    Pair<IAnnotationModel, List<ContributionAnnotation>> annotationsToRemove;

    final List<ContributionAnnotation> firstEntry = createDummyAnnotation(testModel);
    annotationsToRemove = history.addNewEntry(firstEntry);
    assertNull(annotationsToRemove);

    for (int i = 1; i < MAX_HISTORY_LENGTH; i++) {
      annotationsToRemove = history.addNewEntry(createDummyAnnotation(testModel));
      assertNull(annotationsToRemove);
    }

    assertEquals(MAX_HISTORY_LENGTH, history.getSize());

    annotationsToRemove = history.addNewEntry(createDummyAnnotation(testModel));
    assertSame(annotationsToRemove.getRight(), firstEntry);
  }

  @Test
  public void testAddEntryWithMultipleModels() {
    final AnnotationModel testModel2 = new AnnotationModel();

    Pair<IAnnotationModel, List<ContributionAnnotation>> annotationsToRemove =
        history.addNewEntry(createDummyAnnotation(testModel2));
    assertNull(annotationsToRemove);

    for (int i = 1; i < MAX_HISTORY_LENGTH; i++) {
      annotationsToRemove = history.addNewEntry(createDummyAnnotation(testModel));
      assertNull(annotationsToRemove);
    }

    annotationsToRemove = history.addNewEntry(createDummyAnnotation(testModel));
    assertSame(testModel2, annotationsToRemove.getLeft());

    annotationsToRemove = history.addNewEntry(createDummyAnnotation(testModel));
    assertSame(testModel, annotationsToRemove.getLeft());
  }

  private List<ContributionAnnotation> createDummyAnnotation(final AnnotationModel model) {
    final User dummyUser = new User(new JID("alice@test"), false, false, null);
    final ContributionAnnotation dummyAnnotation = new ContributionAnnotation(dummyUser, model);
    return Arrays.asList(dummyAnnotation);
  }
}
