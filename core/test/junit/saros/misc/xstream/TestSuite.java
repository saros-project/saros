package saros.misc.xstream;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  ReplaceableConverterTest.class,
  ReplaceableSingleValueConverterTest.class,
  SPathConverterTest.class,
  UserConverterTest.class
})
public class TestSuite {
  // the class remains completely empty,
  // being used only as a holder for the above annotations
}
