package de.fu_berlin.inf.dpp.context;

import de.fu_berlin.inf.dpp.SarosContext;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.preferences.PreferenceInitializer;
import de.fu_berlin.inf.dpp.test.util.MemorySecurePreferences;
import de.fu_berlin.inf.dpp.test.xmpp.XmppUser;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;

import de.fu_berlin.inf.dpp.Saros;

/**
 * TestClass for using functionality of {@link de.fu_berlin.inf.dpp.Saros}.
 * Overrides the extended constructor. Later this will overrides some
 * eclipsespecific logic and will provide it's own context so it will be
 * possible to start multiple testinstances.
 *
 * @author cordes
 */
public class TestSaros extends Saros {


    private IPreferenceStore _preferenceStore;

    private IWorkspace _workspace;

    public TestSaros() {
        // do not call super().
        log = Logger.getLogger(TestSaros.class);

        sarosContext = SarosContext
                .getContextForSaros(this)
                .isTestContext()
                .build();

        getContext().reinject(this);
        // initialize context !
        getContext().getComponents().size();

        getSarosNet().initialize();
    }

    @Override
    public IPreferenceStore getPreferenceStore() {
        if (_preferenceStore == null) {
            _preferenceStore = new PreferenceStore();
            PreferenceInitializer.setPreferences(_preferenceStore);
            _preferenceStore.setValue(PreferenceConstants.SERVER, "");
            _preferenceStore.setValue(PreferenceConstants.USERNAME, "");
            _preferenceStore.setValue(PreferenceConstants.PASSWORD, "");
            _preferenceStore.setValue(PreferenceConstants.LOCAL_SOCKS5_PROXY_DISABLED, true);
            _preferenceStore.setValue(PreferenceConstants.FORCE_FILETRANSFER_BY_CHAT, true);
            _preferenceStore.setValue(PreferenceConstants.DISABLE_VERSION_CONTROL, true);
            _preferenceStore.setValue(PreferenceConstants.CHATSERVER, "conference.127.0.0.1");
        }
        return _preferenceStore;
    }


    // will be extended

    public IWorkspace getWorspace() {
        return null;
    }

    @Override
    public ISecurePreferences getSecurePrefs() {        
        if (securePrefs == null) {
            securePrefs = new MemorySecurePreferences();
            try {
                securePrefs.put(PreferenceConstants.SERVER, "", false);
                securePrefs.put(PreferenceConstants.USERNAME, "", false);
                securePrefs.put(PreferenceConstants.PASSWORD, "", false);
            } catch (StorageException e) {
                log.error(e);
            }
        }
        return securePrefs;
    }

    public void setXMPPUser(XmppUser user) {
        IPreferenceStore preferenceStore = getPreferenceStore();
        preferenceStore.setValue(PreferenceConstants.USERNAME, user.getUsername());
        preferenceStore.setValue(PreferenceConstants.PASSWORD, user.getPassword());
        preferenceStore.setValue(PreferenceConstants.SERVER, user.getServerAdress());
        try {
            getSecurePrefs().put(PreferenceConstants.SERVER, user.getServerAdress(), false);
            getSecurePrefs().put(PreferenceConstants.USERNAME, user.getUsername(), false);
            getSecurePrefs().put(PreferenceConstants.PASSWORD, user.getPassword(), false);
        } catch (StorageException e) {
            log.error(e);
        }

    }

    public SarosContext getContext() {
        return sarosContext;
    }

    public IWorkspace getWorkspace() {
        return _workspace;
    }

    public void setWorkspace(IWorkspace workspace) {
        _workspace = workspace;
    }

    public String getUserName() {
        return getPreferenceStore().getString(PreferenceConstants.USERNAME);
    }


}
