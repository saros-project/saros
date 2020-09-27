package saros.lsp.extensions.server.contact;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.function.BiPredicate;
import saros.lsp.extensions.client.ISarosLanguageClient;
import saros.lsp.extensions.server.SarosResponse;
import saros.lsp.extensions.server.SarosResultResponse;
import saros.lsp.extensions.server.contact.dto.AddInput;
import saros.lsp.extensions.server.contact.dto.ContactDto;
import saros.lsp.extensions.server.contact.dto.RemoveInput;
import saros.lsp.extensions.server.contact.dto.RenameInput;
import saros.lsp.ui.UIInteractionManager;
import saros.net.xmpp.JID;
import saros.net.xmpp.contact.IContactsUpdate;
import saros.net.xmpp.contact.XMPPContact;
import saros.net.xmpp.contact.XMPPContactsService;

public class ContactService implements IContactService, IContactsUpdate {

  private XMPPContactsService contactsService;
  private ISarosLanguageClient client;
  private UIInteractionManager interactionManager;

  public ContactService(
      XMPPContactsService contactsService,
      ISarosLanguageClient client,
      UIInteractionManager interactionManager) {
    this.contactsService = contactsService;
    this.client = client;
    this.interactionManager = interactionManager;

    this.contactsService.addListener(this);
  }

  @Override
  public CompletableFuture<SarosResponse> add(AddInput input) {

    CompletableFuture<SarosResponse> c = new CompletableFuture<SarosResponse>();

    Executors.newCachedThreadPool()
        .submit(
            () -> {
              try {
                this.contactsService.addContact(
                    new JID(input.id), input.nickname, this.fromUserInput());

                c.complete(new SarosResponse());
              } catch (Exception e) {
                c.complete(new SarosResponse(e));
              }

              return null;
            });

    return c;
  }

  private BiPredicate<String, String> fromUserInput() {
    return (title, message) -> this.interactionManager.getUserInputYesNo(title, message);
  }

  @Override
  public CompletableFuture<SarosResponse> remove(RemoveInput input) {

    try {
      final Optional<XMPPContact> contact = this.contactsService.getContact(input.id);

      if (contact.isPresent()) {
        this.contactsService.removeContact(contact.get());
      }
    } catch (Exception e) {
      return CompletableFuture.completedFuture(new SarosResponse(e));
    }

    return CompletableFuture.completedFuture(new SarosResponse());
  }

  @Override
  public CompletableFuture<SarosResponse> rename(RenameInput input) {

    try {
      final Optional<XMPPContact> optionalContact = this.contactsService.getContact(input.id);
      if (optionalContact.isPresent()) {
        final XMPPContact contact = optionalContact.get();
        this.contactsService.renameContact(contact, input.nickname);

        final ContactDto contactDto = this.createDto(contact);
        contactDto.nickname = input.nickname;
        this.client.sendStateContact(contactDto);
      }
    } catch (Exception e) {
      return CompletableFuture.completedFuture(new SarosResponse(e));
    }

    return CompletableFuture.completedFuture(new SarosResponse());
  }

  @Override
  public CompletableFuture<SarosResultResponse<ContactDto[]>> getAll() {

    final List<XMPPContact> contacts = this.contactsService.getAllContacts();

    final ContactDto[] dtos =
        contacts
            .stream()
            .map(contact -> this.createDto(contact))
            .toArray(size -> new ContactDto[size]);

    return CompletableFuture.completedFuture(new SarosResultResponse<ContactDto[]>(dtos));
  }

  /**
   * Creates a dto of a {@link XMPPContact}.
   *
   * @param contact The contact
   * @return The contact as {@link ContactDto}
   */
  private ContactDto createDto(XMPPContact contact) {
    ContactDto dto = new ContactDto();
    dto.id = contact.getBareJid().toString();
    dto.nickname = contact.getDisplayableName();
    dto.isOnline = contact.getStatus().isOnline();
    dto.hasSarosSupport = contact.hasSarosSupport();

    return dto;
  }

  @Override
  public void update(Optional<XMPPContact> contact, UpdateType updateType) {
    if (contact.isPresent()) {
      XMPPContact con = contact.get();
      ContactDto dto = this.createDto(con);
      dto.subscribed = updateType != UpdateType.REMOVED;

      this.client.sendStateContact(dto);
    }
  }
}
