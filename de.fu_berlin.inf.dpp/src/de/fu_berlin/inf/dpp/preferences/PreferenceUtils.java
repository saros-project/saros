package de.fu_berlin.inf.dpp.preferences;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.Mixer;

import org.apache.log4j.Logger;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.preference.PreferenceStore;
import org.xiph.speex.spi.SpeexEncoding;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.communication.audio.MixerManager;
import de.fu_berlin.inf.dpp.net.JID;

@Component(module = "prefs")
public class PreferenceUtils {

    protected static Logger log = Logger.getLogger(PreferenceUtils.class
        .getName());

    protected Saros saros;
    protected MixerManager mixerManager;
    protected ISecurePreferences securePreferenceStore;

    public PreferenceUtils(Saros saros, MixerManager mixerManager) {
        this.saros = saros;
        this.mixerManager = mixerManager;
        this.securePreferenceStore = saros.getSecurePrefs();
    }

    public boolean isDebugEnabled() {
        return saros.getPreferenceStore().getBoolean(PreferenceConstants.DEBUG);
    }

    public List<JID> getAutoInviteUsers() {

        String autoInvite = saros.getPreferenceStore().getString(
            PreferenceConstants.AUTO_INVITE);

        if (autoInvite == null || autoInvite.trim().length() == 0)
            return Collections.emptyList();

        List<JID> result = new LinkedList<JID>();

        for (String user : autoInvite.split(",")) {
            result.add(new JID(user.trim()));
        }
        return result;
    }

    public boolean isAutoReuseExisting() {
        return saros.getPreferenceStore().getBoolean(
            PreferenceConstants.AUTO_REUSE_PROJECT);
    }

    public boolean isAutoAcceptInvitation() {
        return saros.getPreferenceStore().getBoolean(
            PreferenceConstants.AUTO_ACCEPT_INVITATION);
    }

    /**
     * Returns the server from the {@link PreferenceStore}.<br/>
     * Might be an empty string but never null.
     * 
     * @return
     */
    public String getServer() {
        ISecurePreferences prefs = saros.getSecurePrefs();
        String server = "";

        try {
            server = prefs.get(PreferenceConstants.SERVER, "");
        } catch (StorageException e) {
            log.error("Exception while retrieving account: " + e.getMessage());
        }
        return server;
    }

    /**
     * Returns true if the user has specified a server for a XMPP account.
     * 
     * @return true if there is a user name that is not equal to the empty
     *         string
     */
    public boolean hasServer() {
        return getServer().length() > 0;
    }

    /**
     * Returns Saros's XMPP/Jabber server dns address.
     * 
     * @return
     */
    public String getSarosXMPPServer() {
        return "saros-con.imp.fu-berlin.de";
    }

    /**
     * Returns the default server.<br/>
     * Is never empty or null.
     * 
     * @return
     */
    public String getDefaultServer() {
        return getSarosXMPPServer();
    }

    /**
     * Returns the user name from the {@link ISecurePreferences}.<br/>
     * Might be an empty string but never null.
     * 
     * @return User name
     */
    public String getUserName() {
        ISecurePreferences prefs = saros.getSecurePrefs();
        String username = "";

        try {
            username = prefs.get(PreferenceConstants.USERNAME, "");
        } catch (StorageException e) {
            log.error("Exception while retrieving account: " + e.getMessage());
        }
        return username;
    }

    /**
     * Returns true if the user has specified a user name for a XMPP account.
     * 
     * @return true if there is a user name that is not equal to the empty
     *         string
     */
    public boolean hasUserName() {
        return getUserName().length() > 0;
    }

    /**
     * Returns the user's password from the {@link PreferenceStore}.<br/>
     * Might be an empty string but never null.
     * 
     * @return
     */
    public String getPassword() {
        String password = "";
        try {
            password = this.securePreferenceStore.get(
                PreferenceConstants.PASSWORD, "");
        } catch (StorageException e) {
            log.error("Exception while getting password: " + e.getMessage());
        }

        return password;
    }

    /**
     * Returns whether auto-connect is enabled or not.
     * 
     * @return true if auto-connect is enabled.
     */
    public boolean isAutoConnecting() {
        return saros.getPreferenceStore().getBoolean(
            PreferenceConstants.AUTO_CONNECT);
    }

    /**
     * Returns the Skype user name or an empty string if none was specified.
     * 
     * @return the user name.for Skype or an empty string
     */
    public String getSkypeUserName() {
        return saros.getPreferenceStore().getString(
            PreferenceConstants.SKYPE_USERNAME);
    }

