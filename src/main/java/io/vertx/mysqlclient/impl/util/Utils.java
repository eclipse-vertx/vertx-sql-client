package io.vertx.mysqlclient.impl.util;

public class Utils {
  //TODO From Pgclient(can be reused)
  private final static char[] HEX_ALPHABET = "0123456789abcdef".toCharArray();

  public static String bytesToHexString(byte[] bytes) {
    char[] hexChars = new char[bytes.length * 2];
    for (int j = 0; j < bytes.length; j++) {
      int v = bytes[j] & 0xFF;
      hexChars[j * 2] = HEX_ALPHABET[v >>> 4];
      hexChars[j * 2 + 1] = HEX_ALPHABET[v & 0x0F];
    }
    return new String(hexChars);
  }
}
