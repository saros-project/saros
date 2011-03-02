package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder;

import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.Component;

public class WaitImp extends Component implements Wait {
    private static transient WaitImp self;

    /**
     * {@link WaitImp} is a singleton, but inheritance is possible.
     */
    public static WaitImp getInstance() {
        if (self != null)
            return self;
        self = new WaitImp();
        return self;
    }

    /**********************************************
     * 
     * waits until
     * 
     **********************************************/
    public void waitUntilWindowSarosRunningVCSOperationClosed()
        throws RemoteException {
        bot().waitsUntilShellIsClosed(SHELL_SAROS_RUNNING_VCS_OPERATION);
    }

    public void waitUntilProjectInSVN(String projectName)
        throws RemoteException {
        bot().waitUntil(SarosConditions.isInSVN(projectName));
    }

    public void waitUntilProjectNotInSVN(String projectName)
        throws RemoteException {
        bot().waitUntil(SarosConditions.isNotInSVN(projectName));
    }

    public void waitUntilRevisionIsSame(String fullPath, String revision)
        throws RemoteException {
        bot().waitUntil(SarosConditions.isRevisionSame(fullPath, revision));
    }

    public void waitUntilUrlIsSame(String fullPath, String url)
        throws RemoteException {
        bot().waitUntil(SarosConditions.isUrlSame(fullPath, url));
    }

    public void waitUntilFolderExists(String... folderNodes)
        throws RemoteException {
        String fullPath = getPath(folderNodes);
        bot().waitUntil(SarosConditions.isResourceExist(fullPath));
    }

    public void waitUntilPkgExists(String projectName, String pkg)
        throws RemoteException {
        if (pkg.matches(PKG_REGEX)) {
            bot().waitUntil(
                SarosConditions.isResourceExist(getPkgPath(projectName, pkg)));
        } else {
            throw new RuntimeException(
                "The passed parameter \"pkg\" isn't valid, the package name should corresponds to the pattern [\\w\\.]*\\w+ e.g. PKG1.PKG2.PKG3");
        }
    }

    public void waitUntilPkgNotExists(String projectName, String pkg)
        throws RemoteException {
        if (pkg.matches(PKG_REGEX)) {
            bot().waitUntil(
                SarosConditions
                    .isResourceNotExist(getPkgPath(projectName, pkg)));
        } else {
            throw new RuntimeException(
                "The passed parameter \"pkg\" isn't valid, the package name should corresponds to the pattern [\\w\\.]*\\w+ e.g. PKG1.PKG2.PKG3");
        }
    }

    public void waitUntilFileExists(String... fileNodes) throws RemoteException {
        String fullPath = getPath(fileNodes);
        bot().waitUntil(SarosConditions.isResourceExist(fullPath));
    }

    public void waitUntilClassExists(String projectName, String pkg,
        String className) throws RemoteException {
        String path = getClassPath(projectName, pkg, className);
        bot().waitUntil(SarosConditions.isResourceExist(path));
    }

    public void waitUntilClassNotExists(String projectName, String pkg,
        String className) throws RemoteException {
        String path = getClassPath(projectName, pkg, className);
        bot().waitUntil(SarosConditions.isResourceNotExist(path));
    }

    public void waitUntilIsInSession() throws RemoteException {
        bot().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return sarosBot().state().isInSession();
            }

            public String getFailureMessage() {
                return "can't open the session.";
            }
        });
    }

    public void waitUntilIsInviteeInSession(SarosBot sarosBot)
        throws RemoteException {
        sarosBot.condition().waitUntilIsInSession();
    }

    public void waitUntilIsNotInSession() throws RemoteException {
        bot().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return !sarosBot().state().isInSession();
            }

            public String getFailureMessage() {
                return "can't close the session.";
            }
        });
    }

    public void waitUntilIsInviteeNotInSession(SarosBot sarosBot)
        throws RemoteException {
        sarosBot.condition().waitUntilIsNotInSession();
    }

    public void waitUntilHasWriteAccess() throws RemoteException {
        bot().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return sarosBot().state().hasWriteAccess();
            }

            public String getFailureMessage() {
                return "can't grant " + localJID.getBase()
                    + " the write access.";
            }
        });
    }

    public void waitUntilHasWriteAccessBy(final JID jid) throws RemoteException {
        bot().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return sarosBot().state().hasWriteAccessBy(jid);
            }

            public String getFailureMessage() {
                return "can't grant " + jid.getBase() + " the write accesss.";
            }
        });
    }

    public void waitUntilHasWriteAccessBy(final String tableItemText)
        throws RemoteException {
        bot().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return sarosBot().state().hasWriteAccessBy(tableItemText);
            }

            public String getFailureMessage() {
                return "can't grant " + tableItemText + " the write accesss.";
            }
        });
    }

    public void waitUntilHasReadOnlyAccess() throws RemoteException {
        bot().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return !sarosBot().state().hasWriteAccess();
            }

            public String getFailureMessage() {
                return "can't restrict " + localJID.getBase()
                    + " to read-only access";
            }
        });
    }

    public void waitUntilHasReadOnlyAccessBy(final JID jid)
        throws RemoteException {
        bot().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return !sarosBot().state().hasWriteAccessBy(jid);
            }

            public String getFailureMessage() {
                return "can't restrict " + jid.getBase()
                    + " to read-only access.";
            }
        });
    }

    public void waitUntilHasReadOnlyAccessBy(final String tableItemText)
        throws RemoteException {
        bot().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return sarosBot().state().hasReadOnlyAccessBy(tableItemText);
            }

            public String getFailureMessage() {
                return "can't restrict " + tableItemText
                    + " to read-only access.";
            }
        });
    }

    public void waitUntilIsFollowingBuddy(final JID followedBuddyJID)
        throws RemoteException {
        bot().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return sarosBot().state().isFollowingBuddy(followedBuddyJID);
            }

            public String getFailureMessage() {
                return localJID.getBase() + " is not folloing the user "
                    + followedBuddyJID.getName();
            }
        });
    }

    public void waitUntilIsNotFollowingBuddy(final JID foolowedBuddyJID)
        throws RemoteException {
        bot().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return !sarosBot().state().isFollowingBuddy(foolowedBuddyJID);
            }

            public String getFailureMessage() {
                return foolowedBuddyJID.getBase() + " is still followed.";
            }
        });
    }

    public void waitUntilAllPeersLeaveSession(
        final List<JID> jidsOfAllParticipants) throws RemoteException {
        bot().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                for (JID jid : jidsOfAllParticipants) {
                    if (sarosBot().state().isParticipantNoGUI(jid))
                        return false;
                }
                return true;
            }

            public String getFailureMessage() {
                return "There are someone, who still not leave the session.";
            }
        });
    }

    public void waitUntilIsInconsistencyDetected() throws RemoteException {

        bot().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return bot().view(VIEW_SAROS_SESSION)
                    .toolbarButtonWithRegex(TB_INCONSISTENCY_DETECTED + ".*")
                    .isEnabled();
            }

            public String getFailureMessage() {
                return "The toolbar button " + TB_INCONSISTENCY_DETECTED
                    + " isn't enabled.";
            }
        });
    }

}
