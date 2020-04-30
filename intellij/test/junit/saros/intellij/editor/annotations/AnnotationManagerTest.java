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
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.easymock.EasyMock;
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

    mockAddRangeHighlighters(expectedRanges);

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
   * Mocks the method creating the actual range highlighters in the editor for the list of given
   * ranges.
   *
   * @param ranges the ranges to mock
   * @throws Exception see {@link PowerMock#expectPrivate(Object, Method, Object...)}
   */
  private void mockAddRangeHighlighters(List<Pair<Integer, Integer>> ranges) throws Exception {
    PowerMock.mockStaticPartial(AnnotationManager.class, "addRangeHighlighter");

    for (Pair<Integer, Integer> range : ranges) {
      int rangeStart = range.getLeft();
      int rangeEnd = range.getRight();

      RangeHighlighter rangeHighlighter = EasyMock.createNiceMock(RangeHighlighter.class);

      EasyMock.expect(rangeHighlighter.getStartOffset()).andStubReturn(rangeStart);
      EasyMock.expect(rangeHighlighter.getEndOffset()).andStubReturn(rangeEnd);

      EasyMock.replay(rangeHighlighter);

      PowerMock.expectPrivate(
              annotationManager,
              "addRangeHighlighter",
              user,
              rangeStart,
              rangeEnd,
              editor,
              AnnotationType.CONTRIBUTION_ANNOTATION,
              file)
          .andStubReturn(rangeHighlighter);
    }

    PowerMock.replay(AnnotationManager.class);
  }
}
