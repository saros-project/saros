package saros.ui.views;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.inject.Named;
import org.eclipse.e4.ui.di.AboutToShow;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuSeparator;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import saros.SarosPluginContext;
import saros.communication.InfoManager;
import saros.net.xmpp.JID;
import saros.net.xmpp.contact.XMPPContact;
import saros.preferences.PreferenceConstants;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.User;
import saros.ui.Messages;
import saros.ui.handlers.popup.ChangeColorHandler;
import saros.ui.handlers.popup.ChangeWriteAccessHandler;
import saros.ui.handlers.popup.DeleteContactHandler;
import saros.ui.handlers.popup.FollowThisPersonHandler;
import saros.ui.handlers.popup.JumpToUserWithWriteAccessPositionHandler;
import saros.ui.handlers.popup.OpenChatHandler;
import saros.ui.handlers.popup.RemoveUserHandler;
import saros.ui.handlers.popup.RenameContactHandler;
import saros.ui.handlers.popup.RequestSessionInviteHandler;
import saros.ui.handlers.popup.SendFileHandler;
import saros.ui.handlers.popup.SkypeHandler;
import saros.ui.util.selection.SelectionUtils;

/**
 * This class is responsible for creating the context menu for the saros view.
 * The class is referenced by the fragment defining the popup menu. The
 * createPopupMenu() method will be called by the eclipse 4 framework each time
 * the context menu needs to be created.
 */
class PopupMenu {

    @Inject
    protected ISarosSessionManager sarosSessionManager;
    @Inject
    private InfoManager infoManager;

    /*
     * TODO What about having actions as (disposable and recreatable?)
     * singletons? This map (together with register and get methods) would not
     * be necessary, actions could be added to the menus at will (through a
     * method that would remember only the disposable ones, so they can be
     * disposed when necessary).
     */
    private Map<String, MMenuElement> registeredMenuElements = new HashMap<String, MMenuElement>();

    private static final String ICONS_DIR_PATH = "platform:/plugin/saros.eclipse/icons/";

    private EModelService modelService;

    public PopupMenu() {
        SarosPluginContext.initComponent(this);
    }

    @PostConstruct
    public void postConstruct(EModelService modelService) {
        this.modelService = modelService;

        /**
         * There are a few additional things in the Saros view.
         *
         * <p>
         * There is tool bar that holds the icons along the top (also see
         * fragment.e4xmi).
         *
         * <p>
         * Also, there are context menus which appear when you: - right-click on
         * a person in your current session - right-click on a contact in the
         * contact list.
         */
        createUIElements();
    }

    @AboutToShow
    public void createPopupMenu(List<MMenuElement> elements,
        EModelService modelService,
        @Named(IServiceConstants.ACTIVE_SELECTION) ISelection activeSelection) {
        addRosterMenuItems(elements, modelService, activeSelection);
        addSessionMenuItems(elements, modelService, activeSelection);
    }

    protected void addRosterMenuItems(List<MMenuElement> elements,
        EModelService modelService, ISelection activeSelection) {

        /*
         * Do not display the following actions if participants are selected.
         */
        List<User> participants = SelectionUtils
            .getAdaptableObjects(activeSelection, User.class);
        if (participants.size() > 0)
            return;

        /*
         * Do not display the following actions if no contacts are selected.
         */
        List<XMPPContact> contacts = SelectionUtils
            .getAdaptableObjects(activeSelection, XMPPContact.class);
        if (contacts.isEmpty())
            return;

        XMPPContact contact = contacts.get(0);

        // TODO OLD Behavior: here (and at other places) we check if contact is
        // online
        // (currently you can invite a contact without saros support and get a
        // error message),
        // but could check already for saros support via
        // contact.hasSarosSupport(). In this case
        // we should probably add a Information about missing saros support.
        if (sarosSessionManager.getSession() == null
            && contact.getStatus().isOnline()) {
            MMenu shareResourcesSubMenu = modelService
                .createModelElement(MMenu.class);
            shareResourcesSubMenu.setLabel("Share Resource(s)...");
            shareResourcesSubMenu.setIconURI(
                "platform:/plugin/saros.eclipse/icons/elcl16/session_tsk.png");

            List<MMenuElement> menuEntries = shareResourcesSubMenu
                .getChildren();

            StartSessionWithProjects sswp = new StartSessionWithProjects(
                modelService, activeSelection);
            sswp.createMenu(menuEntries);

            elements.add(shareResourcesSubMenu);
            elements.add(modelService.createModelElement(MMenuSeparator.class));
        }

        // TODO: Currently only Saros/S is known to have a working
        // JoinSessionRequestHandler,
        // remove this once the situation changes / change this to it's own
        // feature.
        if (infoManager
            .getRemoteInfo(contact, PreferenceConstants.SERVER_SUPPORT)
            .isPresent()) {
            elements.add(getMenuElement(RequestSessionInviteHandler.ID));
            elements.add(modelService.createModelElement(MMenuSeparator.class));
        }

        MMenuElement openChat = getMenuElement(OpenChatHandler.ID);
        Map<String, Object> data = openChat.getTransientData();
        data.put("chatRoomsComposite", SarosView.chatRooms);

        // Add menu elements to popup menu
        elements.add(getMenuElement(SkypeHandler.ID));
        elements.add(openChat);
        elements.add(getMenuElement(SendFileHandler.ID));
        elements.add(getMenuElement(RenameContactHandler.ID));
        elements.add(getMenuElement(DeleteContactHandler.ID));
    }

