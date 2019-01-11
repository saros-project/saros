package de.fu_berlin.inf.dpp.ui.browser_functions;

import static org.junit.Assert.assertEquals;

import de.fu_berlin.inf.ag_se.browser.IBrowser;
import de.fu_berlin.inf.dpp.ui.JavaScriptAPI;
import java.lang.reflect.Method;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(JavaScriptAPI.class)
public class TypedJavascriptFunctionTest {

  class SimpleFunctionWithCallable extends TypedJavascriptFunction {
    public SimpleFunctionWithCallable() {
      super("isGreater");
    }

    @BrowserFunction
    public Boolean isGreater(int base, int compare) {
      return new Boolean(compare > base);
    }
  }

  class SimpleFunctionWithoutCallable extends TypedJavascriptFunction {
    public SimpleFunctionWithoutCallable() {
      super("isGreater");
    }

    public Boolean isGreater(int base, int compare) {
      return new Boolean(compare > base);
    }
  }

  private boolean errorShown;

  @Before
  public void setUp() {
    errorShown = false;

    PowerMock.mockStatic(JavaScriptAPI.class);
    JavaScriptAPI.showError(EasyMock.isNull(IBrowser.class), EasyMock.isA(String.class));
    PowerMock.expectLastCall()
        .andStubAnswer(
            new IAnswer<Object>() {
              @Override
              public Object answer() throws Throwable {
                errorShown = true;
                return null;
              }
            });
    PowerMock.replayAll();
  }

  @Test
  public void findCallableWhenPresent() {
    Method callable = TypedJavascriptFunction.getCallable(SimpleFunctionWithCallable.class);
    assertEquals(callable.getName(), "isGreater");
  }

  @Test(expected = IllegalArgumentException.class)
  public void findCallableWhenAbsent() {
    TypedJavascriptFunction.getCallable(SimpleFunctionWithoutCallable.class);
  }

  @Test
  public void matchingParameters() {
    SimpleFunctionWithCallable func = new SimpleFunctionWithCallable();

    Object resultTrue = func.function(new Object[] {new Double(2), new Double(5)});
    assertEquals(Boolean.TRUE, resultTrue);

    Object resultFalse = func.function(new Object[] {new Double(2), new Double(1)});
    assertEquals(Boolean.FALSE, resultFalse);
  }

  @Test
  public void tooManyParameters() {
    SimpleFunctionWithCallable func = new SimpleFunctionWithCallable();

    func.function(new Object[] {new Double(2), new Double(5), new Double(1)});

    assertEquals(true, errorShown);
  }

  @Test
  public void tooFewParameters() {
    SimpleFunctionWithCallable func = new SimpleFunctionWithCallable();

    func.function(new Object[] {new Double(2)});

    assertEquals(true, errorShown);
  }

  @Test
  public void wrongTypes() {
    SimpleFunctionWithCallable func = new SimpleFunctionWithCallable();

    func.function(new Object[] {new Double(2), Boolean.FALSE});

    assertEquals(true, errorShown);
  }
}
