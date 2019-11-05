package saros.misc.xstream;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamDriver;
import com.thoughtworks.xstream.security.NoTypePermission;
import com.thoughtworks.xstream.security.NullPermission;
import com.thoughtworks.xstream.security.PrimitiveTypePermission;
import java.util.Collection;
import java.util.Map;

/** Factory class providing XStream objects with the correctly set-up security framework. */
public class XStreamFactory {

  /**
   * Returns an <code>XStream</code> object with an set-up security framework.
   *
   * @return an <code>XStream</code> object with an set-up security framework
   */
  public static XStream getSecureXStream() {
    XStream xStream = new XStream();

    setUpSecurityFramework(xStream);

    return xStream;
  }

  /**
   * Returns an <code>XStream</code> object with an set-up security framework. The passed <code>
   * HierarchicalStreamDriver</code> will be used when instantiating the <code>XStream</code> object
   *
   * @return an <code>XStream</code> object with an set-up security framework
   */
  public static XStream getSecureXStream(HierarchicalStreamDriver hierarchicalStreamDriver) {
    XStream xStream = new XStream(hierarchicalStreamDriver);

    setUpSecurityFramework(xStream);

    return xStream;
  }

  /**
   * Sets up the security framework for the passed <code>XStream</code> object.
   *
   * @param xStream the <code>XStream</code> object to set the security framework up for
   * @see <a
   *     href="https://x-stream.github.io/security.html">https://x-stream.github.io/security.html</a>
   */
  private static void setUpSecurityFramework(XStream xStream) {
    // forbid all classes by default
    xStream.addPermission(NoTypePermission.NONE);

    // allow default java stuff
    xStream.addPermission(NullPermission.NULL);
    xStream.addPermission(PrimitiveTypePermission.PRIMITIVES);
    xStream.allowTypeHierarchy(Collection.class);
    xStream.allowTypeHierarchy(Map.class);
    xStream.allowTypes(new Class[] {String.class});

    // allow all saros classes
    xStream.allowTypesByWildcard(new String[] {"saros.**"});
  }
}
