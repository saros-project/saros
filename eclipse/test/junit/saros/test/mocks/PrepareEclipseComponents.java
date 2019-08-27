package saros.test.mocks;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Display;
import org.powermock.core.spi.PowerMockPolicy;
import org.powermock.mockpolicies.MockPolicyClassLoadingSettings;
import org.powermock.mockpolicies.MockPolicyInterceptionSettings;
import saros.ui.util.SWTUtils;

/**
 * This policy can be used to avoid reiterating Eclipse components that need to be prepared by
 * PowerMock because they are final.
 *
 * <p>Usage:
 *
 * <pre>
 * {@literal @}RunWith(PowerMockRunner.class)
 * {@literal @}MockPolicy( { PrepareEclipseComponents.class } )
 * public class MyTestClass {
 *     // ...
 * }
 * </pre>
 */
public class PrepareEclipseComponents implements PowerMockPolicy {
  @Override
  public void applyClassLoadingPolicy(MockPolicyClassLoadingSettings settings) {
    // Add more final classes here, if need be
    String[] classes = {
      Display.class.getName(),
      SWTUtils.class.getName(),
      ResourcesPlugin.class.getName(),
      Platform.class.getName()
    };

    settings.addFullyQualifiedNamesOfClassesToLoadByMockClassloader(classes);
  }

  @Override
  public void applyInterceptionPolicy(MockPolicyInterceptionSettings settings) {
    // NOP
  }
}
