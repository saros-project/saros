package saros.lsp.extensions.server.contact.dto;

/** The dto represents a contact from the contact list. */
public class ContactDto {
  public String id;

  public String nickname;

  public boolean isOnline;

  public boolean hasSarosSupport;

  public boolean subscribed;

  public ContactDto() {
    this.subscribed = true;
  }
}
