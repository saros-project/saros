package saros.net.stream;

import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import org.apache.log4j.Logger;

// taken from https://gist.github.com/mikeapr4/3b2b5d05bc57640e77d0#file-certificateutils-java

public class CertificateUtils {

  private static final Logger log = Logger.getLogger(CertificateUtils.class);

  private static final String KEY_TYPE_RSA = "RSA";
  private static final String SIG_ALG_SHA_RSA = "SHA256WithRSA";
  private static final int KEY_SIZE = 1024;
  private static final long CERT_VALIDITY = 365 * 24 * 3600L;
  private static final String ALIAS_PRIVATE = "private";
  private static final String ALIAS_CERT = "cert";

  /** The password for each created store. */
  public static final String KEY_STORE_PASSWORD =
      "notReallyImportant"; // this would only ever be relevant if/when persisted.

  /**
   * @param certValues e.g. CN=Dave, OU=JavaSoft, O=Sun Microsystems, C=US
   * @return
   */
  public static KeyStore createSelfSigned(final String certValues) {
    /*
     * When accessing the classes we get an error, requiring some compile settings to change.
     * However the build will still fail, so use reflection instead.
     */
    try {
      // CertAndKeyGen keyGen = new CertAndKeyGen(KEY_TYPE_RSA, SIG_ALG_SHA_RSA);

      final Class<?> keyGenClass = Class.forName("sun.security.tools.keytool.CertAndKeyGen");

      final Object keyGen =
          keyGenClass
              .getConstructor(String.class, String.class)
              .newInstance(KEY_TYPE_RSA, SIG_ALG_SHA_RSA);

      // keyGen.generate(KEY_SIZE);

      keyGenClass.getMethod("generate", int.class).invoke(keyGen, KEY_SIZE);

      final KeyStore ks = emptyStore();

      if (ks == null) return null;

      // X509Certificate certificate = keyGen.getSelfCertificate(new X500Name(certValues),
      // CERT_VALIDITY);

      Class<?> x500NameClass = Class.forName("sun.security.x509.X500Name");

      Object x500Name = x500NameClass.getConstructor(String.class).newInstance(certValues);

      final X509Certificate certificate =
          (X509Certificate)
              keyGenClass
                  .getMethod("getSelfCertificate", x500NameClass, long.class)
                  .invoke(keyGen, x500Name, CERT_VALIDITY);

      final Key privateKey = (Key) keyGenClass.getMethod("getPrivateKey").invoke(keyGen);
      ks.setCertificateEntry(ALIAS_CERT, certificate);

      ks.setKeyEntry(
          ALIAS_PRIVATE,
          privateKey,
          KEY_STORE_PASSWORD.toCharArray(),
          new Certificate[] {certificate});

      return ks;

    } catch (Exception e) {
      log.error("cannot create self signed certificate", e);
    }
    return null;
  }

  public static KeyStore createSelfSignedForHost(final String host) {
    return createSelfSigned("CN=" + host);
  }

  private static KeyStore emptyStore() {
    try {
      final KeyStore ks = KeyStore.getInstance("JKS");

      // Loading creates the store, can't do anything with it until it's loaded
      ks.load(null, KEY_STORE_PASSWORD.toCharArray());
      return ks;
    } catch (Exception e) {
      log.error("cannot create empty keystore", e);
    }

    return null;
  }
}