    /**
     * Returns the port for SOCKS5 file transfer. If
     * {@link PreferenceConstants#USE_NEXT_PORTS_FOR_FILE_TRANSFER} is set, a
     * negative number is returned (smacks will try next free ports above this
     * number)
     * 
     * @return port for smacks configuration (negative if to try out ports
     *         above)
     */
    public int getFileTransferPort() {
        int port = saros.getPreferenceStore().getInt(
            PreferenceConstants.FILE_TRANSFER_PORT);

        if (saros.getPreferenceStore().getBoolean(
            PreferenceConstants.USE_NEXT_PORTS_FOR_FILE_TRANSFER))
            return -port;
        else
            return port;
    }

    public boolean isSkipSyncSelectable() {
        return saros.getPreferenceStore().getBoolean(
            PreferenceConstants.SKIP_SYNC_SELECTABLE);
    }

    public boolean forceFileTranserByChat() {
        return saros.getPreferenceStore().getBoolean(
            PreferenceConstants.FORCE_FILETRANSFER_BY_CHAT);
    }

    public boolean isConcurrentUndoActivated() {
        return saros.getPreferenceStore().getBoolean(
            PreferenceConstants.CONCURRENT_UNDO);
    }

    public boolean isPingPongActivated() {
        return saros.getPreferenceStore().getBoolean(
            PreferenceConstants.PING_PONG);
    }

    public boolean useVersionControl() {
        return !saros.getPreferenceStore().getBoolean(
            PreferenceConstants.DISABLE_VERSION_CONTROL);
    }

    public void setUseVersionControl(boolean value) {
        saros.getPreferenceStore().setValue(
            PreferenceConstants.DISABLE_VERSION_CONTROL, !value);
    }

    public Mixer getRecordingMixer() {
        return mixerManager.getMixerByName(saros.getPreferenceStore()
            .getString(PreferenceConstants.AUDIO_RECORD_DEVICE));
    }

    public Mixer getPlaybackMixer() {
        return mixerManager.getMixerByName(saros.getPreferenceStore()
            .getString(PreferenceConstants.AUDIO_PLAYBACK_DEVICE));
    }

    public AudioFormat getEncodingFormat() {
        Encoding encoding;
        float sampleRate = Float.parseFloat(saros.getPreferenceStore()
            .getString(PreferenceConstants.AUDIO_SAMPLERATE));
        int quality = Integer.parseInt(saros.getPreferenceStore().getString(
            PreferenceConstants.AUDIO_QUALITY_LEVEL));
        boolean vbr = saros.getPreferenceStore().getBoolean(
            PreferenceConstants.AUDIO_VBR);

        Encoding encodingsVbr[] = new Encoding[] { SpeexEncoding.SPEEX_VBR0,
            SpeexEncoding.SPEEX_VBR1, SpeexEncoding.SPEEX_VBR2,
            SpeexEncoding.SPEEX_VBR3, SpeexEncoding.SPEEX_VBR4,
            SpeexEncoding.SPEEX_VBR5, SpeexEncoding.SPEEX_VBR6,
            SpeexEncoding.SPEEX_VBR7, SpeexEncoding.SPEEX_VBR8,
            SpeexEncoding.SPEEX_VBR9, SpeexEncoding.SPEEX_VBR10 };

        Encoding encodingsCbr[] = new Encoding[] { SpeexEncoding.SPEEX_Q0,
            SpeexEncoding.SPEEX_Q1, SpeexEncoding.SPEEX_Q2,
            SpeexEncoding.SPEEX_Q3, SpeexEncoding.SPEEX_Q4,
            SpeexEncoding.SPEEX_Q5, SpeexEncoding.SPEEX_Q6,
            SpeexEncoding.SPEEX_Q7, SpeexEncoding.SPEEX_Q8,
            SpeexEncoding.SPEEX_Q9, SpeexEncoding.SPEEX_Q10 };

        if (vbr) {
            encoding = encodingsVbr[quality];
        } else {
            encoding = encodingsCbr[quality];
        }
        return new AudioFormat(encoding, sampleRate, 16, 1, 2, sampleRate,
            false);
    }

    public boolean isDtxEnabled() {
        return saros.getPreferenceStore().getBoolean(
            PreferenceConstants.AUDIO_ENABLE_DTX);
    }

    public boolean isLocalSOCKS5ProxyEnabled() {
        return !saros.getPreferenceStore().getBoolean(
            PreferenceConstants.LOCAL_SOCKS5_PROXY_DISABLED);
    }

}
