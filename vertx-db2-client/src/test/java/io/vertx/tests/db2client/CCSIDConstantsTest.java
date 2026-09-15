package io.vertx.tests.db2client;

import io.vertx.db2client.impl.drda.CCSIDConstants;
import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class CCSIDConstantsTest {

  @Test
  public void testCcsidEncodingMapping() {
    // Chinese CCSID checks
    assertEquals(Charset.forName("GBK"), CCSIDConstants.getCharsetForCCSID(1386));
    assertEquals(Charset.forName("GBK"), CCSIDConstants.getCharsetForCCSID(5488));
    assertEquals(Charset.forName("GBK"), CCSIDConstants.getCharsetForCCSID(1114));
    assertEquals(Charset.forName("Cp935"), CCSIDConstants.getCharsetForCCSID(935));

    // Default / UTF-8
    assertEquals(StandardCharsets.UTF_8, CCSIDConstants.getCharsetForCCSID(1208));
  }
}
