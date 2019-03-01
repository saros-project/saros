package saros.communication.extensions;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * @JTourBusStop 3, Creating custom network messages, Providing hints for XStream:
 *
 * <p>As those packet extensions must be converted to XML we use the XStream to accomplish this and
 * so avoiding the need of writing custom serialization and deserialization code. Please note that
 * XStream uses the terms marshall and unmarshall which are synonyms.
 *
 * <p>First you MUST assign a unique alias to the class that contains the data that should be
 * marshalled. This is necessary to ensure that the class can still be unmarshalled on the receiver
 * side even if the class had been renamed in future Saros versions but the compatibility between
 * those version has not been changed.
 *
 * <p>You just have to attach an @XStreamAlias annotation to the class with an alias name. Please
 * keep that name very short as it is included in the XML output.
 *
 * <p>Furthermore you should "format" the XML output by adding XStream specific annotations to the
 * class fields.
 */
@XStreamAlias(/* ProjectNegotiationCancel */ "PNCL")
public class CancelProjectNegotiationExtension extends SarosSessionPacketExtension {

  public static final Provider PROVIDER = new Provider();

  @XStreamAsAttribute private String negotiationID;

  @XStreamAlias("error")
  private String errorMessage;

  public CancelProjectNegotiationExtension(
      String sessionID, String negotiationID, String errorMessage) {
    super(sessionID);

    this.negotiationID = negotiationID;

    if ((errorMessage != null) && (errorMessage.length() > 0)) this.errorMessage = errorMessage;
  }

  /**
   * Returns the error message for this cancellation.
   *
   * @return the error message or <code>null</code> if the remote contact canceled the project
   *     negotiation manually
   */
  public String getErrorMessage() {
    return errorMessage;
  }

  /**
   * Returns the id for the specific project negotiation that was cancelled.
   *
   * @return id of a project negotiation
   */
  public String getNegotiationID() {
    return negotiationID;
  }

  /* *
   *
   * @JTourBusStop 4, Creating custom network messages, Creating the provider:
   *
   * Each packet extension needs a provider so that the marshalled content
   * (XML output) can correctly unmarshalled again.
   *
   * Please use the exact layout as presented in this class. Create a public
   * static final field named PROVIDER. Put this field to the top of the class
   * and put the provider class itself at the bottom of the class.
   *
   * As you see the first argument in the call to the super constructor is the
   * XML element name. IMPORTANT: our logic does not check for correct XML
   * syntax so YOU have to make sure that the name is a valid XML tag.
   *
   * The element name has nothing to do with the XStream alias and can have a
   * completely different name although it MUST be unique among all other
   * packet extensions !
   *
   * The second argument is a var-arg. You must ensure that all classes of the
   * fields that are going to be marshalled are passed into the constructor.
   * Failing to do so will result in XStream annotations that are not
   * processed and so the XML output of the marshalling will not be the same
   * as what you would expected !
   */

  public static class Provider
      extends SarosSessionPacketExtension.Provider<CancelProjectNegotiationExtension> {
    private Provider() {
      super("pncl", CancelProjectNegotiationExtension.class);
    }
  }
}
