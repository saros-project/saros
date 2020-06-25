package saros.ui.wizards.pages;

import java.text.MessageFormat;
import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import saros.SarosPluginContext;
import saros.net.xmpp.JID;
import saros.ui.ImageManager;
import saros.ui.util.FontUtils;
import saros.ui.util.LayoutUtils;
import saros.ui.widgets.SimpleIllustratedComposite;
import saros.ui.widgets.SimpleIllustratedComposite.IllustratedText;
import saros.ui.widgets.SimpleNoteComposite;
import saros.ui.widgets.wizard.SummaryItemComposite;
import saros.ui.wizards.ConfigurationWizard;

/**
 * Final {@link WizardPage} for the {@link ConfigurationWizard} that summarizes the made
 * configuration.
 *
 * @author bkahlert
 */
public class ConfigurationSummaryWizardPage extends WizardPage {

  private static final Logger log = Logger.getLogger(ConfigurationSummaryWizardPage.class);

  public static final String TITLE = saros.ui.Messages.ConfigurationSummaryWizardPage_title;
  public static final String DESCRIPTION =
      saros.ui.Messages.ConfigurationSummaryWizardPage_description;

  private Composite composite;
  private SimpleIllustratedComposite jid;
  private SimpleIllustratedComposite autoConnection;
  private SimpleIllustratedComposite uPnPOption;
  private SimpleIllustratedComposite skypeUsername;

  private final EnterXMPPAccountWizardPage accountPage;
  private final ConfigurationSettingsWizardPage configurationPage;

  public ConfigurationSummaryWizardPage(
      final EnterXMPPAccountWizardPage accountPage,
      final ConfigurationSettingsWizardPage configurationPage) {
    super(ConfigurationSummaryWizardPage.class.getName());
    SarosPluginContext.initComponent(this);
    setTitle(TITLE);
    setDescription(DESCRIPTION);

    this.accountPage = accountPage;
    this.configurationPage = configurationPage;
  }

  @Override
  public void createControl(Composite parent) {
    composite = new Composite(parent, SWT.NONE);
    composite.setLayout(LayoutUtils.createGridLayout(2, true, 5, 10));
    setControl(composite);

    Composite leftColumn = createLeftColumn(composite);
    leftColumn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    Composite rightColumn = createRightColumn(composite);
    rightColumn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    updatePageCompletion();
  }

