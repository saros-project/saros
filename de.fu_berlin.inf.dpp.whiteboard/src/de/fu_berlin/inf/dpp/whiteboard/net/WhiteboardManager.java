package de.fu_berlin.inf.dpp.whiteboard.net;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.editor.annotations.SarosAnnotation;
import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;
import de.fu_berlin.inf.dpp.monitoring.ProgressMonitorAdapterFactory;
import de.fu_berlin.inf.dpp.negotiation.hooks.ISessionNegotiationHook;
import de.fu_berlin.inf.dpp.negotiation.hooks.SessionNegotiationHookManager;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.preferences.IPreferenceStore;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISessionLifecycleListener;
import de.fu_berlin.inf.dpp.session.SarosSessionManager;
import de.fu_berlin.inf.dpp.session.SessionEndReason;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.whiteboard.gef.model.GEFRecordFactory;
import de.fu_berlin.inf.dpp.whiteboard.gef.util.ColorUtils;
import de.fu_berlin.inf.dpp.whiteboard.sxe.ISXEMessageHandler;
import de.fu_berlin.inf.dpp.whiteboard.sxe.SXEController;
import de.fu_berlin.inf.dpp.whiteboard.sxe.SXEController.State;
import de.fu_berlin.inf.dpp.whiteboard.sxe.net.SXEOutgoingSynchronizationProcess;
import de.fu_berlin.inf.dpp.whiteboard.ui.browser.BrowserSXEBridge;
import de.fu_berlin.inf.dpp.whiteboard.ui.browser.IWhiteboardBrowser;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.RGB;
import org.picocontainer.annotations.Inject;

/**
 * This class makes the interconnection between Saros and SXE.
 *
 * <p>The singleton instantiated on plug-in StartUp (see plugin.xml).
 *
 * @author jurke
 */
/*
 * Note: this class is not yet final because a lot of changes are about to
 * happen respective the Saros invitation or network layer
 */
public class WhiteboardManager {

  private static final Logger LOG = Logger.getLogger(WhiteboardManager.class);

  private static final Object LOCK = new Object();

  private static final WhiteboardManager INSTANCE = new WhiteboardManager();

  protected SXEController controller;

  /** Only used by the client */
  private boolean hostHasWhiteboard;
  /** Only used by the Host, for representing which client has a whiteboard */
  private final Map<JID, Boolean> hasWhiteboard = new HashMap<JID, Boolean>();

  // this bridge will connect the sxe controller with the browser
  private BrowserSXEBridge bridge;

  public static WhiteboardManager getInstance() {
    return INSTANCE;
  }

  /**
   * The session life-cycle listener lets the host initialize a local session, enables for other
   * peers to be invited and let the host start the synchronization process.
   */
  protected ISessionLifecycleListener sessionLifecycleListener =
      new ISessionLifecycleListener() {

        @Override
        public void postOutgoingInvitationCompleted(
            ISarosSession session, User user, IProgressMonitor monitor) {

          // This method might be called concurrently in case multiple users
          // are invited at once.
          synchronized (LOCK) {
            if (!hasWhiteboard.get(user.getJID())) {
              LOG.debug(
                  "Skip Whiteboard preparation because "
                      + user
                      + " has no with Whiteboard support");
              return;
            }

            // This preparation needs to be performed only once, and we are
            // doing it lazy: When the first client with a Whiteboard
            // completed his SessionNegotiation.
            if (controller.getState().equals(State.DISCONNECTED)) {
              LOG.debug("Preparing Whiteboard for sending an invitation");
              setupColorAndTransmitter(session);
              controller.startSession();
            }

            SXEOutgoingSynchronizationProcess inv =
                new SXEOutgoingSynchronizationProcess(
                    controller, sxeTransmitter, user.getJID().toString());
            inv.start(ProgressMonitorAdapterFactory.convert(monitor));
          }
        }

        @Override
        public void sessionStarted(ISarosSession session) {

          if (!hostHasWhiteboard) {
            LOG.debug("Skip Whiteboard preparation because session host has no Whiteboard");
            return;
          }

          LOG.debug("Preparing Whiteboard for receiving an invitation");

          setupColorAndTransmitter(session);
          sxeTransmitter.enableInvitation(controller);
        }

        @Override
        public void sessionEnded(ISarosSession session, SessionEndReason reason) {
          hostHasWhiteboard = false;
          hasWhiteboard.clear();

          controller.setDisconnected();

          /*
           * dispose because we do not want to be invited when not in a Saros
           * session and the transmitter will be recreated on start
           */
          dispose();
        }
      };

