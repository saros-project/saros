package de.fu_berlin.inf.dpp.stf.swtbot.helpers;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;

import de.fu_berlin.inf.dpp.stf.sarosswtbot.SarosConstant;
import de.fu_berlin.inf.dpp.stf.swtbot.RmiSWTWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.swtbot.SarosSWTWorkbenchBot;

public class MainObject {
    private static final transient Logger log = Logger
        .getLogger(MainObject.class);
    private RmiSWTWorkbenchBot rmiBot;
    private WaitUntilObject wUntil;
    private ViewObject viewObject;
    private static SarosSWTWorkbenchBot bot = new SarosSWTWorkbenchBot();

    public MainObject(RmiSWTWorkbenchBot rmiBot) {
        this.rmiBot = rmiBot;
        this.wUntil = rmiBot.wUntilObject;
        this.viewObject = rmiBot.viewObject;
    }

    public String[] changeToRegex(String... texts) {
        String[] matchTexts = new String[texts.length];
        for (int i = 0; i < texts.length; i++) {
            matchTexts[i] = texts[i] + ".*";
        }
        return matchTexts;
    }

    protected List<String> getAllProjects() {
        SWTBotTree tree = bot
            .viewByTitle(SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER).bot()
            .tree();
        List<String> projectNames = new ArrayList<String>();
        for (int i = 0; i < tree.getAllItems().length; i++) {
            projectNames.add(tree.getAllItems()[i].getText());
        }
        return projectNames;
    }

}
