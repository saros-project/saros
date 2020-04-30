package saros.intellij.editor.annotations;

import static saros.intellij.editor.annotations.AnnotationManager.MAX_CONTRIBUTION_ANNOTATIONS;

import com.intellij.openapi.editor.Editor;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import saros.filesystem.IFile;
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
}
