package de.fu_berlin.inf.dpp.test.mocks;

import org.powermock.core.spi.PowerMockPolicy;
import org.powermock.mockpolicies.MockPolicyClassLoadingSettings;
import org.powermock.mockpolicies.MockPolicyInterceptionSettings;

import de.fu_berlin.inf.dpp.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.editor.colorstorage.ColorIDSetStorage;
import de.fu_berlin.inf.dpp.observables.SessionNegotiationObservable;

/**
 * This policy can be used to avoid reiterating which core components need to be
 * prepared by PowerMock because they are final.
 * <p>
 * Usage:
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
    @Override
    public void applyClassLoadingPolicy(MockPolicyClassLoadingSettings settings) {
        // Add more final classes here, if need be
        String[] classes = { XMPPAccountStore.class.getName(),
            ColorIDSetStorage.class.getName(),
            SessionNegotiationObservable.class.getName() };

        settings
            .addFullyQualifiedNamesOfClassesToLoadByMockClassloader(classes);
    }

    @Override
    public void applyInterceptionPolicy(MockPolicyInterceptionSettings settings) {
        // nothing to do here
    }
}
