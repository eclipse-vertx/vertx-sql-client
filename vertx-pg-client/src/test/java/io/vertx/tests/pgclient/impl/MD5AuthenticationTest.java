package io.vertx.tests.pgclient.impl;

import io.vertx.pgclient.impl.util.MD5Authentication;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

public class MD5AuthenticationTest {

  @Test
  public void encodeTest() {
    Assert.assertEquals(
      "md54cd35160716308e3e571bbba12bb7591",
      MD5Authentication.encode("scott", "tiger", "salt'n'pepper".getBytes(StandardCharsets.UTF_8)));
  }
}
