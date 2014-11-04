package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.saros.impl;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCTabItem;

import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.bot.SarosSWTBot;
import de.fu_berlin.inf.dpp.stf.server.bot.widget.SarosSWTBotActivityLogLine;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.saros.IActivityLog;

public final class ActivityLog extends StfRemoteObject implements IActivityLog {

    private static final Logger LOG = Logger.getLogger(ActivityLog.class);

    private static final ActivityLog INSTANCE = new ActivityLog();

    private SWTBotCTabItem activityLogTab;

    public static ActivityLog getInstance() {
        return INSTANCE;
    }

    public void setActivityLogTab(SWTBotCTabItem activityLogTab) {
        this.activityLogTab = activityLogTab;
    }

    @Override
    public String getTitle() throws RemoteException {
        String tabTitle = activityLogTab.getText();
        LOG.debug("tab title text: " + tabTitle);
        return tabTitle;
    }

    @Override
    public String[] getLines() throws RemoteException {
        activityLogTab.activate();
        SarosSWTBot bot = new SarosSWTBot(activityLogTab.widget);
        SarosSWTBotActivityLogLine[] logLines = bot.activityLogLines();
        int nrLines = logLines.length;
        String[] lines = new String[nrLines];
        for (int i = 0; i < nrLines; i++) {
            lines[i] = logLines[i].getText();
        }
        LOG.debug("activity log has " + logLines.length + " lines");
        return lines;
    }
}