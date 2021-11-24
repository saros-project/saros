package saros;

import java.util.List;
import javax.annotation.PostConstruct;
import org.apache.log4j.Logger;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import saros.account.XMPPAccountStore;
import saros.preferences.Preferences;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.ui.util.SWTUtils;
import saros.ui.util.WizardUtils;
import saros.ui.util.XMPPConnectionSupport;

/**
 * An instance of this class is instantiated when Eclipse starts, after the
 * Saros plugin has been started.
 *
 * <p>
 * {@link #earlyStartup()} is called after the workbench is initialized.
 */
public class StartupSaros {

    private static final Logger log = Logger.getLogger(StartupSaros.class);

    @Inject
    private Preferences preferences;

    @Inject
    private XMPPAccountStore xmppAccountStore;

    /*
     * Once the workbench is started, the method earlyStartup() will be called
     * from a separate thread
     */

    @PostConstruct
    public void earlyStartup(IEventBroker eb, EPartService partService,
        EModelService service, MApplication app) {
        SarosPluginContext.initComponent(this);

        eb.subscribe(UIEvents.UILifeCycle.APP_STARTUP_COMPLETE,
            new EventHandler() {

                @Override
                public void handleEvent(Event event) {

                    // Create the View
                    MPart sarosView = partService
                        .createPart("saros.ui.views.SarosView");
                    MWindow mainWindow = app.getChildren().get(0);

                    List<MPartStack> parts = service.findElements(mainWindow,
                        "bottomRight", MPartStack.class, null);

                    if (parts.size() > 0) {
                        MPartStack partStackSaros = service
                            .createModelElement(MPartStack.class);
                        partStackSaros.getChildren().add(sarosView);
                        MPartSashContainer partSashContainer = service
                            .createModelElement(MPartSashContainer.class);
                        MPartStack partStackBottomRight = parts.get(0);
                        MElementContainer<MUIElement> parent = partStackBottomRight
                            .getParent();
                        parent.getChildren().remove(partStackBottomRight);
                        partSashContainer.getChildren()
                            .add(partStackBottomRight);
                        partSashContainer.getChildren().add(partStackSaros);
                        partSashContainer.setHorizontal(true);
                        parent.getChildren().add(partSashContainer);
                    }

                    Integer testmode = Integer.getInteger("saros.testmode");

                    if (testmode != null)
                        return;

                    /*
                     * Only show the Configuration Wizard if no accounts are
                     * configured. If Saros is already configured, do not show
                     * the tutorial because the user is probably already
                     * experienced.
                     */

                    /*
                     * TODO first display a dialog if the user wants to get some
                     * help. Afterwards open this wizard and maybe also open a
                     * web site with getting started?
                     */

                    if (xmppAccountStore.isEmpty()) {
                        SWTUtils.runSafeSWTAsync(log,
                            WizardUtils::openSarosConfigurationWizard);
                        return;
                    }

                    /*
                     * HACK workaround for
                     * http://sourceforge.net/p/dpp/bugs/782/ Perform connecting
                     * after the view is created so that the necessary GUI
                     * elements for the chat have already installed their
                     * listeners.
                     *
                     * FIXME This will not work if the view is not created on
                     * startup !
                     */

                    if (preferences.isAutoConnecting()
                        && xmppAccountStore.getDefaultAccount() != null)
                        SWTUtils.runSafeSWTAsync(log,
                            () -> XMPPConnectionSupport.getInstance()
                                .connect(true));

                    eb.unsubscribe(this);
                }
            });
    }
}
