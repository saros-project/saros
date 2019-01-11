package de.fu_berlin.inf.dpp.test.mocks;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.preferences.EclipsePreferenceInitializer;
import de.fu_berlin.inf.dpp.preferences.EclipsePreferenceStoreAdapter;
import de.fu_berlin.inf.dpp.test.util.EclipseMemoryPreferenceStore;
import de.fu_berlin.inf.dpp.test.util.MemoryPreferences;
import org.easymock.EasyMock;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;
import org.osgi.service.prefs.Preferences;
import org.picocontainer.MutablePicoContainer;
import org.powermock.api.easymock.PowerMock;

public class EclipseMocker {
  /** Mock the static call {@link ResourcesPlugin#getWorkspace()}. */
  public static void mockResourcesPlugin() {
    IWorkspaceRoot workspaceRoot = EasyMock.createNiceMock(IWorkspaceRoot.class);
    EasyMock.replay(workspaceRoot);

    final IWorkspace workspace = EasyMock.createNiceMock(IWorkspace.class);
    EasyMock.expect(workspace.getRoot()).andStubReturn(workspaceRoot);
    EasyMock.replay(workspace);

    PowerMock.mockStaticPartial(ResourcesPlugin.class, "getWorkspace");
    ResourcesPlugin.getWorkspace();
    EasyMock.expectLastCall().andReturn(workspace).anyTimes();
    PowerMock.replay(ResourcesPlugin.class);
  }

  /** Mock the static call {@link Platform#getBundle(String)}. */
  public static void mockPlatform() {
    Bundle bundle = EasyMock.createNiceMock(Bundle.class);
    EasyMock.expect(bundle.getVersion()).andStubReturn(new Version("1.0.0.dummy"));
    EasyMock.replay(bundle);

    PowerMock.mockStaticPartial(Platform.class, "getBundle");
    Platform.getBundle("org.eclipse.core.runtime");
    EasyMock.expectLastCall().andStubReturn(bundle);
    PowerMock.replay(Platform.class);
  }

  /** Mock a somewhat clever Saros, with a version number and a preference store. */
  public static Saros mockSaros() {
    // saros.getBundle() is final --> need PowerMock
    Saros saros = PowerMock.createNiceMock(Saros.class);

    Bundle bundle = createNiceMock(Bundle.class);
    expect(bundle.getVersion()).andStubReturn(new Version("1.0.0.dummy"));
    expect(saros.getBundle()).andStubReturn(bundle);

    expect(saros.getPreferenceStore()).andStubReturn(new EclipseMemoryPreferenceStore());

    Preferences globalPref = createNiceMock(Preferences.class);
    expect(saros.getGlobalPreferences()).andStubReturn(globalPref);

    replay(bundle, globalPref, saros);

    return saros;
  }

  public static void mockSarosWithPreferences(
      MutablePicoContainer container, final IPreferenceStore store, final Preferences preferences) {

    Saros saros = createNiceMock(Saros.class);

    saros.getPreferenceStore();
    expectLastCall().andStubReturn(store);

    saros.getGlobalPreferences();
    expectLastCall().andStubReturn(preferences);

    replay(saros);

    container.addComponent(Saros.class, saros);
  }

  public static IPreferenceStore initPreferenceStore(MutablePicoContainer container) {
    final IPreferenceStore store = new EclipseMemoryPreferenceStore();
    EclipsePreferenceInitializer.setPreferences(store);

    // Eclipse store interface
    container.addComponent(store);
    // Saros core store interface
    container.addComponent(new EclipsePreferenceStoreAdapter(store));
    return store;
  }

  public static Preferences initPreferences() {
    final Preferences preferences = new MemoryPreferences();
    EclipsePreferenceInitializer.setPreferences(preferences);
    return preferences;
  }
}
