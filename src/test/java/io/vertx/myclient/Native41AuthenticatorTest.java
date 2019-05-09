package io.vertx.myclient;

import io.vertx.myclient.impl.util.Native41Authenticator;
import io.vertx.myclient.impl.util.Utils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class Native41AuthenticatorTest {
  private String scrambledPasswordHexStr;
  private byte[] scramble;

  @Test
  public void testEncode() {
    scrambledPasswordHexStr = "f2671df1862aed0340be809405b30bb93d29142d";

    scramble = new byte[]{
      // part1
      0x18, 0x38, 0x55, 0x7e, 0x3a, 0x77, 0x65, 0x35,
      // part2
      0x34, 0x1f, 0x44, 0x4a, 0x36, 0x60, 0x5d, 0x79, 0x5c, 0x09, 0x6c, 0x08};

    assertEquals(scrambledPasswordHexStr, Utils.bytesToHexString(Native41Authenticator.encode("password", StandardCharsets.UTF_8, scramble)));
  }

  @Test
  public void testEncode2() {
    scrambledPasswordHexStr = "cc93fb6f68af2e9446dafc0a1667d015b0a49550";

    scramble = new byte[]{
      // part1
      0x42, 0x0f, 0x34, 0x68, 0x6f, 0x77, 0x67, 0x18,
      // part2
      0x14, 0x57, 0x3d, 0x04, 0x39, 0x70, 0x1f, 0x46, 0x58, 0x51, 0x49, 0x31};

    assertEquals(scrambledPasswordHexStr, Utils.bytesToHexString(Native41Authenticator.encode("password", StandardCharsets.UTF_8, scramble)));
  }
}
