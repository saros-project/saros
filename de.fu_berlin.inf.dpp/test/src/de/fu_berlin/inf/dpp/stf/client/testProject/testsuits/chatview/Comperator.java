package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.chatview;

import java.util.List;

public class Comperator {

    public static boolean compareStrings(String jid, String message,
        List<String> textfiled) {

        for (String line : textfiled) {
            System.out.println("Line " + line);
            if (line.startsWith(jid) && line.endsWith(message))
                return true;
        }

        return false;
    }

}
