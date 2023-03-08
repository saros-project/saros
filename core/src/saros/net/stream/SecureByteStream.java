package saros.net.stream;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Random;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class SecureByteStream implements ByteStream {

  private static final int READ_TIMEOUT = 60 * 1000;

  private static final int RSA_KEY_SIZE = 1024; // bits
  private static final int MAX_BUFFER_LENGTH = 1024 * 16; // bytes

  private static final int RC4_KEY_SIZE = 128; // bits

  final CipherInputStream in;
  final CipherOutputStream out;
  final ByteStream delegate;

  private SecureByteStream(
      final ByteStream delegate, final CipherOutputStream out, final CipherInputStream in) {
    this.delegate = delegate;
    this.out = out;
    this.in = in;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return in;
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    return out;
  }

  @Override
  public void close() throws IOException {
    delegate.close();
  }

  @Override
  public int getReadTimeout() throws IOException {
    return delegate.getReadTimeout();
  }

  @Override
  public void setReadTimeout(final int timeout) throws IOException {
    delegate.setReadTimeout(timeout);
  }

  public static ByteStream wrap(final ByteStream stream, final boolean isClient)
      throws IOException {
    if (stream instanceof SecureByteStream) return stream;

    return isClient ? doClientHandshake(stream) : doServerHandshake(stream);
  }

  private static SecureByteStream doServerHandshake(final ByteStream delegate) throws IOException {

    final int currentReadTimeout = delegate.getReadTimeout();

    final DataOutputStream out = new DataOutputStream(delegate.getOutputStream());

    final DataInputStream in = new DataInputStream(delegate.getInputStream());

    try {

      delegate.setReadTimeout(READ_TIMEOUT);

      final KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");

      keyGenerator.initialize(RSA_KEY_SIZE);

      final KeyPair keyPair = keyGenerator.generateKeyPair();

      byte[] publicKeyData = keyPair.getPublic().getEncoded();

      out.writeInt(publicKeyData.length);
      out.write(publicKeyData);
      out.flush();

      final int rsaEncodedRC4KeyDataLength = in.readInt();

      checkBufferSize(rsaEncodedRC4KeyDataLength, "rsaEncodedRC4KeyData");

      final byte[] rsaEncodedRC4KeyData = new byte[rsaEncodedRC4KeyDataLength];

      in.readFully(rsaEncodedRC4KeyData);

      final Cipher rsaCipher = Cipher.getInstance("RSA");

      rsaCipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());

      final byte[] rc4KeyData = rsaCipher.doFinal(rsaEncodedRC4KeyData);

      final SecretKey rc4SecretKey = new SecretKeySpec(rc4KeyData, "RC4");

      final Cipher rc4CipherOut = Cipher.getInstance("RC4");
      final Cipher rc4CipherIn = Cipher.getInstance("RC4");

      rc4CipherOut.init(Cipher.ENCRYPT_MODE, rc4SecretKey);
      rc4CipherIn.init(Cipher.DECRYPT_MODE, rc4SecretKey);

      final CipherOutputStream rc4CipherOutputStream =
          new CipherOutputStream(delegate.getOutputStream(), rc4CipherOut);

      final CipherInputStream rc4CipherInputStream =
          new CipherInputStream(delegate.getInputStream(), rc4CipherIn);

      // send garbage to avoid RC4 attacks/sniffing

      final Random random = new Random();

      final int garbageDataSize = random.nextInt(64) + 64;

      final byte[] garbageData = new byte[garbageDataSize];

      final DataOutputStream encryptedOutputStream = new DataOutputStream(rc4CipherOutputStream);
      random.nextBytes(garbageData);

      encryptedOutputStream.writeInt(garbageDataSize);
      encryptedOutputStream.write(garbageData);
      encryptedOutputStream.flush();

      return new SecureByteStream(delegate, rc4CipherOutputStream, rc4CipherInputStream);

    } catch (NoSuchAlgorithmException
        | InvalidKeyException
        | NoSuchPaddingException
        | IllegalBlockSizeException
        | BadPaddingException e) {
      throw new IOException(e);
    } finally {
      delegate.setReadTimeout(currentReadTimeout);
    }
  }

  private static SecureByteStream doClientHandshake(final ByteStream delegate) throws IOException {
    final int currentReadTimeout = delegate.getReadTimeout();

    final DataOutputStream out = new DataOutputStream(delegate.getOutputStream());

    final DataInputStream in = new DataInputStream(delegate.getInputStream());

    try {

      delegate.setReadTimeout(READ_TIMEOUT);

      final int rsaPublicKeyDataLength = in.readInt();

      checkBufferSize(rsaPublicKeyDataLength, "rsaPublicKeyData");

      final byte[] rsaPublicKeyData = new byte[rsaPublicKeyDataLength];

      in.readFully(rsaPublicKeyData);

      final PublicKey rsaPublicKey =
          KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(rsaPublicKeyData));

      final KeyGenerator keyGenerator = KeyGenerator.getInstance("RC4");
      keyGenerator.init(RC4_KEY_SIZE);

      final SecretKey rc4SecretKey = keyGenerator.generateKey();

      byte[] rc4SecretKeyData = rc4SecretKey.getEncoded();

      final Cipher rsaCipher = Cipher.getInstance("RSA");

      rsaCipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey);

      final byte[] encodedRC4SecretKeyData = rsaCipher.doFinal(rc4SecretKeyData);

      out.writeInt(encodedRC4SecretKeyData.length);
      out.write(encodedRC4SecretKeyData);
      out.flush();

      final Cipher rc4CipherOut = Cipher.getInstance("RC4");
      final Cipher rc4CipherIn = Cipher.getInstance("RC4");

      rc4CipherOut.init(Cipher.ENCRYPT_MODE, rc4SecretKey);
      rc4CipherIn.init(Cipher.DECRYPT_MODE, rc4SecretKey);

      final CipherOutputStream rc4CipherOutputStream =
          new CipherOutputStream(delegate.getOutputStream(), rc4CipherOut);

      final CipherInputStream rc4CipherInputStream =
          new CipherInputStream(delegate.getInputStream(), rc4CipherIn);

      final DataInputStream decryptedInputStream = new DataInputStream(rc4CipherInputStream);

      final int garbageDataSize = decryptedInputStream.readInt();

      checkBufferSize(garbageDataSize, "garbageData");

      final byte[] garbageData = new byte[garbageDataSize];

      decryptedInputStream.readFully(garbageData);

      return new SecureByteStream(delegate, rc4CipherOutputStream, rc4CipherInputStream);

    } catch (NoSuchAlgorithmException
        | NoSuchPaddingException
        | InvalidKeySpecException
        | InvalidKeyException
        | IllegalBlockSizeException
        | BadPaddingException e) {
      throw new IOException(e);
    } finally {
      delegate.setReadTimeout(currentReadTimeout);
    }
  }

  private static void checkBufferSize(final int size, final String bufferName) throws IOException {
    if (size < 0 || size > MAX_BUFFER_LENGTH)
      throw new IOException(
          "invalid " + bufferName + " buffer length: 0 >= " + size + " < " + MAX_BUFFER_LENGTH);
  }
}
