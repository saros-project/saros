package de.fu_berlin.inf.dpp.ui.widgets.wizard;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.ui.util.DialogUtils;
import de.fu_berlin.inf.dpp.ui.util.LayoutUtils;
import de.fu_berlin.inf.dpp.ui.widgets.explanation.note.NoteComposite;
import de.fu_berlin.inf.dpp.util.Utils;

public class PublicXMPPServerComposite extends NoteComposite {
    private static final Logger log = Logger
        .getLogger(PublicXMPPServerComposite.class);

    public static final String LIST_OF_XMPP_SERVERS = "http://www.saros-project.org/InstallUsing#Using_Public_XMPP_Servers";

    @Inject
    PreferenceUtils preferenceUtils;

    public PublicXMPPServerComposite(Composite parent, int style) {
        super(parent, SWT.BORDER);
    }

    @Override
    public Layout getContentLayout() {
        return LayoutUtils.createGridLayout(0, 5);
    }

    @Override
    public void createContent(Composite parent) {
        SarosPluginContext.initComponent(this);

        createQuickStart(parent);
        createProfessionalUsage(parent);

        createMoreInformation(parent);
    }

    protected void createQuickStart(Composite composite) {
        StyledText quickStartText = new StyledText(composite, SWT.WRAP);
        quickStartText.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING,
            true, false));
        quickStartText.setEnabled(false);
        quickStartText.setForeground(this.getForeground());
        quickStartText.setBackground(this.getBackground());
        quickStartText
            .setText("For a quick start we recommend to use Saros's Jabber server: "
                + preferenceUtils.getSarosXMPPServer());
        StyleRange styleRange = new StyleRange();
        styleRange.start = quickStartText.getText().length()
            - preferenceUtils.getSarosXMPPServer().length();
        styleRange.length = preferenceUtils.getSarosXMPPServer().length();
        styleRange.fontStyle = SWT.BOLD;
        quickStartText.setStyleRange(styleRange);

        // Label quickStartLabel = new Label(composite, SWT.WRAP);
        // this.configureLabel(quickStartLabel);
        // quickStartLabel
        // .setText("For a quick start we recommend to use Saros's Jabber server: "
        // + "saros-con.imp.fu-berlin.de");
    }

    protected void createProfessionalUsage(Composite composite) {
        Label otherServersLabel = new Label(composite, SWT.WRAP);
        this.configureLabel(otherServersLabel);
        otherServersLabel
            .setText("You are free to use any other XMPP/Jabber server as well.");
    }

    protected void configureLabel(Label label) {
        label.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
        label.setForeground(this.getForeground());
        label.setBackground(this.getBackground());
    }

    protected void createMoreInformation(Composite composite) {
        Button moreInformationButton = new Button(composite, SWT.PUSH);
        moreInformationButton.setLayoutData(new GridData(SWT.FILL,
            SWT.BEGINNING, true, false));
        moreInformationButton.setText("More Information...");
        moreInformationButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (!Utils.openExternalBrowser(LIST_OF_XMPP_SERVERS)) {
                    log.error("Couldn't open link " + LIST_OF_XMPP_SERVERS
                        + " in external browser.");
                    DialogUtils.openWarningMessageDialog(getShell(), "Warning",
                        "The list of Public XMPP/Jabbers server could not be opened.\n\n"
                            + "Please open " + LIST_OF_XMPP_SERVERS
                            + " manually in your browser.");
                }
            }
        });
    }
}
