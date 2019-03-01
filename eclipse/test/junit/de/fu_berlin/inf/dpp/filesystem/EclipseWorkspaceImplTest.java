package de.fu_berlin.inf.dpp.filesystem;

import static org.junit.Assert.assertTrue;

import de.fu_berlin.inf.dpp.exceptions.OperationCanceledException;
import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;
import java.io.IOException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.eclipse.core.runtime.CoreException;
import org.junit.Before;
import org.junit.Test;

public class EclipseWorkspaceImplTest {

  private org.eclipse.core.resources.IWorkspace workspaceDelegateMock;

  private IWorkspace workspace;

  @Before
  public void setup() {

    workspaceDelegateMock = EasyMock.createMock(org.eclipse.core.resources.IWorkspace.class);

    try {
      workspaceDelegateMock.run(
          EasyMock.isA(org.eclipse.core.resources.IWorkspaceRunnable.class),
          EasyMock.anyObject(org.eclipse.core.runtime.jobs.ISchedulingRule.class),
          EasyMock.anyInt(),
          EasyMock.anyObject(org.eclipse.core.runtime.IProgressMonitor.class));

    } catch (CoreException e) {
      // cannot happen
    }

    EasyMock.expectLastCall()
        .andStubAnswer(
            new IAnswer<Object>() {
              @Override
              public Object answer() throws Throwable {
                ((org.eclipse.core.resources.IWorkspaceRunnable) EasyMock.getCurrentArguments()[0])
                    .run(
                        (org.eclipse.core.runtime.IProgressMonitor)
                            EasyMock.getCurrentArguments()[3]);
                return null;
              }
            });

    EasyMock.expect(workspaceDelegateMock.getRoot()).andStubReturn(null);

    EasyMock.replay(workspaceDelegateMock);

    workspace = new EclipseWorkspaceImpl(workspaceDelegateMock);
  }

  @Test(expected = OperationCanceledException.class)
  public void testDppOCE() throws Exception {

    final IWorkspaceRunnable runnable =
        new IWorkspaceRunnable() {
          @Override
          public void run(IProgressMonitor monitor) throws IOException, OperationCanceledException {
            throwDppOCE();
          }
        };

    try {
      workspace.run(runnable);
    } catch (OperationCanceledException e) {
      assertOriginalExceptionNotSwallowed(e, "throwDppOCE");
      throw e;
    }
  }

  @Test(expected = OperationCanceledException.class)
  public void testWrappedEclipseOCE() throws Exception {

    final IWorkspaceRunnable runnable =
        new IWorkspaceRunnable() {
          @Override
          public void run(IProgressMonitor monitor) throws IOException, OperationCanceledException {
            throwWrappedEclipseOCE();
          }
        };

    try {
      workspace.run(runnable);
    } catch (OperationCanceledException e) {
      assertOriginalExceptionNotSwallowed(e, "throwWrappedEclipseOCE");
      throw e;
    }
  }

  @Test(expected = OperationCanceledException.class)
  public void testEclipseOCE() throws Exception {

    final IWorkspaceRunnable runnable =
        new IWorkspaceRunnable() {
          @Override
          public void run(IProgressMonitor monitor) throws IOException, OperationCanceledException {
            throwEclipseOCE();
          }
        };

    try {
      workspace.run(runnable);
    } catch (OperationCanceledException e) {
      assertOriginalExceptionNotSwallowed(e, "throwEclipseOCE");
      throw e;
    }
  }

  @Test(expected = IOException.class)
  public void testIOException() throws Exception {

    final IWorkspaceRunnable runnable =
        new IWorkspaceRunnable() {
          @Override
          public void run(IProgressMonitor monitor) throws IOException, OperationCanceledException {
            throwIOException();
          }
        };

    try {
      workspace.run(runnable);
    } catch (IOException e) {
      assertOriginalExceptionNotSwallowed(e, "throwIOException");
      throw e;
    }
  }

  // simulates cancelation during file access inside IResourceImpls
  private void throwWrappedEclipseOCE() throws IOException {
    throw new IOException(
        new org.eclipse.core.runtime.OperationCanceledException("canceled by Eclipse"));
  }

  private void throwDppOCE() throws OperationCanceledException {
    throw new OperationCanceledException("canceled by DPP impl");
  }

  private void throwEclipseOCE() throws OperationCanceledException {
    throw new OperationCanceledException("canceled by Eclipse");
  }

  private void throwIOException() throws IOException {
    throw new IOException("broken network");
  }

  private void assertOriginalExceptionNotSwallowed(Exception e, String methodName) {
    assertTrue(
        "method name '" + methodName + "'not found in stack trace",
        ExceptionUtils.getStackTrace(e).contains(methodName));
  }
}