    protected void addSessionMenuItems(List<MMenuElement> elements,
        EModelService modelService, ISelection activeSelection) {

        /*
         * TODO The decision whether to show an entry at all is made here,
         * whereas the decision whether to enable an entry is encapsulated in
         * each action. That does not feel right.
         */
        /*
         * Do not display the following actions if no participants are selected.
         */
        List<User> participants = SelectionUtils
            .getAdaptableObjects(activeSelection, User.class);
        if (participants.size() == 0)
            return;

        /*
         * Do not display the following actions if non-participants are
         * selected.
         */
        List<JID> contacts = SelectionUtils.getAdaptableObjects(activeSelection,
            JID.class);

        if (contacts.size() > 0)
            return;

        boolean isHost = false;

        ISarosSession session = sarosSessionManager.getSession();

        if (session != null)
            isHost = session.isHost();

        if (participants.size() != 1)
            return;

        if (participants.get(0).isLocal()) {
            elements.add(getMenuElement(ChangeColorHandler.ID));

            if (isHost) {
                elements.add(
                    getMenuElement(ChangeWriteAccessHandler.WriteAccess.ID));

                elements
                    .add(getMenuElement(ChangeWriteAccessHandler.ReadOnly.ID));
            }
        } else {
            if (isHost) {
                elements.add(
                    getMenuElement(ChangeWriteAccessHandler.WriteAccess.ID));

                elements
                    .add(getMenuElement(ChangeWriteAccessHandler.ReadOnly.ID));

                elements.add(getMenuElement(RemoveUserHandler.ID));
                elements
                    .add(modelService.createModelElement(MMenuSeparator.class));
            }
            elements.add(getMenuElement(FollowThisPersonHandler.ID));
            elements.add(
                getMenuElement(JumpToUserWithWriteAccessPositionHandler.ID));

            elements.add(modelService.createModelElement(MMenuSeparator.class));

            elements.add(getMenuElement(OpenChatHandler.ID));
            elements.add(getMenuElement(SendFileHandler.ID));
        }
    }

    private void registerMenuElement(MMenuElement menuItem) {
        String id = menuItem.getElementId();
        if (registeredMenuElements.containsKey(id)) {
            throw new IllegalArgumentException(
                "tried to register menu element with id " + id
                    + " more than once");
        }
        registeredMenuElements.put(id, menuItem);
    }

    private MMenuElement getMenuElement(String menuElementId) {
        MMenuElement menuElement = registeredMenuElements.get(menuElementId);

        if (menuElement == null) {
            throw new IllegalArgumentException("a menu element for id "
                + menuElementId + " is not registered");
        }
        menuElement.setVisible(true);
        return menuElement;
    }

    private MMenuElement createMenuItem(String id, String label, String tooltip,
        String iconPath, String handlerClass) {
        MDirectMenuItem item = modelService
            .createModelElement(MDirectMenuItem.class);
        item.setElementId(id);
        item.setLabel(label);
        item.setContributionURI(handlerClass);

        if (tooltip != null)
            item.setTooltip(tooltip);
        if (iconPath != null)
            item.setIconURI(iconPath);

        return item;
    }

