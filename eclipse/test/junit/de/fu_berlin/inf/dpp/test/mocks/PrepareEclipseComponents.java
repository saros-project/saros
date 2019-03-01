package de.fu_berlin.inf.dpp.test.mocks;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.powermock.core.spi.PowerMockPolicy;
import org.powermock.mockpolicies.MockPolicyClassLoadingSettings;
import org.powermock.mockpolicies.MockPolicyInterceptionSettings;

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
    String[] classes = {ResourcesPlugin.class.getName(), Platform.class.getName()};

    settings.addFullyQualifiedNamesOfClassesToLoadByMockClassloader(classes);
  }

  @Override
  public void applyInterceptionPolicy(MockPolicyInterceptionSettings settings) {
    //
  }
}
