package saros.intellij.test;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expectLastCall;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.powermock.api.easymock.PowerMock;
import org.powermock.modules.junit4.PowerMockRunner;

public class IntellijMocker {
  /**
   * Mocks static <code>getInstance()</code> calls.
   *
   * <p>If you use this utility, make sure to (1) run your test class with {@link PowerMockRunner}
   * and (2) add the class to be mocked to the "prepare for test" list:
   *
   * <p>
   *
   * <pre>
   * {@literal @}RunWith(PowerMockRunner.class)
   * {@literal @}PrepareForTest({ IntellijClassToMock.class })
   * public class MyTestClass {
   *     // ...
   * }
   * </pre>
   *
   * @param clazz Class to mock
   * @param argClass class of the argument that is passed to <code>getInstance()</code>, if any. Can
   *     be <code>null</code>
   */
  public static <T> void mockStaticGetInstance(Class<T> clazz, Class<?> argClass) {
    String method = "getInstance";

    T instance = createNiceMock(clazz);
    EasyMock.replay(instance);

    PowerMock.mockStaticPartial(clazz, method);
    try {
      if (argClass == null) {
        // method has no arguments
        clazz.getMethod(method).invoke(null);
      } else {
        // method has one argument
        clazz.getMethod(method, argClass).invoke(null, anyObject(argClass));
      }
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail("reflection error, see stacktrace");
    }

    expectLastCall().andStubReturn(instance);
    PowerMock.replay(clazz);
  }
}