    /**
     * Creates all needed menu items once and registers them, so they can be
     * requested when constructing the context menu.
     *
     * <p>
     * In the eclipse 4 framework you can only reference icons with their path
     * an not by an {@link ImageDescriptor}. So we created all referenced
     * overlaid images.
     */
    private void createUIElements() {
        // ContextMenus Session
        registerMenuElement(createMenuItem(
            ChangeWriteAccessHandler.WriteAccess.ID,
            Messages.GiveWriteAccessAction_title,
            Messages.GiveWriteAccessAction_tooltip,
            ICONS_DIR_PATH + "obj16/contact_saros_obj.png",
            "bundleclass://saros.eclipse/saros.ui.handlers.popup.ChangeWriteAccessHandler$WriteAccess"));
        registerMenuElement(createMenuItem(ChangeWriteAccessHandler.ReadOnly.ID,
            Messages.RestrictToReadOnlyAccessAction_title,
            Messages.RestrictToReadOnlyAccessAction_tooltip,
            ICONS_DIR_PATH + "merged16/contact_saros_obj_readonly.png",
            "bundleclass://saros.eclipse/saros.ui.handlers.popup.ChangeWriteAccessHandler$ReadOnly"));
        registerMenuElement(createMenuItem(FollowThisPersonHandler.ID,
            Messages.FollowThisPersonAction_follow_title,
            Messages.FollowThisPersonAction_follow_tooltip,
            ICONS_DIR_PATH + "merged16/contact_saros_obj_followmode.png",
            "bundleclass://saros.eclipse/saros.ui.handlers.popup.FollowThisPersonHandler"));
        registerMenuElement(createMenuItem(
            JumpToUserWithWriteAccessPositionHandler.ID,
            Messages.JumpToUserWithWriteAccessPositionAction_title,
            Messages.JumpToUserWithWriteAccessPositionAction_tooltip,
            ICONS_DIR_PATH + "elcl16/jump.png",
            "bundleclass://saros.eclipse/saros.ui.handlers.popup.JumpToUserWithWriteAccessPositionHandler"));
        // Using file_obj icon from org.eclipse.ui
        registerMenuElement(createMenuItem(SendFileHandler.ID,
            Messages.SendFileAction_title, Messages.SendFileAction_tooltip,
            "platform:/plugin/org.eclipse.ui/icons/full/obj16/file_obj.png",
            "bundleclass://saros.eclipse/saros.ui.handlers.popup.SendFileHandler"));
        registerMenuElement(createMenuItem(ChangeColorHandler.ID,
            Messages.ChangeColorAction_title,
            Messages.ChangeColorAction_tooltip,
            ICONS_DIR_PATH + "elcl16/changecolor.png",
            "bundleclass://saros.eclipse/saros.ui.handlers.popup.ChangeColorHandler"));
        registerMenuElement(createMenuItem(RemoveUserHandler.ID,
            "Remove from Session", null,
            ICONS_DIR_PATH + "elcl16/contact_remove_tsk.png",
            "bundleclass://saros.eclipse/saros.ui.handlers.popup.RemoveUserHandler"));
        registerMenuElement(createMenuItem(RequestSessionInviteHandler.ID,
            Messages.RequestSessionInviteAction_title, null, null,
            "bundleclass://saros.eclipse/saros.ui.handlers.popup.RequestSessionInviteHandler"));

        // ContextMenus Roster/Contact list
        registerMenuElement(createMenuItem(SkypeHandler.ID,
            Messages.SkypeAction_title, Messages.SkypeAction_tooltip,
            ICONS_DIR_PATH + "elcl16/contact_skype_call_tsk.png",
            "bundleclass://saros.eclipse/saros.ui.handlers.popup.SkypeHandler"));
        registerMenuElement(createMenuItem(RenameContactHandler.ID,
            Messages.RenameContactAction_title,
            Messages.RenameContactAction_tooltip,
            ICONS_DIR_PATH + "etool16/edit.gif",
            "bundleclass://saros.eclipse/saros.ui.handlers.popup.RenameContactHandler"));
        // Using delete icon from org.eclipse.ui
        registerMenuElement(createMenuItem(DeleteContactHandler.ID,
            Messages.DeleteContactAction_title,
            Messages.DeleteContactAction_tooltip,
            "platform:/plugin/org.eclipse.ui/icons/full/etool16/delete.png",
            "bundleclass://saros.eclipse/saros.ui.handlers.popup.DeleteContactHandler"));

        // ContextMenus Both
        registerMenuElement(createMenuItem(OpenChatHandler.ID,
            Messages.OpenChatAction_MenuItem, null,
            ICONS_DIR_PATH + "view16/chat_misc.png",
            "bundleclass://saros.eclipse/saros.ui.handlers.popup.OpenChatHandler"));
    }
}
