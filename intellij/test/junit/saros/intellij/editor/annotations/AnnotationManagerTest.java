package saros.intellij.editor.annotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static saros.intellij.editor.annotations.AnnotationManager.MAX_CONTRIBUTION_ANNOTATIONS;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.easymock.EasyMock;
import org.easymock.IExpectationSetters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import saros.filesystem.IFile;
import saros.intellij.editor.annotations.AnnotationManager.AnnotationType;
import saros.session.User;

/**
 * Tests the methods of {@link AnnotationManager}.
 *
 * <p>To reduce the mocking logic and make the tests easier to understand, all tests for methods
 * that do not care about whether the annotation has a local representation (editor & range
 * highlighters) or not work on annotations without a local representation.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({AnnotationManager.class, AnnotationStore.class, AnnotationQueue.class})
public class AnnotationManagerTest {

  /** Selection annotation store held in the annotation manager. */
  private AnnotationStore<SelectionAnnotation> selectionAnnotationStore;
  /** Contribution annotation store held in the annotation manager. */
  private AnnotationQueue<ContributionAnnotation> contributionAnnotationQueue;

  /**
   * Annotation manager used for testing. Works on {@link #selectionAnnotationStore} and {@link
   * #contributionAnnotationQueue}.
   */
  private AnnotationManager annotationManager;

  /** Mocked file to use when creating annotations. */
  private IFile file;
  /** Mocked user to use when creating annotations. */
  private User user;
  /** Mocked editor to use when creating annotations. */
  private Editor editor;

  @Before
  public void setUp() throws Exception {
    selectionAnnotationStore = new AnnotationStore<>();
    contributionAnnotationQueue = new AnnotationQueue<>(MAX_CONTRIBUTION_ANNOTATIONS);

    /*
     * Mock annotation store CTOR calls to return an object we hold a reference to.
     * This is necessary to access the inner state of the annotation manager for testing.
     */
    PowerMock.expectNew(AnnotationStore.class).andReturn(selectionAnnotationStore);
    PowerMock.expectNew(AnnotationQueue.class, MAX_CONTRIBUTION_ANNOTATIONS)
        .andReturn(contributionAnnotationQueue);

    PowerMock.replayAll();

    annotationManager = new AnnotationManager();

    file = EasyMock.createNiceMock(IFile.class);
    user = EasyMock.createNiceMock(User.class);
    editor = EasyMock.createNiceMock(Editor.class);
  }

  /** Test adding selection annotations without an editor. */
  @Test
  public void testAddSelectionAnnotationNoEditor() {
    /* setup */
    int start = 90;
    int end = 200;
    List<Pair<Integer, Integer>> expectedRange = createSelectionRange(start, end);

    assertTrue(selectionAnnotationStore.getAnnotations().isEmpty());

    /* call to test */
    annotationManager.addSelectionAnnotation(user, file, start, end, null);

    /* check assertions */
    List<SelectionAnnotation> selectionAnnotations = selectionAnnotationStore.getAnnotations();
    assertEquals(1, selectionAnnotations.size());

    SelectionAnnotation selectionAnnotation = selectionAnnotations.get(0);
    assertAnnotationIntegrity(selectionAnnotation, user, file, expectedRange, null);
  }

  /** Test adding selection annotations with an editor. */
  @Test
  public void testAddSelectionAnnotationEditor() throws Exception {
    /* setup */
    int start = 50;
    int end = 52;
    List<Pair<Integer, Integer>> expectedRange = createSelectionRange(start, end);

    prepareMockAddRemoveRangeHighlighters();
    mockAddRangeHighlighters(expectedRange, AnnotationType.SELECTION_ANNOTATION);
    replayMockAddRemoveRangeHighlighters();

    assertTrue(selectionAnnotationStore.getAnnotations().isEmpty());

    /* call to test */
    annotationManager.addSelectionAnnotation(user, file, start, end, editor);

    /* check assertions */
    List<SelectionAnnotation> selectionAnnotations = selectionAnnotationStore.getAnnotations();
    assertEquals(1, selectionAnnotations.size());

    SelectionAnnotation selectionAnnotation = selectionAnnotations.get(0);
    assertAnnotationIntegrity(selectionAnnotation, user, file, expectedRange, editor);
  }

  /**
   * Tests that adding a new selection annotation for a file removes the old selection annotation.
   */
  @Test
  public void testReplaceSelectionAnnotation() {
    /* setup */
    int start = 0;
    int end = 5;
    List<Pair<Integer, Integer>> expectedRange = createSelectionRange(start, end);

    assertTrue(selectionAnnotationStore.getAnnotations().isEmpty());

    /* call to test */
    annotationManager.addSelectionAnnotation(user, file, start, end, null);

    /* check assertions */
    List<SelectionAnnotation> selectionAnnotations = selectionAnnotationStore.getAnnotations();
    assertEquals(1, selectionAnnotations.size());

    SelectionAnnotation selectionAnnotation = selectionAnnotations.get(0);
    assertAnnotationIntegrity(selectionAnnotation, user, file, expectedRange, null);

    /* setup */
    start = 15;
    end = 22;
    expectedRange = createSelectionRange(start, end);

    /* call to test */
    annotationManager.addSelectionAnnotation(user, file, start, end, null);

    /* check assertions */
    selectionAnnotations = selectionAnnotationStore.getAnnotations();
    assertEquals(1, selectionAnnotations.size());

    selectionAnnotation = selectionAnnotations.get(0);
    assertAnnotationIntegrity(selectionAnnotation, user, file, expectedRange, null);
  }

  /**
   * Tests that adding a new selection annotation for a file removes the old selection annotation.
   * Checks whether the local representation of the removed annotation is also removed.
   */
  @Test
  public void testReplaceSelectionAnnotationEditor() throws Exception {
    /* setup */
    int start = 0;
    int end = 5;
    List<Pair<Integer, Integer>> expectedRange = createSelectionRange(start, end);

    SelectionAnnotation selectionAnnotation =
        new SelectionAnnotation(user, file, editor, createAnnotationRanges(expectedRange, true));
    selectionAnnotationStore.addAnnotation(selectionAnnotation);

    assertEquals(1, selectionAnnotationStore.getAnnotations().size());

    prepareMockAddRemoveRangeHighlighters();
    mockRemoveRangeHighlighters(selectionAnnotation);
    replayMockAddRemoveRangeHighlighters();

    start = 15;
    end = 22;
    expectedRange = createSelectionRange(start, end);

    /* call to test */
    annotationManager.addSelectionAnnotation(user, file, start, end, null);

    /* check assertions */
    verifyRemovalCall();

    List<SelectionAnnotation> selectionAnnotations = selectionAnnotationStore.getAnnotations();
    assertEquals(1, selectionAnnotations.size());

    SelectionAnnotation newSelectionAnnotation = selectionAnnotations.get(0);
    assertAnnotationIntegrity(newSelectionAnnotation, user, file, expectedRange, null);
  }

  /**
   * Tests that adding a new selection annotation for a file does not remove the selection
   * annotation for a different file.
   */
  @Test
  public void testDoNotReplaceSelectionAnnotation() {
    /* setup */
    User user1 = user;
    IFile file1 = file;
    User user2 = EasyMock.createNiceMock(User.class);
    IFile file2 = EasyMock.createNiceMock(IFile.class);

    int start1 = 76;
    int end1 = 333;
    List<Pair<Integer, Integer>> expectedRange1 = createSelectionRange(start1, end1);

    assertTrue(selectionAnnotationStore.getAnnotations().isEmpty());

    /* call to test */
    annotationManager.addSelectionAnnotation(user1, file1, start1, end1, null);

    /* check assertions */
    List<SelectionAnnotation> selectionAnnotations1 =
        selectionAnnotationStore.getAnnotations(file1);
    assertEquals(1, selectionAnnotations1.size());

    List<SelectionAnnotation> selectionAnnotations = selectionAnnotationStore.getAnnotations();
    assertEquals(selectionAnnotations1, selectionAnnotations);

    SelectionAnnotation selectionAnnotation1 = selectionAnnotations1.get(0);
    assertAnnotationIntegrity(selectionAnnotation1, user1, file1, expectedRange1, null);

    /* setup */
    int start2 = 12;
    int end2 = 13;
    List<Pair<Integer, Integer>> expectedRange2 = createSelectionRange(start2, end2);

    /* call to test */
    annotationManager.addSelectionAnnotation(user2, file2, start2, end2, null);

    /* check assertions */
    selectionAnnotations = selectionAnnotationStore.getAnnotations();
    assertEquals(2, selectionAnnotations.size());

    selectionAnnotations1 = selectionAnnotationStore.getAnnotations(file1);
    assertEquals(1, selectionAnnotations1.size());

    List<SelectionAnnotation> selectionAnnotations2 =
        selectionAnnotationStore.getAnnotations(file2);
    assertEquals(1, selectionAnnotations2.size());

    selectionAnnotation1 = selectionAnnotations1.get(0);
    assertAnnotationIntegrity(selectionAnnotation1, user1, file1, expectedRange1, null);

    SelectionAnnotation selectionAnnotation2 = selectionAnnotations2.get(0);
    assertAnnotationIntegrity(selectionAnnotation2, user2, file2, expectedRange2, null);
  }

  /**
   * Test adding selection annotations with a zero-width range. Such calls should not lead to an
   * annotation being added. If an old selection for the file is present, it should still be
   * removed.
   */
  @Test
  public void testAddSelectionAnnotationNoRange() {
    /* setup */
    int start = 0;
    int end = 0;

    assertEquals(0, selectionAnnotationStore.getAnnotations().size());

    /* call to test */
    annotationManager.addSelectionAnnotation(user, file, start, end, null);

    /* check assertions */
    List<SelectionAnnotation> selectionAnnotations = selectionAnnotationStore.getAnnotations();
    assertEquals(0, selectionAnnotations.size());

    /* setup */
    annotationManager.addSelectionAnnotation(user, file, 5, 10, null);
    assertEquals(1, selectionAnnotationStore.getAnnotations().size());

    /* call to test */
    annotationManager.addSelectionAnnotation(user, file, start, end, null);

    /* check assertions */
    selectionAnnotations = selectionAnnotationStore.getAnnotations();
    assertEquals(0, selectionAnnotations.size());
  }

  /**
   * Tests that removing selection annotations for a specific file user combination removes all
   * corresponding annotations and does not remove any annotations for a different file or user.
   */
  @Test
  public void testRemoveSelectionAnnotation() {
    /* setup */
    User user1 = user;
    IFile file1 = file;
    User user2 = EasyMock.createNiceMock(User.class);
    IFile file2 = EasyMock.createNiceMock(IFile.class);

    int start1 = 0;
    int end1 = 20;
    int start2 = 53;
    int end2 = 56;
    List<Pair<Integer, Integer>> expectedRange2 = createSelectionRange(start2, end2);

    annotationManager.addSelectionAnnotation(user1, file1, start1, end1, null);
    annotationManager.addSelectionAnnotation(user2, file2, start2, end2, null);

    assertEquals(2, selectionAnnotationStore.getAnnotations().size());

    /* call to test */
    annotationManager.removeSelectionAnnotation(user1, file1);

    /* check assertions */
    List<SelectionAnnotation> selectionAnnotations = selectionAnnotationStore.getAnnotations();
    assertEquals(1, selectionAnnotations.size());
    assertEquals(0, selectionAnnotationStore.getAnnotations(file1).size());
    assertEquals(1, selectionAnnotationStore.getAnnotations(file2).size());

    SelectionAnnotation selectionAnnotation = selectionAnnotations.get(0);
    assertAnnotationIntegrity(selectionAnnotation, user2, file2, expectedRange2, null);

    /* call to test */
    annotationManager.removeSelectionAnnotation(user2, file2);

    /* check assertions */
    selectionAnnotations = selectionAnnotationStore.getAnnotations();
    assertEquals(0, selectionAnnotations.size());

    assertEquals(0, selectionAnnotationStore.getAnnotations(file1).size());
    assertEquals(0, selectionAnnotationStore.getAnnotations(file2).size());
  }

  /**
   * Tests that removing a selection annotation for a file also removes the local representation of
   * the annotation.
   */
  @Test
  public void testRemoveSelectionAnnotationEditor() throws Exception {
    /* setup */
    int start = 0;
    int end = 5;
    List<Pair<Integer, Integer>> expectedRange = createSelectionRange(start, end);

    SelectionAnnotation selectionAnnotation =
        new SelectionAnnotation(user, file, editor, createAnnotationRanges(expectedRange, true));
    selectionAnnotationStore.addAnnotation(selectionAnnotation);

    assertEquals(1, selectionAnnotationStore.getAnnotations().size());

    prepareMockAddRemoveRangeHighlighters();
    mockRemoveRangeHighlighters(selectionAnnotation);
    replayMockAddRemoveRangeHighlighters();

    /* call to test */
    annotationManager.removeSelectionAnnotation(user, file);

    /* check assertions */
    verifyRemovalCall();

    List<SelectionAnnotation> selectionAnnotations = selectionAnnotationStore.getAnnotations();
    assertEquals(0, selectionAnnotations.size());
  }

  /** Test adding contribution annotations without an editor. */
  @Test
  public void testAddContributionAnnotationNoEditor() {
    /* setup */
    int start = 10;
    int end = 20;

    List<Pair<Integer, Integer>> expectedRanges = createContributionRanges(start, end);

    assertTrue(contributionAnnotationQueue.getAnnotations().isEmpty());

    /* call to test */
    annotationManager.addContributionAnnotation(user, file, start, end, null);

    /* check assertions */
    List<ContributionAnnotation> contributionAnnotations =
        contributionAnnotationQueue.getAnnotations();
    assertEquals(1, contributionAnnotations.size());

    ContributionAnnotation contributionAnnotation = contributionAnnotations.get(0);
    assertAnnotationIntegrity(contributionAnnotation, user, file, expectedRanges, null);
  }

  /** Test adding contribution annotations with an editor. */
  @Test
  public void testAddContributionAnnotationEditor() throws Exception {
    /* setup */
    int start = 0;
    int end = 21;

    List<Pair<Integer, Integer>> expectedRanges = createContributionRanges(start, end);

    prepareMockAddRemoveRangeHighlighters();
    mockAddRangeHighlighters(expectedRanges, AnnotationType.CONTRIBUTION_ANNOTATION);
    replayMockAddRemoveRangeHighlighters();

    assertTrue(contributionAnnotationQueue.getAnnotations().isEmpty());

    /* call to test */
    annotationManager.addContributionAnnotation(user, file, start, end, editor);

    /* check assertions */
    List<ContributionAnnotation> contributionAnnotations =
        contributionAnnotationQueue.getAnnotations();
    assertEquals(1, contributionAnnotations.size());

    ContributionAnnotation contributionAnnotation = contributionAnnotations.get(0);
    assertAnnotationIntegrity(contributionAnnotation, user, file, expectedRanges, editor);
  }

  /**
   * Test adding contribution annotations with a zero-width range. Such calls should not lead to an
   * annotation being added.
   */
  @Test
  public void testAddContributionAnnotationNoRange() {
    /* setup */
    int start = 10;
    int end = 10;

    assertTrue(contributionAnnotationQueue.getAnnotations().isEmpty());

    /* call to test */
    annotationManager.addContributionAnnotation(user, file, start, end, null);

    /* check assertions */
    List<ContributionAnnotation> contributionAnnotations =
        contributionAnnotationQueue.getAnnotations();
    assertEquals(0, contributionAnnotations.size());
  }

  /**
   * Tests the contribution annotation queue mechanism to rotate out old entries when new ones are
   * added and the store already holds {@link AnnotationManager#MAX_CONTRIBUTION_ANNOTATIONS}
   * annotations. This maximum is for the whole store and not per file.
   */
  @Test
  public void testContributionAnnotationQueueRotation() {
    User user2 = EasyMock.createNiceMock(User.class);
    IFile file2 = EasyMock.createNiceMock(IFile.class);

    List<ContributionAnnotation> previousAnnotations = new ArrayList<>();
    List<List<Pair<Integer, Integer>>> expectedRanges = new ArrayList<>();
    List<IFile> expectedFiles = new ArrayList<>();
    List<User> expectedUsers = new ArrayList<>();

    for (int i = 0; i < MAX_CONTRIBUTION_ANNOTATIONS; i++) {
      /* setup */
      int start = i + 1;
      int end = i + 30;
      List<Pair<Integer, Integer>> expectedRange = createContributionRanges(start, end);

      User usedUser;
      IFile usedFile;
      if (i % 2 == 0) {
        usedUser = user;
        usedFile = file;
      } else {
        usedUser = user2;
        usedFile = file2;
      }

      expectedRanges.add(expectedRange);
      expectedFiles.add(usedFile);
      expectedUsers.add(usedUser);

      /* call to test */
      annotationManager.addContributionAnnotation(usedUser, usedFile, start, end, null);

      /* check assertions */
      List<ContributionAnnotation> currentAnnotations =
          contributionAnnotationQueue.getAnnotations();
      assertTrue(currentAnnotations.containsAll(previousAnnotations));
      assertEquals(i + 1, currentAnnotations.size());

      currentAnnotations.removeAll(previousAnnotations);
      assertEquals(1, currentAnnotations.size());

      ContributionAnnotation addedAnnotation = currentAnnotations.get(0);
      assertAnnotationIntegrity(addedAnnotation, usedUser, usedFile, expectedRange, null);

      previousAnnotations.add(addedAnnotation);
    }

    /* check integrity of full list */
    List<ContributionAnnotation> annotations = contributionAnnotationQueue.getAnnotations();
    assertEquals(MAX_CONTRIBUTION_ANNOTATIONS, annotations.size());
    assertTrue(previousAnnotations.containsAll(annotations));
    assertTrue(annotations.containsAll(previousAnnotations));

    for (int i = 0; i < previousAnnotations.size(); i++) {
      assertAnnotationIntegrity(
          previousAnnotations.get(i),
          expectedUsers.get(i),
          expectedFiles.get(i),
          expectedRanges.get(i),
          null);
    }

    for (int i = MAX_CONTRIBUTION_ANNOTATIONS; i <= 2 * MAX_CONTRIBUTION_ANNOTATIONS; i++) {
      /* setup */
      int start = i + 1;
      int end = i + 30;

      User usedUser;
      IFile usedFile;
      if (i % 2 == 0) {
        usedUser = user;
        usedFile = file;
      } else {
        usedUser = user2;
        usedFile = file2;
      }

      expectedUsers.add(usedUser);
      expectedFiles.add(usedFile);
      expectedRanges.add(createContributionRanges(start, end));

      /* call to test */
      annotationManager.addContributionAnnotation(usedUser, usedFile, start, end, null);

      /* check assertions */
      List<ContributionAnnotation> currentAnnotations =
          contributionAnnotationQueue.getAnnotations();
      assertEquals(MAX_CONTRIBUTION_ANNOTATIONS, currentAnnotations.size());

      ContributionAnnotation rotatedOutAnnotation = previousAnnotations.remove(0);
      User rotatedOutExpectedUser = expectedUsers.remove(0);
      IFile rotatedOutExpectedFile = expectedFiles.remove(0);
      List<Pair<Integer, Integer>> rotatedOutExpectedRanges = expectedRanges.remove(0);

      assertAnnotationIntegrity(
          rotatedOutAnnotation,
          rotatedOutExpectedUser,
          rotatedOutExpectedFile,
          rotatedOutExpectedRanges,
          null);

      assertTrue(currentAnnotations.containsAll(previousAnnotations));
      assertFalse(currentAnnotations.contains(rotatedOutAnnotation));

      currentAnnotations.removeAll(previousAnnotations);
      assertEquals(1, currentAnnotations.size());

      previousAnnotations.add(currentAnnotations.get(0));
    }

    /* check integrity of full replaced list */
    annotations = contributionAnnotationQueue.getAnnotations();
    assertEquals(MAX_CONTRIBUTION_ANNOTATIONS, annotations.size());
    assertTrue(previousAnnotations.containsAll(annotations));
    assertTrue(annotations.containsAll(previousAnnotations));

    for (int i = 0; i < previousAnnotations.size(); i++) {
      assertAnnotationIntegrity(
          previousAnnotations.get(i),
          expectedUsers.get(i),
          expectedFiles.get(i),
          expectedRanges.get(i),
          null);
    }
  }

  /**
   * Tests that the contribution annotation queue mechanism to rotate out old entries also removes
   * the local representation of the removed annotations.
   */
  @Test
  public void testContributionAnnotationQueueRotationEditor() throws Exception {
    /* setup */
    List<Pair<Integer, Integer>> expectedRanges = createContributionRanges(100, 200);

    ContributionAnnotation contributionAnnotation =
        new ContributionAnnotation(
            user, file, editor, createAnnotationRanges(expectedRanges, true));
    contributionAnnotationQueue.addAnnotation(contributionAnnotation);

    ContributionAnnotation filler =
        new ContributionAnnotation(
            user, file, null, createAnnotationRanges(createContributionRanges(0, 1), false));

    for (int i = 0; i < MAX_CONTRIBUTION_ANNOTATIONS - 1; i++) {
      contributionAnnotationQueue.addAnnotation(filler);
    }

    List<ContributionAnnotation> contributionAnnotations =
        contributionAnnotationQueue.getAnnotations();
    assertTrue(contributionAnnotations.contains(contributionAnnotation));

    prepareMockAddRemoveRangeHighlighters();
    mockRemoveRangeHighlighters(contributionAnnotation);
    replayMockAddRemoveRangeHighlighters();

    /* call to test */
    annotationManager.addContributionAnnotation(user, file, 0, 1, null);

    /* check assertions */
    verifyRemovalCall();

    contributionAnnotations = contributionAnnotationQueue.getAnnotations();
    assertFalse(contributionAnnotations.contains(contributionAnnotation));
  }

  /**
   * Tests moving annotations without an editor in reaction to text being added to the file after
   * the annotation. The annotation position should not change.
   */
  @Test
  public void testMoveAnnotationsAfterAdditionNoEditorAfterAnnotation() {
    /* setup */
    int start = 40;
    int end = 50;
    List<Pair<Integer, Integer>> expectedSelectionRanges = createSelectionRange(start, end);
    List<Pair<Integer, Integer>> expectedContributionRanges = createContributionRanges(start, end);

    SelectionAnnotation selectionAnnotation =
        new SelectionAnnotation(
            user, file, null, createAnnotationRanges(expectedSelectionRanges, false));
    selectionAnnotationStore.addAnnotation(selectionAnnotation);

    ContributionAnnotation contributionAnnotation =
        new ContributionAnnotation(
            user, file, null, createAnnotationRanges(expectedContributionRanges, false));
    contributionAnnotationQueue.addAnnotation(contributionAnnotation);

    /* call to test */
    annotationManager.moveAnnotationsAfterAddition(file, 50, 55);

    /* check assertions */
    List<SelectionAnnotation> selectionAnnotations = selectionAnnotationStore.getAnnotations();
    assertEquals(1, selectionAnnotations.size());

    selectionAnnotation = selectionAnnotations.get(0);
    assertAnnotationIntegrity(selectionAnnotation, user, file, expectedSelectionRanges, null);

    List<ContributionAnnotation> contributionAnnotations =
        contributionAnnotationQueue.getAnnotations();
    assertEquals(1, contributionAnnotations.size());

    contributionAnnotation = contributionAnnotations.get(0);
    assertAnnotationIntegrity(contributionAnnotation, user, file, expectedContributionRanges, null);
  }

  /**
   * Tests moving annotations without an editor in reaction to text being added to the file before
   * the annotation. The annotation position should be shifted right by the addition length.
   */
  @Test
  public void testMoveAnnotationsAfterAdditionNoEditorBeforeAnnotation() {
    /* setup */
    int start = 40;
    int end = 50;
    List<Pair<Integer, Integer>> expectedSelectionRanges = createSelectionRange(start, end);
    List<Pair<Integer, Integer>> expectedContributionRanges = createContributionRanges(start, end);

    SelectionAnnotation selectionAnnotation =
        new SelectionAnnotation(
            user, file, null, createAnnotationRanges(expectedSelectionRanges, false));
    selectionAnnotationStore.addAnnotation(selectionAnnotation);

    ContributionAnnotation contributionAnnotation =
        new ContributionAnnotation(
            user, file, null, createAnnotationRanges(expectedContributionRanges, false));
    contributionAnnotationQueue.addAnnotation(contributionAnnotation);

    int additionStart = 2;
    int additionEnd = 15;
    int additionOffset = additionEnd - additionStart;

    /* call to test */
    annotationManager.moveAnnotationsAfterAddition(file, additionStart, additionEnd);

    /* check assertions */
    start += additionOffset;
    end += additionOffset;
    expectedSelectionRanges = createSelectionRange(start, end);
    expectedContributionRanges = createContributionRanges(start, end);

    List<SelectionAnnotation> selectionAnnotations = selectionAnnotationStore.getAnnotations();
    assertEquals(1, selectionAnnotations.size());

    selectionAnnotation = selectionAnnotations.get(0);
    assertAnnotationIntegrity(selectionAnnotation, user, file, expectedSelectionRanges, null);

    List<ContributionAnnotation> contributionAnnotations =
        contributionAnnotationQueue.getAnnotations();
    assertEquals(1, contributionAnnotations.size());

    contributionAnnotation = contributionAnnotations.get(0);
    assertAnnotationIntegrity(contributionAnnotation, user, file, expectedContributionRanges, null);
  }

  /**
   * Tests moving annotations without an editor in reaction to text being added to the file in the
   * annotation range. Selection annotations should be extended by the addition length. For
   * contribution annotations, the range highlighters after the addition point should be shifted
   * right by the addition length.
   */
  @SuppressWarnings("UnnecessaryLocalVariable")
  @Test
  public void testMoveAnnotationsAfterAdditionNoEditorInAnnotation() {
    /* setup */
    int start = 40;
    int end = 50;
    List<Pair<Integer, Integer>> expectedSelectionRanges = createSelectionRange(start, end);
    List<Pair<Integer, Integer>> expectedContributionRanges = createContributionRanges(start, end);

    SelectionAnnotation selectionAnnotation =
        new SelectionAnnotation(
            user, file, null, createAnnotationRanges(expectedSelectionRanges, false));
    selectionAnnotationStore.addAnnotation(selectionAnnotation);

    ContributionAnnotation contributionAnnotation =
        new ContributionAnnotation(
            user, file, null, createAnnotationRanges(expectedContributionRanges, false));
    contributionAnnotationQueue.addAnnotation(contributionAnnotation);

    int additionStart = 45;
    int additionEnd = 73;
    int additionOffset = additionEnd - additionStart;

    /* call to test */
    annotationManager.moveAnnotationsAfterAddition(file, additionStart, additionEnd);

    /* check assertions */
    int newSelectionStart = start;
    int newSelectionEnd = end + additionOffset;
    expectedSelectionRanges = createSelectionRange(newSelectionStart, newSelectionEnd);

    int newContributionStartFirstHalve = start;
    int newContributionEndFirstHalve = additionStart;
    List<Pair<Integer, Integer>> rangesFirstHalve =
        createContributionRanges(newContributionStartFirstHalve, newContributionEndFirstHalve);
    int newContributionStarSecondHalve = additionEnd;
    int newContributionEndSecondHalve = additionEnd + end - additionStart;
    List<Pair<Integer, Integer>> rangesSecondHalve =
        createContributionRanges(newContributionStarSecondHalve, newContributionEndSecondHalve);

    expectedContributionRanges = new ArrayList<>();
    expectedContributionRanges.addAll(rangesFirstHalve);
    expectedContributionRanges.addAll(rangesSecondHalve);

    List<SelectionAnnotation> selectionAnnotations = selectionAnnotationStore.getAnnotations();
    assertEquals(1, selectionAnnotations.size());

    selectionAnnotation = selectionAnnotations.get(0);
    assertAnnotationIntegrity(selectionAnnotation, user, file, expectedSelectionRanges, null);

    List<ContributionAnnotation> contributionAnnotations =
        contributionAnnotationQueue.getAnnotations();
    assertEquals(1, contributionAnnotations.size());

    contributionAnnotation = contributionAnnotations.get(0);
    assertAnnotationIntegrity(contributionAnnotation, user, file, expectedContributionRanges, null);
  }

  /**
   * Tests moving annotations with an editor in reaction to text being added to the file before, in,
   * or after the annotation. The annotation position should not be adjusted in any case.
   */
  @Test
  public void testMoveAnnotationsAfterAdditionEditor() {
    /* setup */
    int start = 40;
    int end = 50;
    List<Pair<Integer, Integer>> expectedSelectionRanges = createSelectionRange(start, end);
    List<Pair<Integer, Integer>> expectedContributionRanges = createContributionRanges(start, end);

    SelectionAnnotation selectionAnnotation =
        new SelectionAnnotation(
            user, file, editor, createAnnotationRanges(expectedSelectionRanges, true));
    selectionAnnotationStore.addAnnotation(selectionAnnotation);

    ContributionAnnotation contributionAnnotation =
        new ContributionAnnotation(
            user, file, editor, createAnnotationRanges(expectedContributionRanges, true));
    contributionAnnotationQueue.addAnnotation(contributionAnnotation);

    int additionStartBefore = 50;
    int additionEndBefore = 55;

    /* call to test */
    annotationManager.moveAnnotationsAfterAddition(file, additionStartBefore, additionEndBefore);

    /* check assertions */
    List<SelectionAnnotation> selectionAnnotations = selectionAnnotationStore.getAnnotations();
    assertEquals(1, selectionAnnotations.size());

    selectionAnnotation = selectionAnnotations.get(0);
    assertAnnotationIntegrity(selectionAnnotation, user, file, expectedSelectionRanges, editor);

    List<ContributionAnnotation> contributionAnnotations =
        contributionAnnotationQueue.getAnnotations();
    assertEquals(1, contributionAnnotations.size());

    contributionAnnotation = contributionAnnotations.get(0);
    assertAnnotationIntegrity(
        contributionAnnotation, user, file, expectedContributionRanges, editor);

    /* setup*/
    int additionStartAfter = 2;
    int additionEndAfter = 15;

    /* call to test */
    annotationManager.moveAnnotationsAfterAddition(file, additionStartAfter, additionEndAfter);

    /* check assertions */
    selectionAnnotations = selectionAnnotationStore.getAnnotations();
    assertEquals(1, selectionAnnotations.size());

    selectionAnnotation = selectionAnnotations.get(0);
    assertAnnotationIntegrity(selectionAnnotation, user, file, expectedSelectionRanges, editor);

    contributionAnnotations = contributionAnnotationQueue.getAnnotations();
    assertEquals(1, contributionAnnotations.size());

    contributionAnnotation = contributionAnnotations.get(0);
    assertAnnotationIntegrity(
        contributionAnnotation, user, file, expectedContributionRanges, editor);

    /* setup*/
    int additionStartIn = 45;
    int additionEndIn = 73;

    /* call to test */
    annotationManager.moveAnnotationsAfterAddition(file, additionStartIn, additionEndIn);

    /* check assertions */
    selectionAnnotations = selectionAnnotationStore.getAnnotations();
    assertEquals(1, selectionAnnotations.size());

    selectionAnnotation = selectionAnnotations.get(0);
    assertAnnotationIntegrity(selectionAnnotation, user, file, expectedSelectionRanges, editor);

    contributionAnnotations = contributionAnnotationQueue.getAnnotations();
    assertEquals(1, contributionAnnotations.size());

    contributionAnnotation = contributionAnnotations.get(0);
    assertAnnotationIntegrity(
        contributionAnnotation, user, file, expectedContributionRanges, editor);
  }

  /**
   * Tests moving annotations without an editor in reaction to text being added to a different file
   * before, in, or after the annotation position. The annotation position should not be adjusted in
   * any case.
   */
  @Test
  public void testMoveAnnotationsAfterAdditionDifferentFile() {
    /* setup */
    IFile file2 = EasyMock.createNiceMock(IFile.class);

    int start = 40;
    int end = 50;
    List<Pair<Integer, Integer>> expectedSelectionRanges = createSelectionRange(start, end);
    List<Pair<Integer, Integer>> expectedContributionRanges = createContributionRanges(start, end);

    SelectionAnnotation selectionAnnotation =
        new SelectionAnnotation(
            user, file, null, createAnnotationRanges(expectedSelectionRanges, false));
    selectionAnnotationStore.addAnnotation(selectionAnnotation);

    ContributionAnnotation contributionAnnotation =
        new ContributionAnnotation(
            user, file, null, createAnnotationRanges(expectedContributionRanges, false));
    contributionAnnotationQueue.addAnnotation(contributionAnnotation);

    int additionStartBefore = 50;
    int additionEndBefore = 55;

    /* call to test */
    annotationManager.moveAnnotationsAfterAddition(file2, additionStartBefore, additionEndBefore);

    /* check assertions */
    List<SelectionAnnotation> selectionAnnotations = selectionAnnotationStore.getAnnotations();
    assertEquals(1, selectionAnnotations.size());

    selectionAnnotation = selectionAnnotations.get(0);
    assertAnnotationIntegrity(selectionAnnotation, user, file, expectedSelectionRanges, null);

    List<ContributionAnnotation> contributionAnnotations =
        contributionAnnotationQueue.getAnnotations();
    assertEquals(1, contributionAnnotations.size());

    contributionAnnotation = contributionAnnotations.get(0);
    assertAnnotationIntegrity(contributionAnnotation, user, file, expectedContributionRanges, null);

    /* setup*/
    int additionStartAfter = 2;
    int additionEndAfter = 15;

    /* call to test */
    annotationManager.moveAnnotationsAfterAddition(file2, additionStartAfter, additionEndAfter);

    /* check assertions */
    selectionAnnotations = selectionAnnotationStore.getAnnotations();
    assertEquals(1, selectionAnnotations.size());

    selectionAnnotation = selectionAnnotations.get(0);
    assertAnnotationIntegrity(selectionAnnotation, user, file, expectedSelectionRanges, null);

    contributionAnnotations = contributionAnnotationQueue.getAnnotations();
    assertEquals(1, contributionAnnotations.size());

    contributionAnnotation = contributionAnnotations.get(0);
    assertAnnotationIntegrity(contributionAnnotation, user, file, expectedContributionRanges, null);

    /* setup*/
    int additionStartIn = 45;
    int additionEndIn = 73;

    /* call to test */
    annotationManager.moveAnnotationsAfterAddition(file2, additionStartIn, additionEndIn);

    /* check assertions */
    selectionAnnotations = selectionAnnotationStore.getAnnotations();
    assertEquals(1, selectionAnnotations.size());

    selectionAnnotation = selectionAnnotations.get(0);
    assertAnnotationIntegrity(selectionAnnotation, user, file, expectedSelectionRanges, null);

    contributionAnnotations = contributionAnnotationQueue.getAnnotations();
    assertEquals(1, contributionAnnotations.size());

    contributionAnnotation = contributionAnnotations.get(0);
    assertAnnotationIntegrity(contributionAnnotation, user, file, expectedContributionRanges, null);
  }

  /**
   * Tests moving annotations without an editor in reaction to text being removed from the file
   * after the annotation. The annotation position should not change.
   */
  @Test
  public void testMoveAnnotationsAfterDeletionNoEditorAfterAnnotation() {
    /* setup */
    int start = 40;
    int end = 50;
    List<Pair<Integer, Integer>> expectedSelectionRanges = createSelectionRange(start, end);
    List<Pair<Integer, Integer>> expectedContributionRanges = createContributionRanges(start, end);

    SelectionAnnotation selectionAnnotation =
        new SelectionAnnotation(
            user, file, null, createAnnotationRanges(expectedSelectionRanges, false));
    selectionAnnotationStore.addAnnotation(selectionAnnotation);

    ContributionAnnotation contributionAnnotation =
        new ContributionAnnotation(
            user, file, null, createAnnotationRanges(expectedContributionRanges, false));
    contributionAnnotationQueue.addAnnotation(contributionAnnotation);

    /* call to test */
    annotationManager.moveAnnotationsAfterDeletion(file, 50, 55);

    /* check assertions */
    List<SelectionAnnotation> selectionAnnotations = selectionAnnotationStore.getAnnotations();
    assertEquals(1, selectionAnnotations.size());

    selectionAnnotation = selectionAnnotations.get(0);
    assertAnnotationIntegrity(selectionAnnotation, user, file, expectedSelectionRanges, null);

    List<ContributionAnnotation> contributionAnnotations =
        contributionAnnotationQueue.getAnnotations();
    assertEquals(1, contributionAnnotations.size());

    contributionAnnotation = contributionAnnotations.get(0);
    assertAnnotationIntegrity(contributionAnnotation, user, file, expectedContributionRanges, null);
  }

  /**
   * Tests moving annotations without an editor in reaction to text being removed from the file
   * before the annotation. The annotation position should be shifted left by the deletion length.
   */
  @Test
  public void testMoveAnnotationsAfterDeletionNoEditorBeforeAnnotation() {
    /* setup */
    int start = 40;
    int end = 50;
    List<Pair<Integer, Integer>> expectedSelectionRanges = createSelectionRange(start, end);
    List<Pair<Integer, Integer>> expectedContributionRanges = createContributionRanges(start, end);

    SelectionAnnotation selectionAnnotation =
        new SelectionAnnotation(
            user, file, null, createAnnotationRanges(expectedSelectionRanges, false));
    selectionAnnotationStore.addAnnotation(selectionAnnotation);

    ContributionAnnotation contributionAnnotation =
        new ContributionAnnotation(
            user, file, null, createAnnotationRanges(expectedContributionRanges, false));
    contributionAnnotationQueue.addAnnotation(contributionAnnotation);

    int deletionStart = 2;
    int deletionEnd = 15;
    int deletionOffset = deletionEnd - deletionStart;

    /* call to test */
    annotationManager.moveAnnotationsAfterDeletion(file, deletionStart, deletionEnd);

    /* check assertions */
    start -= deletionOffset;
    end -= deletionOffset;
    expectedSelectionRanges = createSelectionRange(start, end);
    expectedContributionRanges = createContributionRanges(start, end);

    List<SelectionAnnotation> selectionAnnotations = selectionAnnotationStore.getAnnotations();
    assertEquals(1, selectionAnnotations.size());

    selectionAnnotation = selectionAnnotations.get(0);
    assertAnnotationIntegrity(selectionAnnotation, user, file, expectedSelectionRanges, null);

    List<ContributionAnnotation> contributionAnnotations =
        contributionAnnotationQueue.getAnnotations();
    assertEquals(1, contributionAnnotations.size());

    contributionAnnotation = contributionAnnotations.get(0);
    assertAnnotationIntegrity(contributionAnnotation, user, file, expectedContributionRanges, null);
  }

  /**
   * Tests moving annotations without an editor in reaction to text being removed from the file
   * overlapping with the start of the the annotation. The annotation position should be shifted
   * left to the deletion start and the range should be shortened by the overlap.
   */
  @Test
  public void testMoveAnnotationsAfterDeletionNoEditorLeadingOverlap() {
    /* setup */
    int start = 40;
    int end = 50;
    List<Pair<Integer, Integer>> expectedSelectionRanges = createSelectionRange(start, end);
    List<Pair<Integer, Integer>> expectedContributionRanges = createContributionRanges(start, end);

    SelectionAnnotation selectionAnnotation =
        new SelectionAnnotation(
            user, file, null, createAnnotationRanges(expectedSelectionRanges, false));
    selectionAnnotationStore.addAnnotation(selectionAnnotation);

    ContributionAnnotation contributionAnnotation =
        new ContributionAnnotation(
            user, file, null, createAnnotationRanges(expectedContributionRanges, false));
    contributionAnnotationQueue.addAnnotation(contributionAnnotation);

    int deletionStart = 33;
    int deletionEnd = 42;
    int deletionOffset = deletionEnd - deletionStart;

    /* call to test */
    annotationManager.moveAnnotationsAfterDeletion(file, deletionStart, deletionEnd);

    /* check assertions */
    start = deletionStart;
    end -= deletionOffset;
    expectedSelectionRanges = createSelectionRange(start, end);
    expectedContributionRanges = createContributionRanges(start, end);

    List<SelectionAnnotation> selectionAnnotations = selectionAnnotationStore.getAnnotations();
    assertEquals(1, selectionAnnotations.size());

    selectionAnnotation = selectionAnnotations.get(0);
    assertAnnotationIntegrity(selectionAnnotation, user, file, expectedSelectionRanges, null);

    List<ContributionAnnotation> contributionAnnotations =
        contributionAnnotationQueue.getAnnotations();
    assertEquals(1, contributionAnnotations.size());

    contributionAnnotation = contributionAnnotations.get(0);
    assertAnnotationIntegrity(contributionAnnotation, user, file, expectedContributionRanges, null);
  }

  /**
   * Tests moving annotations without an editor in reaction to text being removed from the file in
   * the annotation range. The annotation range should be shortened by the deletion length.
   */
  @Test
  public void testMoveAnnotationsAfterDeletionNoEditorInAnnotation() {
    /* setup */
    int start = 40;
    int end = 50;
    List<Pair<Integer, Integer>> expectedSelectionRanges = createSelectionRange(start, end);
    List<Pair<Integer, Integer>> expectedContributionRanges = createContributionRanges(start, end);

    SelectionAnnotation selectionAnnotation =
        new SelectionAnnotation(
            user, file, null, createAnnotationRanges(expectedSelectionRanges, false));
    selectionAnnotationStore.addAnnotation(selectionAnnotation);

    ContributionAnnotation contributionAnnotation =
        new ContributionAnnotation(
            user, file, null, createAnnotationRanges(expectedContributionRanges, false));
    contributionAnnotationQueue.addAnnotation(contributionAnnotation);

    int deletionStart = 45;
    int deletionEnd = 49;
    int deletionOffset = deletionEnd - deletionStart;

    /* call to test */
    annotationManager.moveAnnotationsAfterDeletion(file, deletionStart, deletionEnd);

    /* check assertions */
    end -= deletionOffset;
    expectedSelectionRanges = createSelectionRange(start, end);
    expectedContributionRanges = createContributionRanges(start, end);

    List<SelectionAnnotation> selectionAnnotations = selectionAnnotationStore.getAnnotations();
    assertEquals(1, selectionAnnotations.size());

    selectionAnnotation = selectionAnnotations.get(0);
    assertAnnotationIntegrity(selectionAnnotation, user, file, expectedSelectionRanges, null);

    List<ContributionAnnotation> contributionAnnotations =
        contributionAnnotationQueue.getAnnotations();
    assertEquals(1, contributionAnnotations.size());

    contributionAnnotation = contributionAnnotations.get(0);
    assertAnnotationIntegrity(contributionAnnotation, user, file, expectedContributionRanges, null);
  }

  /**
   * Tests moving annotations without an editor in reaction to text being removed from the file
   * overlapping with the end of the annotation. The annotation range should be shortened to match
   * the deletion start.
   */
  @Test
  public void testMoveAnnotationsAfterDeletionNoEditorTrailingOverlap() {
    /* setup */
    int start = 40;
    int end = 50;
    List<Pair<Integer, Integer>> expectedSelectionRanges = createSelectionRange(start, end);
    List<Pair<Integer, Integer>> expectedContributionRanges = createContributionRanges(start, end);

    SelectionAnnotation selectionAnnotation =
        new SelectionAnnotation(
            user, file, null, createAnnotationRanges(expectedSelectionRanges, false));
    selectionAnnotationStore.addAnnotation(selectionAnnotation);

    ContributionAnnotation contributionAnnotation =
        new ContributionAnnotation(
            user, file, null, createAnnotationRanges(expectedContributionRanges, false));
    contributionAnnotationQueue.addAnnotation(contributionAnnotation);

    int deletionStart = 48;
    int deletionEnd = 63;

    /* call to test */
    annotationManager.moveAnnotationsAfterDeletion(file, deletionStart, deletionEnd);

    /* check assertions */
    end = deletionStart;
    expectedSelectionRanges = createSelectionRange(start, end);
    expectedContributionRanges = createContributionRanges(start, end);

    List<SelectionAnnotation> selectionAnnotations = selectionAnnotationStore.getAnnotations();
    assertEquals(1, selectionAnnotations.size());

    selectionAnnotation = selectionAnnotations.get(0);
    assertAnnotationIntegrity(selectionAnnotation, user, file, expectedSelectionRanges, null);

    List<ContributionAnnotation> contributionAnnotations =
        contributionAnnotationQueue.getAnnotations();
    assertEquals(1, contributionAnnotations.size());

    contributionAnnotation = contributionAnnotations.get(0);
    assertAnnotationIntegrity(contributionAnnotation, user, file, expectedContributionRanges, null);
  }

  /**
   * Tests moving annotations without an editor in reaction to text being removed from the file
   * covering the annotation. The annotation should be removed from the annotation store.
   */
  @Test
  public void testMoveAnnotationsAfterDeletionNoEditorCoveringAnnotation() {
    /* setup */
    int start = 40;
    int end = 50;
    List<Pair<Integer, Integer>> expectedSelectionRanges = createSelectionRange(start, end);
    List<Pair<Integer, Integer>> expectedContributionRanges = createContributionRanges(start, end);

    SelectionAnnotation selectionAnnotation =
        new SelectionAnnotation(
            user, file, null, createAnnotationRanges(expectedSelectionRanges, false));
    selectionAnnotationStore.addAnnotation(selectionAnnotation);

    ContributionAnnotation contributionAnnotation =
        new ContributionAnnotation(
            user, file, null, createAnnotationRanges(expectedContributionRanges, false));
    contributionAnnotationQueue.addAnnotation(contributionAnnotation);

    int deletionStart = 30;
    int deletionEnd = 60;

    /* call to test */
    annotationManager.moveAnnotationsAfterDeletion(file, deletionStart, deletionEnd);

    /* check assertions */
    List<SelectionAnnotation> selectionAnnotations = selectionAnnotationStore.getAnnotations();
    assertEquals(0, selectionAnnotations.size());

    List<ContributionAnnotation> contributionAnnotations =
        contributionAnnotationQueue.getAnnotations();
    assertEquals(0, contributionAnnotations.size());
  }

  /**
   * Tests moving annotations with an editor in reaction to text being removed from the file. The
   * annotation position should not change in any case.
   */
  @Test
  public void testMoveAnnotationsAfterDeletionEditor() {
    /* setup */
    int start = 40;
    int end = 50;
    List<Pair<Integer, Integer>> expectedSelectionRanges = createSelectionRange(start, end);
    List<Pair<Integer, Integer>> expectedContributionRanges = createContributionRanges(start, end);

    SelectionAnnotation selectionAnnotation =
        new SelectionAnnotation(
            user, file, editor, createAnnotationRanges(expectedSelectionRanges, true));
    selectionAnnotationStore.addAnnotation(selectionAnnotation);

    ContributionAnnotation contributionAnnotation =
        new ContributionAnnotation(
            user, file, editor, createAnnotationRanges(expectedContributionRanges, true));
    contributionAnnotationQueue.addAnnotation(contributionAnnotation);

    int deletionStartBefore = 50;
    int deletionEndBefore = 55;

    /* call to test */
    annotationManager.moveAnnotationsAfterDeletion(file, deletionStartBefore, deletionEndBefore);

    /* check assertions */
    List<SelectionAnnotation> selectionAnnotations = selectionAnnotationStore.getAnnotations();
    assertEquals(1, selectionAnnotations.size());

    selectionAnnotation = selectionAnnotations.get(0);
    assertAnnotationIntegrity(selectionAnnotation, user, file, expectedSelectionRanges, editor);

    List<ContributionAnnotation> contributionAnnotations =
        contributionAnnotationQueue.getAnnotations();
    assertEquals(1, contributionAnnotations.size());

    contributionAnnotation = contributionAnnotations.get(0);
    assertAnnotationIntegrity(
        contributionAnnotation, user, file, expectedContributionRanges, editor);

    /* setup*/
    deletionStartBefore = 33;
    int deletionEndIn = 45;

    /* call to test */
    annotationManager.moveAnnotationsAfterDeletion(file, deletionStartBefore, deletionEndIn);

    /* check assertions */
    selectionAnnotations = selectionAnnotationStore.getAnnotations();
    assertEquals(1, selectionAnnotations.size());

    selectionAnnotation = selectionAnnotations.get(0);
    assertAnnotationIntegrity(selectionAnnotation, user, file, expectedSelectionRanges, editor);

    contributionAnnotations = contributionAnnotationQueue.getAnnotations();
    assertEquals(1, contributionAnnotations.size());

    contributionAnnotation = contributionAnnotations.get(0);
    assertAnnotationIntegrity(
        contributionAnnotation, user, file, expectedContributionRanges, editor);

    /* setup*/
    int deletionStartIn = 45;
    deletionEndIn = 49;

    /* call to test */
    annotationManager.moveAnnotationsAfterDeletion(file, deletionStartIn, deletionEndIn);

    /* check assertions */
    selectionAnnotations = selectionAnnotationStore.getAnnotations();
    assertEquals(1, selectionAnnotations.size());

    selectionAnnotation = selectionAnnotations.get(0);
    assertAnnotationIntegrity(selectionAnnotation, user, file, expectedSelectionRanges, editor);

    contributionAnnotations = contributionAnnotationQueue.getAnnotations();
    assertEquals(1, contributionAnnotations.size());

    contributionAnnotation = contributionAnnotations.get(0);
    assertAnnotationIntegrity(
        contributionAnnotation, user, file, expectedContributionRanges, editor);

    /* setup*/
    deletionStartIn = 48;
    int deletionEndAfter = 63;

    /* call to test */
    annotationManager.moveAnnotationsAfterDeletion(file, deletionStartIn, deletionEndAfter);

    /* check assertions */
    selectionAnnotations = selectionAnnotationStore.getAnnotations();
    assertEquals(1, selectionAnnotations.size());

    selectionAnnotation = selectionAnnotations.get(0);
    assertAnnotationIntegrity(selectionAnnotation, user, file, expectedSelectionRanges, editor);

    contributionAnnotations = contributionAnnotationQueue.getAnnotations();
    assertEquals(1, contributionAnnotations.size());

    contributionAnnotation = contributionAnnotations.get(0);
    assertAnnotationIntegrity(
        contributionAnnotation, user, file, expectedContributionRanges, editor);

    /* setup*/
    int deletionStartAfter = 50;
    deletionEndAfter = 55;

    /* call to test */
    annotationManager.moveAnnotationsAfterDeletion(file, deletionStartAfter, deletionEndAfter);

    /* check assertions */
    selectionAnnotations = selectionAnnotationStore.getAnnotations();
    assertEquals(1, selectionAnnotations.size());

    selectionAnnotation = selectionAnnotations.get(0);
    assertAnnotationIntegrity(selectionAnnotation, user, file, expectedSelectionRanges, editor);

    contributionAnnotations = contributionAnnotationQueue.getAnnotations();
    assertEquals(1, contributionAnnotations.size());

    contributionAnnotation = contributionAnnotations.get(0);
    assertAnnotationIntegrity(
        contributionAnnotation, user, file, expectedContributionRanges, editor);

    /* setup*/
    deletionStartBefore = 30;
    deletionEndAfter = 60;

    /* call to test */
    annotationManager.moveAnnotationsAfterDeletion(file, deletionStartBefore, deletionEndAfter);

    /* check assertions */
    selectionAnnotations = selectionAnnotationStore.getAnnotations();
    assertEquals(1, selectionAnnotations.size());

    selectionAnnotation = selectionAnnotations.get(0);
    assertAnnotationIntegrity(selectionAnnotation, user, file, expectedSelectionRanges, editor);

    contributionAnnotations = contributionAnnotationQueue.getAnnotations();
    assertEquals(1, contributionAnnotations.size());

    contributionAnnotation = contributionAnnotations.get(0);
    assertAnnotationIntegrity(
        contributionAnnotation, user, file, expectedContributionRanges, editor);
  }

  /**
   * Tests moving annotations without an editor in reaction to text being removed from a different
   * file. The annotation position should not change in any case.
   */
  @Test
  public void testMoveAnnotationsAfterDeletionDifferentFile() {
    /* setup */
    IFile file2 = EasyMock.createNiceMock(IFile.class);

    int start = 40;
    int end = 50;
    List<Pair<Integer, Integer>> expectedSelectionRanges = createSelectionRange(start, end);
    List<Pair<Integer, Integer>> expectedContributionRanges = createContributionRanges(start, end);

    SelectionAnnotation selectionAnnotation =
        new SelectionAnnotation(
            user, file, null, createAnnotationRanges(expectedSelectionRanges, false));
    selectionAnnotationStore.addAnnotation(selectionAnnotation);

    ContributionAnnotation contributionAnnotation =
        new ContributionAnnotation(
            user, file, null, createAnnotationRanges(expectedContributionRanges, false));
    contributionAnnotationQueue.addAnnotation(contributionAnnotation);

    int deletionStartBefore = 50;
    int deletionEndBefore = 55;

    /* call to test */
    annotationManager.moveAnnotationsAfterDeletion(file2, deletionStartBefore, deletionEndBefore);

    /* check assertions */
    List<SelectionAnnotation> selectionAnnotations = selectionAnnotationStore.getAnnotations();
    assertEquals(1, selectionAnnotations.size());

    selectionAnnotation = selectionAnnotations.get(0);
    assertAnnotationIntegrity(selectionAnnotation, user, file, expectedSelectionRanges, null);

    List<ContributionAnnotation> contributionAnnotations =
        contributionAnnotationQueue.getAnnotations();
    assertEquals(1, contributionAnnotations.size());

    contributionAnnotation = contributionAnnotations.get(0);
    assertAnnotationIntegrity(contributionAnnotation, user, file, expectedContributionRanges, null);

    /* setup*/
    deletionStartBefore = 33;
    int deletionEndIn = 45;

    /* call to test */
    annotationManager.moveAnnotationsAfterDeletion(file2, deletionStartBefore, deletionEndIn);

    /* check assertions */
    selectionAnnotations = selectionAnnotationStore.getAnnotations();
    assertEquals(1, selectionAnnotations.size());

    selectionAnnotation = selectionAnnotations.get(0);
    assertAnnotationIntegrity(selectionAnnotation, user, file, expectedSelectionRanges, null);

    contributionAnnotations = contributionAnnotationQueue.getAnnotations();
    assertEquals(1, contributionAnnotations.size());

    contributionAnnotation = contributionAnnotations.get(0);
    assertAnnotationIntegrity(contributionAnnotation, user, file, expectedContributionRanges, null);

    /* setup*/
    int deletionStartIn = 45;
    deletionEndIn = 49;

    /* call to test */
    annotationManager.moveAnnotationsAfterDeletion(file2, deletionStartIn, deletionEndIn);

    /* check assertions */
    selectionAnnotations = selectionAnnotationStore.getAnnotations();
    assertEquals(1, selectionAnnotations.size());

    selectionAnnotation = selectionAnnotations.get(0);
    assertAnnotationIntegrity(selectionAnnotation, user, file, expectedSelectionRanges, null);

    contributionAnnotations = contributionAnnotationQueue.getAnnotations();
    assertEquals(1, contributionAnnotations.size());

    contributionAnnotation = contributionAnnotations.get(0);
    assertAnnotationIntegrity(contributionAnnotation, user, file, expectedContributionRanges, null);

    /* setup*/
    deletionStartIn = 48;
    int deletionEndAfter = 63;

    /* call to test */
    annotationManager.moveAnnotationsAfterDeletion(file2, deletionStartIn, deletionEndAfter);

    /* check assertions */
    selectionAnnotations = selectionAnnotationStore.getAnnotations();
    assertEquals(1, selectionAnnotations.size());

    selectionAnnotation = selectionAnnotations.get(0);
    assertAnnotationIntegrity(selectionAnnotation, user, file, expectedSelectionRanges, null);

    contributionAnnotations = contributionAnnotationQueue.getAnnotations();
    assertEquals(1, contributionAnnotations.size());

    contributionAnnotation = contributionAnnotations.get(0);
    assertAnnotationIntegrity(contributionAnnotation, user, file, expectedContributionRanges, null);

    /* setup*/
    int deletionStartAfter = 50;
    deletionEndAfter = 55;

    /* call to test */
    annotationManager.moveAnnotationsAfterDeletion(file2, deletionStartAfter, deletionEndAfter);

    /* check assertions */
    selectionAnnotations = selectionAnnotationStore.getAnnotations();
    assertEquals(1, selectionAnnotations.size());

    selectionAnnotation = selectionAnnotations.get(0);
    assertAnnotationIntegrity(selectionAnnotation, user, file, expectedSelectionRanges, null);

    contributionAnnotations = contributionAnnotationQueue.getAnnotations();
    assertEquals(1, contributionAnnotations.size());

    contributionAnnotation = contributionAnnotations.get(0);
    assertAnnotationIntegrity(contributionAnnotation, user, file, expectedContributionRanges, null);

    /* setup*/
    deletionStartBefore = 30;
    deletionEndAfter = 60;

    /* call to test */
    annotationManager.moveAnnotationsAfterDeletion(file2, deletionStartBefore, deletionEndAfter);

    /* check assertions */
    selectionAnnotations = selectionAnnotationStore.getAnnotations();
    assertEquals(1, selectionAnnotations.size());

    selectionAnnotation = selectionAnnotations.get(0);
    assertAnnotationIntegrity(selectionAnnotation, user, file, expectedSelectionRanges, null);

    contributionAnnotations = contributionAnnotationQueue.getAnnotations();
    assertEquals(1, contributionAnnotations.size());

    contributionAnnotation = contributionAnnotations.get(0);
    assertAnnotationIntegrity(contributionAnnotation, user, file, expectedContributionRanges, null);
  }

  /**
   * Test applying store annotations to a newly opened editor for a file. All annotations should
   * contain a valid range highlighter for every annotation range after the call.
   */
  @Test
  public void testApplyStoredAnnotations() throws Exception {
    /* setup */
    EasyMock.expect(editor.isDisposed()).andStubReturn(false);
    EasyMock.replay(editor);

    int start = 94;
    int end = 112;
    List<Pair<Integer, Integer>> expectedSelectionRanges = createSelectionRange(start, end);
    List<Pair<Integer, Integer>> expectedContributionRanges = createContributionRanges(start, end);

    SelectionAnnotation selectionAnnotation =
        new SelectionAnnotation(
            user, file, null, createAnnotationRanges(expectedSelectionRanges, false));
    selectionAnnotationStore.addAnnotation(selectionAnnotation);

    ContributionAnnotation contributionAnnotation =
        new ContributionAnnotation(
            user, file, null, createAnnotationRanges(expectedContributionRanges, false));
    contributionAnnotationQueue.addAnnotation(contributionAnnotation);

    assertAnnotationIntegrity(selectionAnnotation, user, file, expectedSelectionRanges, null);
    assertAnnotationIntegrity(contributionAnnotation, user, file, expectedContributionRanges, null);

    prepareMockAddRemoveRangeHighlighters();
    mockAddRangeHighlighters(expectedSelectionRanges, AnnotationType.SELECTION_ANNOTATION);
    mockAddRangeHighlighters(expectedContributionRanges, AnnotationType.CONTRIBUTION_ANNOTATION);
    replayMockAddRemoveRangeHighlighters();

    /* call to test */
    annotationManager.applyStoredAnnotations(file, editor);

    /* check assertions */
    List<SelectionAnnotation> selectionAnnotations = selectionAnnotationStore.getAnnotations();
    assertEquals(1, selectionAnnotations.size());

    selectionAnnotation = selectionAnnotations.get(0);
    assertAnnotationIntegrity(selectionAnnotation, user, file, expectedSelectionRanges, editor);

    List<ContributionAnnotation> contributionAnnotations =
        contributionAnnotationQueue.getAnnotations();
    assertEquals(1, contributionAnnotations.size());

    contributionAnnotation = contributionAnnotations.get(0);
    assertAnnotationIntegrity(
        contributionAnnotation, user, file, expectedContributionRanges, editor);
  }

  /**
   * Tests updating the positions of the stored annotations by using their range highlighter
   * positions. After the call, the positions of all annotation ranges should match the position of
   * their range highlighter.
   */
  @Test
  public void testUpdateAnnotationStore() {
    /* setup */
    int start = 12;
    int end = 34;
    int offset = 23;
    List<Pair<Integer, Integer>> startSelectionRanges = createSelectionRange(start, end);
    List<Pair<Integer, Integer>> startContributionRanges = createContributionRanges(start, end);

    List<Pair<Integer, Integer>> expectedSelectionRanges =
        createSelectionRange(start + offset, end + offset);
    List<Pair<Integer, Integer>> expectedContributionRanges =
        createContributionRanges(start + offset, end + offset);

    /*
     * Mock range highlighters to return initial position for initialization check and adjusted
     * position for all subsequent calls.
     */
    List<AnnotationRange> selectionAnnotationRanges =
        startSelectionRanges
            .stream()
            .map(
                range -> {
                  int rangeStart = range.getLeft();
                  int rangeEnd = range.getRight();

                  RangeHighlighter rangeHighlighter =
                      EasyMock.createNiceMock(RangeHighlighter.class);

                  EasyMock.expect(rangeHighlighter.getStartOffset()).andReturn(rangeStart);
                  EasyMock.expect(rangeHighlighter.getEndOffset()).andReturn(rangeEnd);

                  EasyMock.expect(rangeHighlighter.getStartOffset())
                      .andStubReturn(rangeStart + offset);
                  EasyMock.expect(rangeHighlighter.getEndOffset()).andStubReturn(rangeEnd + offset);

                  EasyMock.expect(rangeHighlighter.isValid()).andStubReturn(true);

                  EasyMock.replay(rangeHighlighter);

                  return new AnnotationRange(rangeStart, rangeEnd, rangeHighlighter);
                })
            .collect(Collectors.toList());

    List<AnnotationRange> contributionAnnotationRanges =
        startContributionRanges
            .stream()
            .map(
                range -> {
                  int rangeStart = range.getLeft();
                  int rangeEnd = range.getRight();

                  RangeHighlighter rangeHighlighter =
                      EasyMock.createNiceMock(RangeHighlighter.class);

                  EasyMock.expect(rangeHighlighter.getStartOffset()).andReturn(rangeStart);
                  EasyMock.expect(rangeHighlighter.getEndOffset()).andReturn(rangeEnd);

                  EasyMock.expect(rangeHighlighter.getStartOffset())
                      .andStubReturn(rangeStart + offset);
                  EasyMock.expect(rangeHighlighter.getEndOffset()).andStubReturn(rangeEnd + offset);

                  EasyMock.expect(rangeHighlighter.isValid()).andStubReturn(true);

                  EasyMock.replay(rangeHighlighter);

                  return new AnnotationRange(rangeStart, rangeEnd, rangeHighlighter);
                })
            .collect(Collectors.toList());

    SelectionAnnotation selectionAnnotation =
        new SelectionAnnotation(user, file, editor, selectionAnnotationRanges);
    selectionAnnotationStore.addAnnotation(selectionAnnotation);

    ContributionAnnotation contributionAnnotation =
        new ContributionAnnotation(user, file, editor, contributionAnnotationRanges);
    contributionAnnotationQueue.addAnnotation(contributionAnnotation);

    /* call to test */
    annotationManager.updateAnnotationStore(file);

    /* check assertions */
    List<SelectionAnnotation> selectionAnnotations = selectionAnnotationStore.getAnnotations();
    assertEquals(1, selectionAnnotations.size());

    selectionAnnotation = selectionAnnotations.get(0);
    assertAnnotationIntegrity(selectionAnnotation, user, file, expectedSelectionRanges, editor);

    List<ContributionAnnotation> contributionAnnotations =
        contributionAnnotationQueue.getAnnotations();
    assertEquals(1, contributionAnnotations.size());

    contributionAnnotation = contributionAnnotations.get(0);
    assertAnnotationIntegrity(
        contributionAnnotation, user, file, expectedContributionRanges, editor);
  }

  /**
   * Tests updating the positions of the stored annotations by using their range highlighter
   * positions. After the call, the invalid annotations should no longer be contained in the
   * annotation store.
   */
  @Test
  public void testUpdateAnnotationStoreRemoveInvalidAnnotations() {
    /* setup */
    int start = 12;
    int end = 34;
    List<Pair<Integer, Integer>> startSelectionRanges = createSelectionRange(start, end);
    List<Pair<Integer, Integer>> startContributionRanges = createContributionRanges(start, end);

    /*
     * Mock range highlighters to return 'isValid=true' for initialization check and 'isValid=false'
     *  for all subsequent calls.
     */
    List<AnnotationRange> selectionAnnotationRanges =
        startSelectionRanges
            .stream()
            .map(
                range -> {
                  int rangeStart = range.getLeft();
                  int rangeEnd = range.getRight();

                  RangeHighlighter rangeHighlighter =
                      EasyMock.createNiceMock(RangeHighlighter.class);

                  EasyMock.expect(rangeHighlighter.getStartOffset()).andReturn(rangeStart);
                  EasyMock.expect(rangeHighlighter.getEndOffset()).andReturn(rangeEnd);

                  EasyMock.expect(rangeHighlighter.isValid()).andReturn(true);
                  EasyMock.expect(rangeHighlighter.isValid()).andStubReturn(false);

                  EasyMock.replay(rangeHighlighter);

                  return new AnnotationRange(rangeStart, rangeEnd, rangeHighlighter);
                })
            .collect(Collectors.toList());

    List<AnnotationRange> contributionAnnotationRanges =
        startContributionRanges
            .stream()
            .map(
                range -> {
                  int rangeStart = range.getLeft();
                  int rangeEnd = range.getRight();

                  RangeHighlighter rangeHighlighter =
                      EasyMock.createNiceMock(RangeHighlighter.class);

                  EasyMock.expect(rangeHighlighter.getStartOffset()).andReturn(rangeStart);
                  EasyMock.expect(rangeHighlighter.getEndOffset()).andReturn(rangeEnd);

                  EasyMock.expect(rangeHighlighter.isValid()).andReturn(true);
                  EasyMock.expect(rangeHighlighter.isValid()).andStubReturn(false);

                  EasyMock.replay(rangeHighlighter);

                  return new AnnotationRange(rangeStart, rangeEnd, rangeHighlighter);
                })
            .collect(Collectors.toList());

    SelectionAnnotation selectionAnnotation =
        new SelectionAnnotation(user, file, editor, selectionAnnotationRanges);
    selectionAnnotationStore.addAnnotation(selectionAnnotation);

    ContributionAnnotation contributionAnnotation =
        new ContributionAnnotation(user, file, editor, contributionAnnotationRanges);
    contributionAnnotationQueue.addAnnotation(contributionAnnotation);

    /* call to test */
    annotationManager.updateAnnotationStore(file);

    /* check assertions */
    List<SelectionAnnotation> selectionAnnotations = selectionAnnotationStore.getAnnotations();
    assertEquals(0, selectionAnnotations.size());

    List<ContributionAnnotation> contributionAnnotations =
        contributionAnnotationQueue.getAnnotations();
    assertEquals(0, contributionAnnotations.size());
  }

  /**
   * Test removing the local representation of all annotations for a file. After the call, no
   * annotation for the file should contain an editor or a range highlighter in any one of its
   * annotation ranges.
   */
  @Test
  public void testRemoveLocalRepresentation() {
    /* setup */
    int start = 40;
    int end = 50;
    List<Pair<Integer, Integer>> expectedSelectionRanges = createSelectionRange(start, end);
    List<Pair<Integer, Integer>> expectedContributionRanges = createContributionRanges(start, end);

    SelectionAnnotation selectionAnnotation =
        new SelectionAnnotation(
            user, file, editor, createAnnotationRanges(expectedSelectionRanges, true));
    selectionAnnotationStore.addAnnotation(selectionAnnotation);

    ContributionAnnotation contributionAnnotation =
        new ContributionAnnotation(
            user, file, editor, createAnnotationRanges(expectedContributionRanges, true));
    contributionAnnotationQueue.addAnnotation(contributionAnnotation);

    /* call to test */
    annotationManager.removeLocalRepresentation(file);

    /* check assertions */
    List<SelectionAnnotation> selectionAnnotations = selectionAnnotationStore.getAnnotations();
    assertEquals(1, selectionAnnotations.size());

    selectionAnnotation = selectionAnnotations.get(0);
    assertAnnotationIntegrity(selectionAnnotation, user, file, expectedSelectionRanges, null);

    List<ContributionAnnotation> contributionAnnotations =
        contributionAnnotationQueue.getAnnotations();
    assertEquals(1, contributionAnnotations.size());

    contributionAnnotation = contributionAnnotations.get(0);
    assertAnnotationIntegrity(contributionAnnotation, user, file, expectedContributionRanges, null);
  }

  /**
   * Test removing the local representation of all annotations for a different file. No annotations
   * should be changed.
   */
  @Test
  public void testRemoveLocalRepresentationDifferentFile() {
    /* setup */
    IFile file2 = EasyMock.createNiceMock(IFile.class);

    int start = 40;
    int end = 50;
    List<Pair<Integer, Integer>> expectedSelectionRanges = createSelectionRange(start, end);
    List<Pair<Integer, Integer>> expectedContributionRanges = createContributionRanges(start, end);

    SelectionAnnotation selectionAnnotation =
        new SelectionAnnotation(
            user, file, editor, createAnnotationRanges(expectedSelectionRanges, true));
    selectionAnnotationStore.addAnnotation(selectionAnnotation);

    ContributionAnnotation contributionAnnotation =
        new ContributionAnnotation(
            user, file, editor, createAnnotationRanges(expectedContributionRanges, true));
    contributionAnnotationQueue.addAnnotation(contributionAnnotation);

    /* call to test */
    annotationManager.removeLocalRepresentation(file2);

    /* check assertions */
    List<SelectionAnnotation> selectionAnnotations = selectionAnnotationStore.getAnnotations();
    assertEquals(1, selectionAnnotations.size());

    selectionAnnotation = selectionAnnotations.get(0);
    assertAnnotationIntegrity(selectionAnnotation, user, file, expectedSelectionRanges, editor);

    List<ContributionAnnotation> contributionAnnotations =
        contributionAnnotationQueue.getAnnotations();
    assertEquals(1, contributionAnnotations.size());

    contributionAnnotation = contributionAnnotations.get(0);
    assertAnnotationIntegrity(
        contributionAnnotation, user, file, expectedContributionRanges, editor);
  }

  /**
   * Tests reloading all store annotations. After the call, all annotations should still have the
   * same state but their range highlighters should have been removed and re-added.
   */
  @Test
  public void testReloadAnnotations() throws Exception {
    /* setup */
    EasyMock.expect(editor.isDisposed()).andStubReturn(false);
    EasyMock.replay(editor);

    int start = 94;
    int end = 112;
    List<Pair<Integer, Integer>> expectedSelectionRanges = createSelectionRange(start, end);
    List<Pair<Integer, Integer>> expectedContributionRanges = createContributionRanges(start, end);

    SelectionAnnotation selectionAnnotation =
        new SelectionAnnotation(
            user, file, editor, createAnnotationRanges(expectedSelectionRanges, true));
    selectionAnnotationStore.addAnnotation(selectionAnnotation);

    ContributionAnnotation contributionAnnotation =
        new ContributionAnnotation(
            user, file, editor, createAnnotationRanges(expectedContributionRanges, true));
    contributionAnnotationQueue.addAnnotation(contributionAnnotation);

    prepareMockAddRemoveRangeHighlighters();
    mockAddRangeHighlighters(expectedSelectionRanges, AnnotationType.SELECTION_ANNOTATION);
    mockAddRangeHighlighters(expectedContributionRanges, AnnotationType.CONTRIBUTION_ANNOTATION);
    mockRemoveRangeHighlighters(selectionAnnotation);
    mockRemoveRangeHighlighters(contributionAnnotation);
    replayMockAddRemoveRangeHighlighters();

    /* call to mock */
    annotationManager.reloadAnnotations();

    /* check assertions */
    verifyRemovalCall();

    /* check assertions */
    List<SelectionAnnotation> selectionAnnotations = selectionAnnotationStore.getAnnotations();
    assertEquals(1, selectionAnnotations.size());

    selectionAnnotation = selectionAnnotations.get(0);
    assertAnnotationIntegrity(selectionAnnotation, user, file, expectedSelectionRanges, editor);

    List<ContributionAnnotation> contributionAnnotations =
        contributionAnnotationQueue.getAnnotations();
    assertEquals(1, contributionAnnotations.size());

    contributionAnnotation = contributionAnnotations.get(0);
    assertAnnotationIntegrity(
        contributionAnnotation, user, file, expectedContributionRanges, editor);
  }

  /** Tests removing all annotations for a specific user. */
  @Test
  public void testRemoveAnnotationsForUser() throws Exception {
    /* setup */
    int start = 94;
    int end = 112;
    List<Pair<Integer, Integer>> expectedSelectionRanges = createSelectionRange(start, end);
    List<Pair<Integer, Integer>> expectedContributionRanges = createContributionRanges(start, end);

    SelectionAnnotation selectionAnnotation =
        new SelectionAnnotation(
            user, file, editor, createAnnotationRanges(expectedSelectionRanges, true));
    selectionAnnotationStore.addAnnotation(selectionAnnotation);

    ContributionAnnotation contributionAnnotation =
        new ContributionAnnotation(
            user, file, editor, createAnnotationRanges(expectedContributionRanges, true));
    contributionAnnotationQueue.addAnnotation(contributionAnnotation);

    prepareMockAddRemoveRangeHighlighters();
    mockRemoveRangeHighlighters(selectionAnnotation);
    mockRemoveRangeHighlighters(contributionAnnotation);
    replayMockAddRemoveRangeHighlighters();

    /* calls to test */
    annotationManager.removeAnnotations(user);

    /* check assertions */
    verifyRemovalCall();

    List<SelectionAnnotation> selectionAnnotations = selectionAnnotationStore.getAnnotations();
    assertEquals(0, selectionAnnotations.size());

    List<ContributionAnnotation> contributionAnnotations =
        contributionAnnotationQueue.getAnnotations();
    assertEquals(0, contributionAnnotations.size());
  }

  /**
   * Tests removing all annotations for a different user. All annotations should still be present
   * after the call.
   */
  @Test
  public void testRemoveAnnotationsForDifferentUser() {
    /* setup */
    User user2 = EasyMock.createNiceMock(User.class);

    int start = 94;
    int end = 112;
    List<Pair<Integer, Integer>> expectedSelectionRanges = createSelectionRange(start, end);
    List<Pair<Integer, Integer>> expectedContributionRanges = createContributionRanges(start, end);

    SelectionAnnotation selectionAnnotation =
        new SelectionAnnotation(
            user, file, null, createAnnotationRanges(expectedSelectionRanges, false));
    selectionAnnotationStore.addAnnotation(selectionAnnotation);

    ContributionAnnotation contributionAnnotation =
        new ContributionAnnotation(
            user, file, null, createAnnotationRanges(expectedContributionRanges, false));
    contributionAnnotationQueue.addAnnotation(contributionAnnotation);

    /* calls to test */
    annotationManager.removeAnnotations(user2);

    /* check assertions */
    List<SelectionAnnotation> selectionAnnotations = selectionAnnotationStore.getAnnotations();
    assertEquals(1, selectionAnnotations.size());

    selectionAnnotation = selectionAnnotations.get(0);
    assertAnnotationIntegrity(selectionAnnotation, user, file, expectedSelectionRanges, null);

    List<ContributionAnnotation> contributionAnnotations =
        contributionAnnotationQueue.getAnnotations();
    assertEquals(1, contributionAnnotations.size());

    contributionAnnotation = contributionAnnotations.get(0);
    assertAnnotationIntegrity(contributionAnnotation, user, file, expectedContributionRanges, null);
  }

  /** Tests removing all annotations for a specific file. */
  @Test
  public void testRemoveAnnotationsForFile() throws Exception {
    /* setup */
    int start = 94;
    int end = 112;
    List<Pair<Integer, Integer>> expectedSelectionRanges = createSelectionRange(start, end);
    List<Pair<Integer, Integer>> expectedContributionRanges = createContributionRanges(start, end);

    SelectionAnnotation selectionAnnotation =
        new SelectionAnnotation(
            user, file, editor, createAnnotationRanges(expectedSelectionRanges, true));
    selectionAnnotationStore.addAnnotation(selectionAnnotation);

    ContributionAnnotation contributionAnnotation =
        new ContributionAnnotation(
            user, file, editor, createAnnotationRanges(expectedContributionRanges, true));
    contributionAnnotationQueue.addAnnotation(contributionAnnotation);

    prepareMockAddRemoveRangeHighlighters();
    mockRemoveRangeHighlighters(selectionAnnotation);
    mockRemoveRangeHighlighters(contributionAnnotation);
    replayMockAddRemoveRangeHighlighters();

    /* calls to test */
    annotationManager.removeAnnotations(file);

    /* check assertions */
    verifyRemovalCall();

    List<SelectionAnnotation> selectionAnnotations = selectionAnnotationStore.getAnnotations();
    assertEquals(0, selectionAnnotations.size());

    List<ContributionAnnotation> contributionAnnotations =
        contributionAnnotationQueue.getAnnotations();
    assertEquals(0, contributionAnnotations.size());
  }

  /**
   * Tests removing all annotations for a different file. All annotations should still be present
   * after the call.
   */
  @Test
  public void testRemoveAnnotationsForDifferentFile() {
    /* setup */
    IFile file2 = EasyMock.createNiceMock(IFile.class);

    int start = 94;
    int end = 112;
    List<Pair<Integer, Integer>> expectedSelectionRanges = createSelectionRange(start, end);
    List<Pair<Integer, Integer>> expectedContributionRanges = createContributionRanges(start, end);

    SelectionAnnotation selectionAnnotation =
        new SelectionAnnotation(
            user, file, null, createAnnotationRanges(expectedSelectionRanges, false));
    selectionAnnotationStore.addAnnotation(selectionAnnotation);

    ContributionAnnotation contributionAnnotation =
        new ContributionAnnotation(
            user, file, null, createAnnotationRanges(expectedContributionRanges, false));
    contributionAnnotationQueue.addAnnotation(contributionAnnotation);

    /* calls to test */
    annotationManager.removeAnnotations(file2);

    /* check assertions */
    List<SelectionAnnotation> selectionAnnotations = selectionAnnotationStore.getAnnotations();
    assertEquals(1, selectionAnnotations.size());

    selectionAnnotation = selectionAnnotations.get(0);
    assertAnnotationIntegrity(selectionAnnotation, user, file, expectedSelectionRanges, null);

    List<ContributionAnnotation> contributionAnnotations =
        contributionAnnotationQueue.getAnnotations();
    assertEquals(1, contributionAnnotations.size());

    contributionAnnotation = contributionAnnotations.get(0);
    assertAnnotationIntegrity(contributionAnnotation, user, file, expectedContributionRanges, null);
  }

  /** Tests removing all annotations as part of the annotation manager disposal. */
  @Test
  public void testRemoveAllAnnotations() throws Exception {
    /* setup */
    User user2 = EasyMock.createNiceMock(User.class);
    IFile file2 = EasyMock.createNiceMock(IFile.class);

    int start = 94;
    int end = 112;
    List<Pair<Integer, Integer>> expectedSelectionRanges = createSelectionRange(start, end);
    List<Pair<Integer, Integer>> expectedContributionRanges = createContributionRanges(start, end);

    SelectionAnnotation selectionAnnotation1 =
        new SelectionAnnotation(
            user, file, editor, createAnnotationRanges(expectedSelectionRanges, true));
    selectionAnnotationStore.addAnnotation(selectionAnnotation1);

    ContributionAnnotation contributionAnnotation1 =
        new ContributionAnnotation(
            user, file, editor, createAnnotationRanges(expectedContributionRanges, true));
    contributionAnnotationQueue.addAnnotation(contributionAnnotation1);

    SelectionAnnotation selectionAnnotation2 =
        new SelectionAnnotation(
            user2, file, editor, createAnnotationRanges(expectedSelectionRanges, true));
    selectionAnnotationStore.addAnnotation(selectionAnnotation2);

    ContributionAnnotation contributionAnnotation2 =
        new ContributionAnnotation(
            user2, file, editor, createAnnotationRanges(expectedContributionRanges, true));
    contributionAnnotationQueue.addAnnotation(contributionAnnotation2);

    SelectionAnnotation selectionAnnotation3 =
        new SelectionAnnotation(
            user, file2, editor, createAnnotationRanges(expectedSelectionRanges, true));
    selectionAnnotationStore.addAnnotation(selectionAnnotation3);

    ContributionAnnotation contributionAnnotation3 =
        new ContributionAnnotation(
            user, file2, editor, createAnnotationRanges(expectedContributionRanges, true));
    contributionAnnotationQueue.addAnnotation(contributionAnnotation3);

    SelectionAnnotation selectionAnnotation4 =
        new SelectionAnnotation(
            user2, file2, editor, createAnnotationRanges(expectedSelectionRanges, true));
    selectionAnnotationStore.addAnnotation(selectionAnnotation4);

    ContributionAnnotation contributionAnnotation4 =
        new ContributionAnnotation(
            user2, file2, editor, createAnnotationRanges(expectedContributionRanges, true));
    contributionAnnotationQueue.addAnnotation(contributionAnnotation4);

    prepareMockAddRemoveRangeHighlighters();
    mockRemoveRangeHighlighters(selectionAnnotation1);
    mockRemoveRangeHighlighters(contributionAnnotation1);
    mockRemoveRangeHighlighters(selectionAnnotation2);
    mockRemoveRangeHighlighters(contributionAnnotation2);
    mockRemoveRangeHighlighters(selectionAnnotation3);
    mockRemoveRangeHighlighters(contributionAnnotation3);
    mockRemoveRangeHighlighters(selectionAnnotation4);
    mockRemoveRangeHighlighters(contributionAnnotation4);
    replayMockAddRemoveRangeHighlighters();

    /* calls to test */
    annotationManager.dispose();

    /* check assertions */
    verifyRemovalCall();

    List<SelectionAnnotation> selectionAnnotations = selectionAnnotationStore.getAnnotations();
    assertEquals(0, selectionAnnotations.size());

    List<ContributionAnnotation> contributionAnnotations =
        contributionAnnotationQueue.getAnnotations();
    assertEquals(0, contributionAnnotations.size());
  }

  /** Test updating the file mapping for annotations. */
  @Test
  public void testUpdateAnnotationPath() {
    /* setup */
    IFile file2 = EasyMock.createNiceMock(IFile.class);

    int start = 94;
    int end = 112;
    List<Pair<Integer, Integer>> expectedSelectionRanges = createSelectionRange(start, end);
    List<Pair<Integer, Integer>> expectedContributionRanges = createContributionRanges(start, end);

    SelectionAnnotation selectionAnnotation =
        new SelectionAnnotation(
            user, file, null, createAnnotationRanges(expectedSelectionRanges, false));
    selectionAnnotationStore.addAnnotation(selectionAnnotation);

    ContributionAnnotation contributionAnnotation =
        new ContributionAnnotation(
            user, file, null, createAnnotationRanges(expectedContributionRanges, false));
    contributionAnnotationQueue.addAnnotation(contributionAnnotation);

    /* call to test */
    annotationManager.updateAnnotationPath(file, file2);

    /* check assertions */
    List<SelectionAnnotation> selectionAnnotations = selectionAnnotationStore.getAnnotations();
    assertEquals(1, selectionAnnotations.size());

    selectionAnnotation = selectionAnnotations.get(0);
    assertAnnotationIntegrity(selectionAnnotation, user, file2, expectedSelectionRanges, null);

    List<ContributionAnnotation> contributionAnnotations =
        contributionAnnotationQueue.getAnnotations();
    assertEquals(1, contributionAnnotations.size());

    contributionAnnotation = contributionAnnotations.get(0);
    assertAnnotationIntegrity(
        contributionAnnotation, user, file2, expectedContributionRanges, null);
  }

  /**
   * Test updating the file mapping for a different file. All annotations should still be unchanged
   * after the call.
   */
  @Test
  public void testUpdateAnnotationPathDifferentFile() {
    /* setup */
    IFile file2 = EasyMock.createNiceMock(IFile.class);
    IFile file3 = EasyMock.createNiceMock(IFile.class);

    int start = 94;
    int end = 112;
    List<Pair<Integer, Integer>> expectedSelectionRanges = createSelectionRange(start, end);
    List<Pair<Integer, Integer>> expectedContributionRanges = createContributionRanges(start, end);

    SelectionAnnotation selectionAnnotation =
        new SelectionAnnotation(
            user, file, null, createAnnotationRanges(expectedSelectionRanges, false));
    selectionAnnotationStore.addAnnotation(selectionAnnotation);

    ContributionAnnotation contributionAnnotation =
        new ContributionAnnotation(
            user, file, null, createAnnotationRanges(expectedContributionRanges, false));
    contributionAnnotationQueue.addAnnotation(contributionAnnotation);

    /* call to test */
    annotationManager.updateAnnotationPath(file2, file3);

    /* check assertions */
    List<SelectionAnnotation> selectionAnnotations = selectionAnnotationStore.getAnnotations();
    assertEquals(1, selectionAnnotations.size());

    selectionAnnotation = selectionAnnotations.get(0);
    assertAnnotationIntegrity(selectionAnnotation, user, file, expectedSelectionRanges, null);

    List<ContributionAnnotation> contributionAnnotations =
        contributionAnnotationQueue.getAnnotations();
    assertEquals(1, contributionAnnotations.size());

    contributionAnnotation = contributionAnnotations.get(0);
    assertAnnotationIntegrity(contributionAnnotation, user, file, expectedContributionRanges, null);
  }

  /**
   * Creates a list containing a single entry for the given range.
   *
   * @param rangeStart the start of the range
   * @param rangeEnd the end of the range
   * @return a list containing a single entry for the given range
   */
  private List<Pair<Integer, Integer>> createSelectionRange(int rangeStart, int rangeEnd) {
    return Collections.singletonList(new ImmutablePair<>(rangeStart, rangeEnd));
  }

  /**
   * Creates a list of ranges of size 1 covering the given range.
   *
   * @param rangeStart the start of the range
   * @param rangeEnd the end of the range
   * @return a list of ranges of size 1 covering the given range
   */
  private List<Pair<Integer, Integer>> createContributionRanges(int rangeStart, int rangeEnd) {
    List<Pair<Integer, Integer>> expectedRanges = new ArrayList<>(rangeEnd - rangeStart);

    for (int i = rangeStart; i < rangeEnd; i++) {
      expectedRanges.add(new ImmutablePair<>(i, i + 1));
    }

    return expectedRanges;
  }

  private List<AnnotationRange> createAnnotationRanges(
      List<Pair<Integer, Integer>> ranges, boolean addRangeHighlighters) {
    return ranges
        .stream()
        .map(
            range -> {
              int rangeStart = range.getLeft();
              int rangeEnd = range.getRight();

              if (addRangeHighlighters) {
                RangeHighlighter rangeHighlighter = mockRangeHighlighter(rangeStart, rangeEnd);

                return new AnnotationRange(rangeStart, rangeEnd, rangeHighlighter);
              } else {
                return new AnnotationRange(rangeStart, rangeEnd);
              }
            })
        .collect(Collectors.toList());
  }

  /**
   * Asserts the annotation integrity.
   *
   * <p>Asserts that
   *
   * <ul>
   *   <li>the annotation holds the given file object
   *   <li>the annotation holds the given user object
   *   <li>the annotation contains exactly the expected ranges
   *   <li>if <code>expectedEditor!=null</code>
   *       <ul>
   *         <li>the annotations contains
   *         <li>each annotation range contains a range highlighter
   *         <li>the position of each range highlighter matches its annotation range
   *       </ul>
   *   <li>if <code>expectedEditor!=null</code>
   *       <ul>
   *         <li>the annotation does not contain
   *         <li>each annotation range does not contain a range highlighter
   *       </ul>
   * </ul>
   *
   * @param annotation the annotation to check
   * @param expectedUser the user to whom the annotation should belong
   * @param expectedFile the file to which the annotation should belong
   * @param expectedRanges the ranges that should be contained in the annotation
   * @param expectedEditor the editor that should be contained in the annotation
   */
  private void assertAnnotationIntegrity(
      AbstractEditorAnnotation annotation,
      User expectedUser,
      IFile expectedFile,
      List<Pair<Integer, Integer>> expectedRanges,
      Editor expectedEditor) {

    assertEquals(expectedUser, annotation.getUser());
    assertEquals(expectedFile, annotation.getFile());

    Editor editor = annotation.getEditor();
    if (expectedEditor != null) {
      assertEquals(expectedEditor, editor);

    } else {
      assertNull(editor);
    }

    List<AnnotationRange> annotationRanges = annotation.getAnnotationRanges();
    assertEquals(expectedRanges.size(), annotationRanges.size());

    List<Pair<Integer, Integer>> foundRanges = new ArrayList<>();

    for (AnnotationRange annotationRange : annotationRanges) {
      int rangeStart = annotationRange.getStart();
      int rangeEnd = annotationRange.getEnd();
      Pair<Integer, Integer> range = new ImmutablePair<>(rangeStart, rangeEnd);

      foundRanges.add(range);

      RangeHighlighter rangeHighlighter = annotationRange.getRangeHighlighter();
      if (expectedEditor != null) {
        assertNotNull(rangeHighlighter);
        assertEquals(rangeStart, rangeHighlighter.getStartOffset());
        assertEquals(rangeEnd, rangeHighlighter.getEndOffset());
      } else {
        assertNull(rangeHighlighter);
      }
    }

    assertEquals(expectedRanges, foundRanges);
  }

  /**
   * Must be called before the first call to {@link #mockAddRangeHighlighters(List, AnnotationType)}
   * or {@link #mockRemoveRangeHighlighters(AbstractEditorAnnotation)}.
   */
  private void prepareMockAddRemoveRangeHighlighters() {
    PowerMock.mockStaticPartial(
        AnnotationManager.class, "addRangeHighlighter", "removeRangeHighlighter");
  }

  /**
   * Mocks the method creating the actual range highlighters in the editor for the list of given
   * ranges.
   *
   * <p>{@link #prepareMockAddRemoveRangeHighlighters()} must be called before the first call to
   * this methods and {@link #replayMockAddRemoveRangeHighlighters()} must be called after the last
   * call to this method to replay the added mocking logic.
   *
   * @param ranges the ranges to mock
   * @throws Exception see {@link PowerMock#expectPrivate(Object, Method, Object...)}
   */
  private void mockAddRangeHighlighters(
      List<Pair<Integer, Integer>> ranges, AnnotationType annotationType) throws Exception {

    for (Pair<Integer, Integer> range : ranges) {
      int rangeStart = range.getLeft();
      int rangeEnd = range.getRight();

      RangeHighlighter rangeHighlighter = mockRangeHighlighter(rangeStart, rangeEnd);

      PowerMock.expectPrivate(
              annotationManager,
              "addRangeHighlighter",
              user,
              rangeStart,
              rangeEnd,
              editor,
              annotationType,
              file)
          .andStubReturn(rangeHighlighter);
    }
  }

  /**
   * Mocks the method removing the actual range highlighters from the editor for the given
   * annotation.
   *
   * <p>When using this call, it is also advised to call {@link PowerMock#verify(Object...)} on
   * <code>AnnotationManager.class</code> to ensure that the {@link
   * IExpectationSetters#atLeastOnce()} restriction is met.
   *
   * <p>{@link #prepareMockAddRemoveRangeHighlighters()} must be called before the first call to
   * this methods and {@link #replayMockAddRemoveRangeHighlighters()} must be called after the last
   * call to this method to replay the added mocking logic.
   *
   * @param annotation the annotation whose range highlighter removal to mock
   * @throws Exception see {@link PowerMock#expectPrivate(Object, Method, Object...)}
   */
  private void mockRemoveRangeHighlighters(AbstractEditorAnnotation annotation) throws Exception {
    PowerMock.expectPrivate(annotationManager, "removeRangeHighlighter", annotation)
        .atLeastOnce()
        .asStub();
  }

  /**
   * Must be called after the last call to {@link #mockAddRangeHighlighters(List, AnnotationType)}
   * or {@link #mockRemoveRangeHighlighters(AbstractEditorAnnotation)}.
   */
  private void replayMockAddRemoveRangeHighlighters() {
    PowerMock.replay(AnnotationManager.class);
  }

  /**
   * Verifies that all added range highlighter removal mocks where called at least once. This can be
   * used to check whether the local representation of removed annotations was also removed.
   */
  private void verifyRemovalCall() {
    PowerMock.verify(AnnotationManager.class);
  }

  private RangeHighlighter mockRangeHighlighter(int rangeStart, int rangeEnd) {
    RangeHighlighter rangeHighlighter = EasyMock.createNiceMock(RangeHighlighter.class);

    EasyMock.expect(rangeHighlighter.getStartOffset()).andStubReturn(rangeStart);
    EasyMock.expect(rangeHighlighter.getEndOffset()).andStubReturn(rangeEnd);
    EasyMock.expect(rangeHighlighter.isValid()).andStubReturn(true);

    EasyMock.expect(rangeHighlighter.isValid()).andStubReturn(true);

    EasyMock.replay(rangeHighlighter);

    return rangeHighlighter;
  }
}
