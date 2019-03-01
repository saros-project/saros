package saros.whiteboard.ui.browser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.log4j.Logger;
import saros.whiteboard.net.WhiteboardManager;
import saros.whiteboard.sxe.ISXEMessageHandler.MessageListener;
import saros.whiteboard.sxe.SXEController;
import saros.whiteboard.sxe.net.SXEMessage;
import saros.whiteboard.sxe.records.ElementRecord;

/**
 * BrowserSXEBridge is the java interface between the SXEController which contains all model data
 * and the browser which contains the whiteboard and manipulates the data.
 */
public class BrowserSXEBridge {

  private static final Logger LOG = Logger.getLogger(BrowserSXEBridge.class);

  private static final Gson gson = new Gson();

  private static Gson gsonOnlyExposed =
      new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

  private static final String JAVA_FUNCTION_NAME = "__java_triggerAction";

  private final SXEController sxe;

  private final IWhiteboardBrowser browser;

  private final BrowserActionExecuter executer;

  /**
   * Creates the action executor and initialises the message listener and the browser function
   *
   * @param sxe SXEController created in {@link WhiteboardManager}
   * @param browser the browser created in the view
   */
  public BrowserSXEBridge(SXEController sxe, IWhiteboardBrowser browser) {
    this.sxe = sxe;
    this.browser = browser;
    executer = new BrowserActionExecuter(sxe);
  }

  public void init() {
    sxe.addMessageListener(messageListener);
    browser.createBrowserFunction(JAVA_FUNCTION_NAME, browserFunction);
    // send current state to browser
    browser.asyncRun(
        "setState(" + gsonOnlyExposed.toJson(sxe.getDocumentRecord().getRoot()) + ");");
    LOG.debug("BrowserSXEBridge has been initialised");
  }

  private final MessageListener messageListener =
      new MessageListener() {
        @Override
        public void sxeRecordMessageApplied(SXEMessage message) {
          browser.asyncRun("message(" + gson.toJson(message) + ");");
        }

        @Override
        public void sxeStateMessageApplied(SXEMessage message, ElementRecord root) {
          browser.asyncRun("setState(" + gsonOnlyExposed.toJson(root) + ");");
        }

        @Override
        public void sxeMessageSent(SXEMessage message) {
          /*
           * in theory we don't need to do anything here, it is however
           * possible to listen to sent messages so we can also show changes
           * from the old whiteboard
           */
        }
      };

  private final BrowserRunnable browserFunction =
      new BrowserRunnable() {
        @Override
        public Object run(Object[] arguments) {
          String json = (String) arguments[0];
          BrowserAction action = gson.fromJson(json, BrowserAction.class);
          executer.execute(action);
          return true;
        }
      };

  /** Removes the message listener and clears the whiteboard in browser. */
  public void dispose() {
    sxe.removeMessageListener(messageListener);
    LOG.debug("BrowserSXEBridge has been disposed");
  }
}
