package saros.test.mocks;

import org.powermock.core.spi.PowerMockPolicy;
import org.powermock.mockpolicies.MockPolicyClassLoadingSettings;
import org.powermock.mockpolicies.MockPolicyInterceptionSettings;
import saros.account.XMPPAccountStore;
import saros.editor.colorstorage.ColorIDSetStorage;
import saros.negotiation.NegotiationFactory;

/**
 * This policy can be used to avoid reiterating which core components need to be prepared by
 * PowerMock because they are final.
 *
 * <p>Usage:
 *
 * <pre>
 * {@literal @}RunWith(PowerMockRunner.class)
 * {@literal @}MockPolicy( { PrepareFinalCoreComponents.class } )
 * public class MyTestClass {
 *     // ...
 * }
 * </pre>
 */
public class PrepareCoreComponents implements PowerMockPolicy {

  private static final Class<?>[] finalClasses =
  // Add more final classes here, if need be
  {NegotiationFactory.class, XMPPAccountStore.class, ColorIDSetStorage.class};

  @Override
  public void applyClassLoadingPolicy(MockPolicyClassLoadingSettings settings) {

    settings.addFullyQualifiedNamesOfClassesToLoadByMockClassloader(toClassNames(finalClasses));
  }

  @Override
  public void applyInterceptionPolicy(MockPolicyInterceptionSettings settings) {
    // nothing to do here
  }

  private static String[] toClassNames(Class<?>[] classes) {
    String[] classNames = new String[classes.length];

    int i = 0;

    for (Class<?> clazz : classes) classNames[i++] = clazz.getName();

    return classNames;
  }
}
