package saros.server.console;

import static java.util.stream.Collectors.partitioningBy;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import org.apache.log4j.Logger;
import saros.exceptions.OperationCanceledException;
import saros.net.util.XMPPUtils;
import saros.net.xmpp.JID;
import saros.net.xmpp.contact.XMPPContact;
import saros.net.xmpp.contact.XMPPContactsService;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.ISarosSessionManager;

public class InviteCommand extends ConsoleCommand {
  private static final Logger log = Logger.getLogger(InviteCommand.class);
  private final ISarosSessionManager sessionManager;

  public InviteCommand(ISarosSessionManager sessionManager, ServerConsole console) {
    this.sessionManager = sessionManager;
    console.registerCommand(this);
  }

  @Inject private XMPPContactsService contactsService;

  @Override
  public String identifier() {
    return "invite";
  }

  @Override
  public int minArgument() {
    return 1;
  }

  @Override
  public String help() {
    return "invite <JID>... - Invite users to session";
  }

  @Override
  public void execute(List<String> args, PrintStream out) {
    try {
      Map<Boolean, List<JID>> jids =
          args.stream().map(JID::new).collect(partitioningBy(XMPPUtils::validateJID));

      for (JID jid : jids.get(false)) {
        log.warn("Invalid JID skipped: " + jid);
      }

      for (JID jid : jids.get(true)) {

        // --------------add contact if not already--------------
        boolean contactFound = false;
        for (XMPPContact contact :
            contactsService.getAllContacts()) { // loop and check if contact was already added
          if (contact.getBareJid().equals(jid)) {
            contactFound = true;
            break;
          }
        }

        if (!contactFound) { // add contact
          log.warn(
              "Adding "
                  + jid
                  + " as new contact. You will have to reinvite him after he accepted being added to the contacts");
          out.println(
              "Adding "
                  + jid
                  + " as new contact. You will have to reinvite him after he accepted being added to the contacts"); // the previous warning wont be displayed in the console

          final BiPredicate<String, String> questionDialogHandler = // error handler
              (title, message) -> {
                log.error(
                    title
                        + message.replace(
                            "Do you want to add the contact anyway?",
                            "It will be continued anyway"));
                // It will always throw an Not subscribed error for any reason so it cant
                // test if the contact exists.
                // Reviewers please tell if its an bug or me being unexperienced
                return true;
              };

          try {
            contactsService.addContact(jid, null, questionDialogHandler);
            log.info(
                "The contact was added successfully. Please invite him again after he accepted");
            out.println(
                "The contact was added successfully. Please invite him again after he accepted");
            return; // exit because the user cant accept the invite in time
          } catch (OperationCanceledException e) {
            log.error(e);
          }
        }
        // --------------end add contact if not already--------------

        sessionManager.invite(
            jid,
            "Invitation by server command"); // invite will fail because user has not accepted yet
        sessionManager.startSharingReferencePoints(jid); // share
      }
    } catch (Exception e) {
      log.error("Error inviting users", e);
    }
  }
}
