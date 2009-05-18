/**
 * 
 */
package de.fu_berlin.inf.dpp.ui;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
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
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.feedback.FeedbackManager;
import de.fu_berlin.inf.dpp.feedback.Messages;
import de.fu_berlin.inf.dpp.feedback.StatisticManager;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;

/**
 * The preferences page for the settings concerning the user feedback. The user
 * can enable or disable all automatic requests for participating in the survey
 * and can define the interval in which the requests are shown. <br>
 * Additionally he can start the survey directly.
 * 
 * @author Lisa Dohrmann
 */
public class FeedbackPreferencePage extends
    org.eclipse.jface.preference.PreferencePage implements
    IWorkbenchPreferencePage {

    @Inject
    protected Saros saros;

    @Inject
    protected FeedbackManager feedbackManager;

    @Inject
    protected StatisticManager statisticManager;

    protected Button radioDisable;
    protected Button radioEnable;
    protected Button allowSubmission;
    protected Combo intervalCombo;

    protected boolean isFeedbackDisabled;
    protected FeedbackInterval currentInterval;
    protected boolean isSubmissionAllowed;

    public FeedbackPreferencePage() {
        Saros.reinject(this);
        setPreferenceStore(saros.getPreferenceStore());
        setDescription(Messages.getString("feedback.page.description")); //$NON-NLS-1$
    }

    public void init(IWorkbench workbench) {
        // nothing to initialize here
    }

    protected void initialize() {
        isFeedbackDisabled = feedbackManager.isFeedbackDisabled();
        int interval = feedbackManager.getSurveyInterval();
        currentInterval = FeedbackInterval.getFromInterval(interval);
        isSubmissionAllowed = statisticManager.isStatisticSubmissionAllowed();

        initComponents();
    }

    protected void initComponents() {
        radioDisable.setSelection(isFeedbackDisabled);
        radioEnable.setSelection(!isFeedbackDisabled);

        intervalCombo.select(currentInterval.getIndex());
        setIntervalComboVisible();

        allowSubmission.setSelection(isSubmissionAllowed);
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
        group.setText(Messages.getString("feedback.page.group.help")); //$NON-NLS-1$
        group.setLayout(new GridLayout(2, false));
        group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Label label = new Label(group, SWT.WRAP);
        label.setText(Messages.getString("feedback.page.participate.now")); //$NON-NLS-1$
        GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
        gd.widthHint = convertWidthInCharsToPixels(60);
        label.setLayoutData(gd);

        Button startSurveyButton = new Button(group, SWT.PUSH);
        startSurveyButton.setText(Messages
            .getString("feedback.page.survey.start")); //$NON-NLS-1$
        startSurveyButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER,
            true, false));

        startSurveyButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                feedbackManager.showSurvey();
            }
        });
    }

    /**
     * Creates a group that contains the interval settings.
     * 
     * @param parent
     *            the parent composite for the group
     */
    protected void createIntervalGroup(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        group.setText(Messages.getString("feedback.page.group.interval")); //$NON-NLS-1$
        group.setLayout(new GridLayout(1, false));
        group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        radioDisable = new Button(group, SWT.RADIO);
        radioDisable.setText(Messages.getString("feedback.page.radio.disable")); //$NON-NLS-1$

        radioEnable = new Button(group, SWT.RADIO);
        radioEnable.setText(Messages.getString("feedback.page.radio.enable")); //$NON-NLS-1$

        SelectionListener listener = new SelectionAdapter() {

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
     * @param group
     *            the parent composite for the combo box
     */
    protected void createIntervalCombo(Composite group) {
        intervalCombo = new Combo(group, SWT.READ_ONLY | SWT.DROP_DOWN);
        intervalCombo.setItems(FeedbackInterval.toStringArray());
        GridData gd = new GridData();
        gd.horizontalIndent = 30;
        gd.widthHint = 200;
        intervalCombo.setLayoutData(gd);
        intervalCombo.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                // check which interval was selected
                int selection = intervalCombo.getSelectionIndex();
                currentInterval = FeedbackInterval.getFromIndex(selection);
            }

        });
    }

    /**
     * Create a group that lets the user enable or disable the submission of
     * feedback. It therefore contains a simple check box.
     * 
     * @param parent
     */
    protected void createStatisticGroup(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        group.setText(Messages.getString("feedback.page.group.statistic")); //$NON-NLS-1$
        group.setLayout(new GridLayout(1, false));
        group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        allowSubmission = new Button(group, SWT.CHECK);
        allowSubmission.setText(Messages
            .getString("feedback.page.statistic.allow")); //$NON-NLS-1$

        allowSubmission.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                isSubmissionAllowed = allowSubmission.getSelection();
            }
        });
    }

    /**
     * Sets the combo box visible if the user enabled the feedback reminders or
     * invisible otherwise.
     */
    protected void setIntervalComboVisible() {
        if (intervalCombo == null)
            return;
        intervalCombo.setEnabled(!isFeedbackDisabled);
    }

    @Override
    protected void performDefaults() {
        // get default values from PreferenceStore
        isFeedbackDisabled = getPreferenceStore().getDefaultInt(
            PreferenceConstants.FEEDBACK_SURVEY_DISABLED) != FeedbackManager.FEEDBACK_ENABLED;
        currentInterval = FeedbackInterval.getFromInterval(getPreferenceStore()
            .getDefaultInt(PreferenceConstants.FEEDBACK_SURVEY_INTERVAL));
        isSubmissionAllowed = getPreferenceStore().getDefaultInt(
            PreferenceConstants.STATISTIC_ALLOW_SUBMISSION) == StatisticManager.STATISTIC_ALLOW;

        // initialize components with defaults
        initComponents();
        super.performDefaults();
    }

    @Override
    public boolean performOk() {
        feedbackManager.setFeedbackDisabled(isFeedbackDisabled);
        feedbackManager.setSurveyInterval(currentInterval.getInterval());

        statisticManager.setStatisticSubmissionAllowed(isSubmissionAllowed);
        return super.performOk();
    }

    /**
     * The enum class to handle feedback intervals. It is supposed to map the
     * interval's index in the combobox to the actual interval value and vice
     * versa.
     */
    protected enum FeedbackInterval {
        EVERY(1), EVERY_THIRD(3), EVERY_FIFTH(5), EVERY_TENTH(10), EVERY_FIFTEENTH(
            15);

        private static final Map<Integer, FeedbackInterval> lookup = new HashMap<Integer, FeedbackInterval>();

        static {
            for (FeedbackInterval f : EnumSet.allOf(FeedbackInterval.class)) {
                lookup.put(f.getInterval(), f);
            }
        }

        private int interval;

        private FeedbackInterval(int i) {
            this.interval = i;
        }

        public int getInterval() {
            return interval;
        }

        public int getIndex() {
            return this.ordinal();
        }

        @Override
        public String toString() {
            return this.name().toLowerCase().replace('_', ' ').concat(
                " session"); //$NON-NLS-1$
        }

        /**
         * Returns the enum for the given interval.
         * 
         * @param interval
         * @return the enum for the interval, can't be null
         */
        public static FeedbackInterval getFromInterval(int interval) {
            FeedbackInterval[] values = FeedbackInterval.values();
            // make sure the given interval isn't too small or too big
            if (interval < values[0].interval)
                interval = values[0].interval;
            if (interval > values[values.length - 1].interval)
                interval = values[values.length - 1].interval;

            FeedbackInterval i = lookup.get(interval);
            // if the interval didn't exist in the map, return the default
            if (i == null) {
                return EVERY_FIFTH;
            }
            return i;
        }

        /**
         * Returns the enum for the given index.
         * 
         * @param index
         * @return the enum for the index
         */
        public static FeedbackInterval getFromIndex(int index) {
            // make sure the index exists
            if (index < 0)
                index = 0;
            if (index > FeedbackInterval.values().length - 1)
                index = FeedbackInterval.values().length - 1;

            return FeedbackInterval.values()[index];
        }

        /**
         * Returns an array of Strings that contains the enum names in a user
         * friendly text representation.
         * 
         * @return String array of enum names
         */
        public static String[] toStringArray() {
            FeedbackInterval[] intervals = FeedbackInterval.values();
            String[] strings = new String[intervals.length];
            for (int i = 0; i < intervals.length; i++) {
                strings[i] = intervals[i].toString();
            }
            return strings;
        }
    }

}
