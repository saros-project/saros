package saros.concurrent.jupiter.test.puzzles;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  ConvergenceProblemTest.class,
  CounterExampleTest.class,
  DOptPuzzleTest.class,
  GOTOInclusionTransformationTest.class,
  InclusionTransformationTest.class,
  SimpleClientServerTest.class,
  SimpleJupiterDocumentTest.class,
  SimpleServerProxyTest.class
})
public class TestSuite {
  // the class remains completely empty,
  // being used only as a holder for the above annotations
}