  protected Composite createLeftColumn(Composite composite) {
    Composite leftColumn = new Composite(composite, SWT.NONE);
    leftColumn.setLayout(LayoutUtils.createGridLayout(5, 0));
    // leftColumn.setText("Complete");

    /*
     * auto connect
     */
    Composite autoConnectComposite = new Composite(leftColumn, SWT.NONE);
    autoConnectComposite.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, true));
    autoConnectComposite.setLayout(LayoutUtils.createGridLayout(0, 5));

    Label successLabel = new Label(autoConnectComposite, SWT.WRAP);
    successLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    successLabel.setText(saros.ui.Messages.ConfigurationSummaryWizardPage_label_success);
    FontUtils.makeBold(successLabel);

    SimpleNoteComposite check =
        new SimpleNoteComposite(
            autoConnectComposite,
            SWT.BORDER,
            ImageManager.ELCL_PREFERENCES_OPEN,
            saros.ui.Messages.ConfigurationSummaryWizardPage_check_settings);
    check.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    check.setSpacing(5);

    SimpleNoteComposite addContacts =
        new SimpleNoteComposite(
            autoConnectComposite,
            SWT.BORDER,
            ImageManager.ELCL_CONTACT_ADD,
            saros.ui.Messages.ConfigurationSummaryWizardPage_addContacts);

    addContacts.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    addContacts.setSpacing(5);

    SimpleNoteComposite shareResources =
        new SimpleNoteComposite(
            autoConnectComposite,
            SWT.BORDER,
            ImageManager.ELCL_SESSION,
            saros.ui.Messages.ConfigurationSummaryWizardPage_share_resources);
    shareResources.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    shareResources.setSpacing(5);

    return leftColumn;
  }

  protected Composite createRightColumn(Composite composite) {
    Group rightColumn = new Group(composite, SWT.NONE);
    rightColumn.setLayout(LayoutUtils.createGridLayout(5, 0));
    rightColumn.setText(
        saros.ui.Messages.ConfigurationSummaryWizardPage_right_column_your_configuration);

    /*
     * jid settings
     */
    Composite jidSettingsComposite = new Composite(rightColumn, SWT.NONE);
    jidSettingsComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
    jidSettingsComposite.setLayout(LayoutUtils.createGridLayout(0, 5));

    jid = new SummaryItemComposite(jidSettingsComposite, SWT.BOLD);
    jid.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    /*
     * separator
     */
    new Label(rightColumn, SWT.SEPARATOR | SWT.HORIZONTAL)
        .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    /*
     * network settings
     */
    Composite networkSettingsComposite = new Composite(rightColumn, SWT.NONE);
    networkSettingsComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
    networkSettingsComposite.setLayout(LayoutUtils.createGridLayout(0, 5));

    autoConnection = new SummaryItemComposite(networkSettingsComposite, SWT.NONE);
    autoConnection.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    uPnPOption = new SummaryItemComposite(networkSettingsComposite, SWT.NONE);
    uPnPOption.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    skypeUsername = new SummaryItemComposite(networkSettingsComposite, SWT.NONE);
    skypeUsername.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    return rightColumn;
  }

  protected void updatePageCompletion() {
    setPageComplete(true);
  }

  @Override
  public void setVisible(boolean visible) {
    super.setVisible(visible);

    if (!visible) return;

    if (accountPage == null || configurationPage == null) return;

    JID accountJID = accountPage.getJID();

    boolean autoConnect = configurationPage.isAutoConnect();

    boolean uPnPEnabled = configurationPage.getPortmappingDevice() != null;

    String currentSkypeUsername =
        configurationPage.isSkypeUsage() ? configurationPage.getSkypeUsername() : ""; // $NON-NLS-1$

    if (jid != null) {
      jid.setContent(new IllustratedText(ImageManager.ELCL_SPACER, accountJID.getBase()));
    }

    if (autoConnect) {
      autoConnection.setContent(
          new IllustratedText(
              ImageManager.ELCL_XMPP_CONNECTED,
              saros.ui.Messages.ConfigurationSummaryWizardPage_connect_auto));
    } else {
      autoConnection.setContent(
          new IllustratedText(
              ImageManager.DLCL_XMPP_CONNECTED,
              saros.ui.Messages.ConfigurationSummaryWizardPage_connect_auto_not));
    }

    if (uPnPEnabled) {
      uPnPOption.setContent(
          new IllustratedText(
              ImageManager.ICON_UPNP, saros.ui.Messages.ConfigurationSummaryWizardPage_use_upnp));
    } else {

      Image disabledUPnP = null;
      try {
        disabledUPnP = new Image(null, ImageManager.ICON_UPNP, SWT.IMAGE_DISABLE);
      } catch (Exception e) {
        log.debug("Unable to convert image:" + e.getMessage()); // $NON-NLS-1$
      }

      uPnPOption.setContent(
          new IllustratedText(
              disabledUPnP, saros.ui.Messages.ConfigurationSummaryWizardPage_use_upnp_not));
    }

    if (!currentSkypeUsername.isEmpty()) {
      this.skypeUsername.setContent(
          new IllustratedText(
              ImageManager.ELCL_CONTACT_SKYPE_CALL,
              MessageFormat.format(
                  saros.ui.Messages.ConfigurationSummaryWizardPage_skype_show_username,
                  currentSkypeUsername)));
    } else {
      this.skypeUsername.setContent(
          new IllustratedText(
              ImageManager.DLCL_CONTACT_SKYPE_CALL,
              saros.ui.Messages.ConfigurationSummaryWizardPage_skype_show_username_not));
    }

    composite.layout(false, true);
  }
}
