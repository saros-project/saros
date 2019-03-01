/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package saros.communication.extensions;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import java.util.HashMap;
import java.util.Map;
import saros.negotiation.hooks.ISessionNegotiationHook;
import saros.net.xmpp.JID;

/** Packet used for exchanging data during session negotiation. Can be used on both sides. */
@XStreamAlias(/* SessionNegotiationParameterExchange */ "SNPE")
public class InvitationParameterExchangeExtension extends InvitationExtension {

  public static final Provider PROVIDER = new Provider();

  @XStreamAlias("host")
  private JID sessionHost;

  /**
   * Stores the hook-specific settings for transmission. For each hook, these are represented as a
   * <code>Map&lt;String, String&gt;</code>.
   */
  /*
   * Currently, the mapping from "hook" to "setting" is done via the hook's
   * identifier (a <code>String</code>). An alternative way would be to use
   * the hook's <code>Class</code>. But this would make Saros compatibility
   * more fragile, e.g. consider moving/renaming a hook class. An identifier
   * can mask such changes.
   */
  @XStreamAlias("settings")
  private Map<String, Map<String, String>> hookSettings;

  public InvitationParameterExchangeExtension(String negotiationID) {
    super(negotiationID);

    hookSettings = new HashMap<String, Map<String, String>>();
  }

  public JID getSessionHost() {
    return sessionHost;
  }

  public void setSessionHost(JID sessionHost) {
    this.sessionHost = sessionHost;
  }

  // Component ? this is a stupid class containing data for serialization ...

  /**
   * Saves the hook-specific settings into this extension.
   *
   * <p>One "secret" of this component is the way how a hook and its settings are stored (and
   * therefore the way they will be transmitted). Changing this is likely to break the compatibility
   * with older versions of Saros due to the change in the invitation process.
   *
   * @param hook The hook of which the settings should be transferred between client and host.
   * @param settings The settings for <code>hook</code>, represented in the form of [Key, Value]
   *     pairs. The extension won't be changed if the settings are <code>null</code>.
   */
  public void saveHookSettings(ISessionNegotiationHook hook, Map<String, String> settings) {
    if (settings == null) return;

    hookSettings.put(hook.getIdentifier(), settings);
  }

  /**
   * Retrieves the hook-specific settings from this extension.
   *
   * @param hook The hook of which the settings should be retrieved.
   * @return The settings for the <code>hook</code> represented as [Key, Value] pairs. Will be
   *     <code>null</code>, if the communication partner (i.e. either the invited client, or the
   *     inviting host) does not possess the <code>hook</code>.
   */
  public Map<String, String> getHookSettings(ISessionNegotiationHook hook) {
    return hookSettings.get(hook.getIdentifier());
  }

  public static class Provider
      extends InvitationExtension.Provider<InvitationParameterExchangeExtension> {

    private Provider() {
      super("snpe", InvitationParameterExchangeExtension.class);
    }
  }
}
