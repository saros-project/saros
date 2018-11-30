/** */
package de.fu_berlin.inf.dpp.ui.preferencePages;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.feedback.AbstractFeedbackManager;
import de.fu_berlin.inf.dpp.feedback.ErrorLogManager;
import de.fu_berlin.inf.dpp.feedback.FeedbackInterval;
import de.fu_berlin.inf.dpp.feedback.FeedbackManager;
import de.fu_berlin.inf.dpp.feedback.Messages;
import de.fu_berlin.inf.dpp.feedback.StatisticManager;
import de.fu_berlin.inf.dpp.feedback.StatisticManagerConfiguration;
import de.fu_berlin.inf.dpp.preferences.EclipsePreferenceConstants;
import de.fu_berlin.inf.dpp.ui.util.LinkListener;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.picocontainer.annotations.Inject;

/**
 * The preferences page for the settings concerning the user feedback. The user can enable or
 * disable all automatic requests for participating in the survey and can define the interval in
 * which the requests are shown. <br>
 * Additionally he can start the survey directly.
 *
 * @author Lisa Dohrmann
 */
@Component(module = "prefs")
public class FeedbackPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

  @Inject private IPreferenceStore preferenceStore;

  protected Button radioDisable;
  protected Button radioEnable;
  protected Button allowSubmission;
  protected Button allowErrorLogSubmission;
  protected Button allowFullErrorLogSubmission;
  protected Button allowPseudonym;
  protected Combo intervalCombo;
  protected Text statisticsPseudonymText;
  protected Composite pseudonymityIDGroup;
  protected Composite pseudonymityGroup;

  protected boolean isFeedbackDisabled;
  protected FeedbackInterval currentInterval;
  protected boolean isSubmissionAllowed;
  protected boolean isErrorLogSubmissionAllowed;
  protected boolean isFullErrorLogSubmissionAllowed;
  protected String pseudonymID;
  protected boolean isPseudonymAllowed;

  public FeedbackPreferencePage() {
    SarosPluginContext.initComponent(this);
    setPreferenceStore(preferenceStore);
    setDescription(Messages.getString("feedback.page.description")); // $NON-NLS-1$
  }

  @Override
  public void init(IWorkbench workbench) {
    // nothing to initialize here
  }

  protected void initialize() {
    isFeedbackDisabled = FeedbackManager.isFeedbackDisabled();

    int interval = FeedbackManager.getSurveyInterval();

    currentInterval = FeedbackInterval.getFromInterval(interval);

    isSubmissionAllowed = StatisticManagerConfiguration.isStatisticSubmissionAllowed();

    isPseudonymAllowed = StatisticManagerConfiguration.isPseudonymSubmissionAllowed();

    pseudonymID = StatisticManagerConfiguration.getStatisticsPseudonymID();

    initComponents();
  }

  protected void initComponents() {
    radioDisable.setSelection(isFeedbackDisabled);
    radioEnable.setSelection(!isFeedbackDisabled);

    intervalCombo.select(currentInterval.getIndex());
    setIntervalComboVisible();

    statisticsPseudonymText.setText(pseudonymID);

    allowSubmission.setSelection(isSubmissionAllowed);
    setSubmissionAllowed(isSubmissionAllowed);
    allowPseudonym.setSelection(isPseudonymAllowed);
    setPseudonymAllowed(isPseudonymAllowed);
    allowErrorLogSubmission.setSelection(isErrorLogSubmissionAllowed);
    allowFullErrorLogSubmission.setSelection(isFullErrorLogSubmissionAllowed);

    allowFullErrorLogSubmission.setEnabled(isErrorLogSubmissionAllowed);
  }

  /**
   * Sets the combo box to enabled if the user enabled the feedback reminders or disabled otherwise.
   */
  protected void setIntervalComboVisible() {
    if (intervalCombo == null) return;
    intervalCombo.setEnabled(!isFeedbackDisabled);
  }

  protected void setSubmissionAllowed(boolean allowed) {
    isSubmissionAllowed = allowed;
    allowPseudonym.setEnabled(isSubmissionAllowed);
    statisticsPseudonymText.setEnabled(isSubmissionAllowed && isPseudonymAllowed);
  }

  protected void setPseudonymAllowed(boolean isAllowed) {
    isPseudonymAllowed = isAllowed;
    statisticsPseudonymText.setEnabled(isSubmissionAllowed && isPseudonymAllowed);
  }

  protected void setErrorLogSubmissionAllowed(boolean allowed) {
    isErrorLogSubmissionAllowed = allowed;
    allowFullErrorLogSubmission.setEnabled(allowed);
  }

  @Override
  protected Control createContents(Composite parent) {
    Composite composite = new Composite(parent, SWT.NONE);
    GridLayout layout = new GridLayout(1, true);
    layout.verticalSpacing = 15;
    composite.setLayout(layout);

    createStartSurveyGroup(composite);
    createIntervalGroup(composite);
    createStatisticGroup(composite);
    createErrorLogGroup(composite);
    createContactGroup(composite);

    Label label = new Label(composite, SWT.NONE);
    label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    label.setText(Messages.getString("feedback.page.note")); // $NON-NLS-1$

    initialize();

    return composite;
  }

  /**
   * Creates a group that contains a button which directly starts the survey.
   *
   * @param parent
   */
  protected void createStartSurveyGroup(Composite parent) {
    Group group = new Group(parent, SWT.NONE);
    group.setText(Messages.getString("feedback.page.group.help")); // $NON-NLS-1$
    group.setLayout(new GridLayout(2, false));
    group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    Link link = new Link(group, SWT.WRAP);
    link.setText(Messages.getString("feedback.page.participate.now")); // $NON-NLS-1$
    GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
    gd.widthHint = convertWidthInCharsToPixels(60);
    link.setLayoutData(gd);
    link.addListener(SWT.Selection, new LinkListener());

    Button startSurveyButton = new Button(group, SWT.PUSH);
    startSurveyButton.setText(Messages.getString("feedback.page.survey.start")); // $NON-NLS-1$
    startSurveyButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));

    startSurveyButton.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            FeedbackManager.resetSessionsUntilNextToInterval();
            int browserType = FeedbackManager.showSurvey();
            /*
             * close the preferences window if the internal browser is used
             * so the user can actually see the window
             */
            if (browserType == FeedbackManager.BROWSER_INT)
              FeedbackPreferencePage.this.getShell().close();
          }
        });
  }

  /**
   * Creates a group that contains the interval settings.
   *
   * @param parent the parent composite for the group
   */
  protected void createIntervalGroup(Composite parent) {
    Group group = new Group(parent, SWT.NONE);
    group.setText(Messages.getString("feedback.page.group.interval")); // $NON-NLS-1$
    group.setLayout(new GridLayout(1, false));
    group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    radioDisable = new Button(group, SWT.RADIO);
    radioDisable.setText(Messages.getString("feedback.page.radio.disable")); // $NON-NLS-1$

    radioEnable = new Button(group, SWT.RADIO);
    radioEnable.setText(Messages.getString("feedback.page.radio.enable")); // $NON-NLS-1$

    SelectionListener listener =
        new SelectionAdapter() {

          @Override
          public void widgetSelected(SelectionEvent e) {
            // check if feedback should be disabled or not
            isFeedbackDisabled = radioDisable.getSelection();
            setIntervalComboVisible();
          }
        };
    // listen for selection of the disable button
    radioDisable.addSelectionListener(listener);

    createIntervalCombo(group);
  }

  /**
   * Creates the combo box containing various intervals the user can choose.
   *
   * @param group the parent composite for the combo box
   */
  protected void createIntervalCombo(Composite group) {
    intervalCombo = new Combo(group, SWT.READ_ONLY | SWT.DROP_DOWN);
    intervalCombo.setItems(FeedbackInterval.toStringArray());
    GridData gd = new GridData();
    gd.horizontalIndent = 30;
    gd.widthHint = 200;
    intervalCombo.setLayoutData(gd);
    intervalCombo.addSelectionListener(
        new SelectionAdapter() {

          @Override
          public void widgetSelected(SelectionEvent e) {
            // check which interval was selected
            int selection = intervalCombo.getSelectionIndex();
            currentInterval = FeedbackInterval.getFromIndex(selection);
          }
        });
  }

  /**
   * Create a group that lets the user enable or disable the submission of feedback. It therefore
   * contains a simple check box.
   *
   * @param parent
   */
  protected void createStatisticGroup(Composite parent) {
    Group group = new Group(parent, SWT.NONE);
    group.setText(Messages.getString("feedback.page.group.statistic")); // $NON-NLS-1$
    group.setLayout(new GridLayout(1, false));
    group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    Composite row1 = new Composite(group, SWT.NONE);
    row1.setLayout(new GridLayout(2, false));
    row1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    allowSubmission = new Button(row1, SWT.CHECK);
    allowSubmission.setText(Messages.getString("feedback.page.statistic.allow")); // $NON-NLS-1$

    allowSubmission.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            setSubmissionAllowed(allowSubmission.getSelection());
          }
        });

    Button infoButton = new Button(row1, SWT.PUSH);
    infoButton.setText(Messages.getString("feedback.page.button.more")); // $NON-NLS-1$
    infoButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
    infoButton.addSelectionListener(
        new SelectionAdapter() {

          @Override
          public void widgetSelected(SelectionEvent e) {
            SWTUtils.openExternalBrowser(StatisticManager.INFO_URL);
          }
        });

    pseudonymityGroup = new Composite(group, SWT.NONE);
    pseudonymityGroup.setLayout(new GridLayout(1, false));
    pseudonymityGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    allowPseudonym = new Button(pseudonymityGroup, SWT.CHECK);
    allowPseudonym.setText(
        Messages.getString("feedback.page.statistic.pseudonym.allow")); // $NON-NLS-1$

    allowPseudonym.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            setPseudonymAllowed(allowPseudonym.getSelection());
          }
        });

    pseudonymityIDGroup = new Composite(pseudonymityGroup, SWT.NONE);
    pseudonymityIDGroup.setLayout(new GridLayout(2, false));
    pseudonymityIDGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    Link label = new Link(pseudonymityIDGroup, SWT.NONE);
    label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
    label.setText(Messages.getString("feedback.page.userid")); // $NON-NLS-1$
    label.addListener(SWT.Selection, new LinkListener());

    statisticsPseudonymText = new Text(pseudonymityIDGroup, SWT.SINGLE | SWT.BORDER);
    statisticsPseudonymText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    statisticsPseudonymText.addModifyListener(
        new ModifyListener() {
          @Override
          public void modifyText(ModifyEvent e) {
            pseudonymID = statisticsPseudonymText.getText();
          }
        });

    /*
     * TODO Add a Check-Button to determine whether the user is a member of
     * the Saros team or not. A checked button means: Whatever version of
     * Saros is used, this user is a team member and thus his statistic has
     * to be filtered from the rest.
     */
  }

  protected void createErrorLogGroup(Composite parent) {
    Group group = new Group(parent, SWT.NONE);
    group.setText(Messages.getString("feedback.page.group.error.log")); // $NON-NLS-1$
    group.setLayout(new GridLayout(2, false));
    group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    allowErrorLogSubmission = new Button(group, SWT.CHECK);
    allowErrorLogSubmission.setText(
        Messages.getString("feedback.page.error.log.allow")); // $NON-NLS-1$

    allowErrorLogSubmission.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            setErrorLogSubmissionAllowed(allowErrorLogSubmission.getSelection());
          }
        });

    Button startErrorSubmissionButton = new Button(group, SWT.PUSH);
    startErrorSubmissionButton.setText(
        Messages.getString("feedback.page.error.submit.start")); // $NON-NLS-1$
    startErrorSubmissionButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));

    startErrorSubmissionButton.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            ErrorLogManager.submitErrorLog();
          }
        });

    allowFullErrorLogSubmission = new Button(group, SWT.CHECK);
    allowFullErrorLogSubmission.setText(
        Messages.getString("feedback.page.error.log.full.allow")); // $NON-NLS-1$
    allowFullErrorLogSubmission.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            isFullErrorLogSubmissionAllowed = allowFullErrorLogSubmission.getSelection();
          }
        });

    /*
     * TODO add a button to open the error log directory and let the user
     * pick a log to view it in an editor
     */

  }

  protected void createContactGroup(Composite parent) {
    Group group = new Group(parent, SWT.NONE);
    group.setText(Messages.getString("feedback.page.group.contact")); // $NON-NLS-1$
    group.setLayout(new GridLayout(1, false));
    group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    Link linkMail = new Link(group, SWT.NONE);
    linkMail.setText(Messages.getString("feedback.page.email")); // $NON-NLS-1$
    GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
    gd.widthHint = convertWidthInCharsToPixels(70);
    linkMail.setLayoutData(gd);
    /*
     * TODO maybe use desktop api (java 1.6) here instead to open a new mail
     * directly. right now the browser is first opened and he then opens a
     * mail window.
     */
    linkMail.addListener(SWT.Selection, new LinkListener());

    Link linkBug = new Link(group, SWT.NONE);
    linkBug.setText(Messages.getString("feedback.page.bug.tracker")); // $NON-NLS-1$
    gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
    gd.widthHint = convertWidthInCharsToPixels(70);
    linkBug.setLayoutData(gd);
    linkBug.addListener(SWT.Selection, new LinkListener());
  }

  protected void createSpacer(Composite composite, int columnSpan) {
    Label label = new Label(composite, SWT.NONE);
    GridData gd = new GridData();
    gd.horizontalSpan = columnSpan;
    label.setLayoutData(gd);
  }

  @Override
  protected void performDefaults() {
    // get default values from PreferenceStore
    isFeedbackDisabled =
        getPreferenceStore().getDefaultInt(EclipsePreferenceConstants.FEEDBACK_SURVEY_DISABLED)
            != FeedbackManager.FEEDBACK_ENABLED;
    currentInterval =
        FeedbackInterval.getFromInterval(
            getPreferenceStore()
                .getDefaultInt(EclipsePreferenceConstants.FEEDBACK_SURVEY_INTERVAL));
    setSubmissionAllowed(
        getPreferenceStore().getDefaultInt(EclipsePreferenceConstants.STATISTIC_ALLOW_SUBMISSION)
            == AbstractFeedbackManager.ALLOW);
    setPseudonymAllowed(
        getPreferenceStore()
            .getDefaultBoolean(EclipsePreferenceConstants.STATISTIC_ALLOW_PSEUDONYM));
    setErrorLogSubmissionAllowed(
        getPreferenceStore().getDefaultInt(EclipsePreferenceConstants.ERROR_LOG_ALLOW_SUBMISSION)
            == AbstractFeedbackManager.ALLOW);
    isFullErrorLogSubmissionAllowed =
        getPreferenceStore()
                .getDefaultInt(EclipsePreferenceConstants.ERROR_LOG_ALLOW_SUBMISSION_FULL)
            == AbstractFeedbackManager.ALLOW;

    // initialize components with defaults
    initComponents();
    super.performDefaults();
  }

  @Override
  public boolean performOk() {

    FeedbackManager.setFeedbackDisabled(isFeedbackDisabled);

    FeedbackManager.setSurveyInterval(currentInterval.getInterval());

    StatisticManagerConfiguration.setStatisticSubmissionAllowed(isSubmissionAllowed);

    StatisticManagerConfiguration.setPseudonymSubmissionAllowed(isPseudonymAllowed);

    StatisticManagerConfiguration.setStatisticsPseudonymID(pseudonymID);

    ErrorLogManager.setErrorLogSubmissionAllowed(isErrorLogSubmissionAllowed);

    ErrorLogManager.setFullErrorLogSubmissionAllowed(isFullErrorLogSubmissionAllowed);

    return super.performOk();
  }
}
