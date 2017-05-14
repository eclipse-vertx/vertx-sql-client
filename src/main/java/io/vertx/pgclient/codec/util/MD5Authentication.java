package io.vertx.pgclient.codec.util;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static java.nio.charset.StandardCharsets.*;

public class MD5Authentication {

  public static String encode(String username, String password, byte[] salt) {

    HexBinaryAdapter hex = new HexBinaryAdapter();

    byte[] digest, passDigest;

    MessageDigest messageDigest;

    try {
      messageDigest = MessageDigest.getInstance("MD5");
    }
    catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }

    messageDigest.update(password.getBytes(UTF_8));
    messageDigest.update(username.getBytes(UTF_8));
    digest = messageDigest.digest();

    byte[] hexDigest = hex.marshal(digest).toLowerCase().getBytes(US_ASCII);

    messageDigest.update(hexDigest);
    messageDigest.update(salt);
    passDigest = messageDigest.digest();

    return "md5" + hex.marshal(passDigest).toLowerCase();
  }
}