  /**
   * This hook determines whether there are Whiteboard counterparts on the remote sides to
   * communicate with. The result will be stored in {@link #hasWhiteboard} and {@link
   * #hostHasWhiteboard}.
   */
  private final ISessionNegotiationHook hook =
      new ISessionNegotiationHook() {

        private static final String IDENTIFIER = "whiteboard";
        private static final String USE_KEY = "useWhiteboard";
        private static final String USE_VAL = "true";

        @Override
        public String getIdentifier() {
          return IDENTIFIER;
        }

        @Override
        public void setInitialHostPreferences(IPreferenceStore hostPreferences) {
          // NOP
        }

        @Override
        public Map<String, String> tellClientPreferences() {
          Map<String, String> map = new HashMap<String, String>();
          map.put(USE_KEY, USE_VAL);
          return map;
        }

        @Override
        public Map<String, String> considerClientPreferences(
            JID client, Map<String, String> input) {

          // This method might be called concurrently in case multiple
          // client are invited at once.
          synchronized (LOCK) {
            boolean has = (input != null && USE_VAL.equals(input.get(USE_KEY)));

            LOG.debug("Detected " + (has ? "" : "no") + " Whiteboard at " + client);

            hasWhiteboard.put(client, has);

            return has ? input : null;
          }
        }

        @Override
        public void applyActualParameters(
            Map<String, String> input,
            IPreferenceStore hostPreferences,
            IPreferenceStore clientPreferences) {

          hostHasWhiteboard = (input != null && USE_VAL.equals(input.get(USE_KEY)));
        }
      };

  @Inject private SarosSessionManager sessionManager;

  @Inject private SessionNegotiationHookManager hooks;

  private SarosSXETransmitter sxeTransmitter;

  private WhiteboardManager() {

    SarosPluginContext.initComponent(this);

    sessionManager.addSessionLifecycleListener(sessionLifecycleListener);
    hooks.addHook(hook);

    LOG.debug("WhiteboardManager instantiated");

    controller = new SXEController(new GEFRecordFactory());
  }

  private void setupColorAndTransmitter(final ISarosSession session) {

    SWTUtils.runSafeSWTSync(
        LOG,
        new Runnable() {

          @Override
          public void run() {
            RGB color = SarosAnnotation.getUserColor(session.getLocalUser()).getRGB();

            ColorUtils.setForegroundColor(color);
          }
        });

    sxeTransmitter = new SarosSXETransmitter(session);
    controller.initNetwork(sxeTransmitter);
  }

  public ISXEMessageHandler getSXEMessageHandler() {
    return controller;
  }

  private void dispose() {
    controller.clear();
    if (bridge != null) {
      bridge.dispose();
    }
    if (sxeTransmitter != null) sxeTransmitter.dispose();
  }

  /**
   * Instantiates the bridge with the given browser.
   *
   * <p>note: it would be best to call this function after the browser has fully loaded the document
   * to guarantee proper browser function initialisation
   *
   * @param browser the browser created in the view which displays the whiteboard html document
   */
  public void createBridge(IWhiteboardBrowser browser) {
    if (bridge != null) {
      bridge.dispose();
    }
    bridge = new BrowserSXEBridge(controller, browser);
    bridge.init();
  }
}
