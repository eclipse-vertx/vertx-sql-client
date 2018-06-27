package io.reactiverse.pgclient.impl.codec.util;

import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class MD5AuthenticationTest {

  @Test
  public void encodeTest() {
    assertEquals(
      "md54cd35160716308e3e571bbba12bb7591",
      MD5Authentication.encode("scott", "tiger", "salt'n'pepper".getBytes(StandardCharsets.UTF_8)));
  }
}
