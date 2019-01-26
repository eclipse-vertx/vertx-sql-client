package io.reactiverse.mysqlclient.impl.util;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Native41Authenticator {
  /**
   * Native authentication method 'mysql_native_password'
   * Calculate method: SHA1( password ) XOR SHA1( "20-bytes random data from server" <concat> SHA1( SHA1( password ) ) )
   *
   * @param password password value
   * @param charset  charset of password
   * @param salt     20 byte random challenge from server
   * @return scrambled password
   */
  public static byte[] encode(String password, Charset charset, byte[] salt) {
    MessageDigest messageDigest;
    try {
      messageDigest = MessageDigest.getInstance("SHA-1");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }

    byte[] passwordBytes = password.getBytes(charset);
    // SHA1(password)
    byte[] passwordHash1 = messageDigest.digest(passwordBytes);
    messageDigest.reset();
    // SHA1(SHA1(password))
    byte[] passwordHash2 = messageDigest.digest(passwordHash1);
    messageDigest.reset();

    // SHA1("20-bytes random data from server" <concat> SHA1(SHA1(password))
    messageDigest.update(salt);
    messageDigest.update(passwordHash2);
    byte[] passwordHash3 = messageDigest.digest();

    // result = passwordHash1 XOR passwordHash3
    for (int i = 0; i < passwordHash1.length; i++) {
      passwordHash1[i] = (byte) (passwordHash1[i] ^ passwordHash3[i]);
    }
    return passwordHash1;
  }
}
