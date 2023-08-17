package io.vertx.mysqlclient;

import io.vertx.mysqlclient.impl.MySQLDatabaseMetadata;
import org.junit.Assert;
import org.junit.Test;

public class MySQLServerVersionParserTest {

  private MySQLDatabaseMetadata actual;

  @Test
  public void testMySQL_V8_0() {
    actual = MySQLDatabaseMetadata.parse("8.0.19");
    Assert.assertEquals("8.0.19", actual.fullVersion());
    Assert.assertEquals("MySQL", actual.productName());
    Assert.assertEquals(8, actual.majorVersion());
    Assert.assertEquals(0, actual.minorVersion());
    Assert.assertEquals(19, actual.microVersion());
  }

  @Test
  public void testMySQL_V5_7() {
    actual = MySQLDatabaseMetadata.parse("5.7.29");
    Assert.assertEquals("5.7.29", actual.fullVersion());
    Assert.assertEquals("MySQL", actual.productName());
    Assert.assertEquals(5, actual.majorVersion());
    Assert.assertEquals(7, actual.minorVersion());
    Assert.assertEquals(29, actual.microVersion());
  }

  @Test
  public void testMySQL_LOG() {
    actual = MySQLDatabaseMetadata.parse("5.7.29-log");
    Assert.assertEquals("5.7.29-log", actual.fullVersion());
    Assert.assertEquals("MySQL", actual.productName());
    Assert.assertEquals(5, actual.majorVersion());
    Assert.assertEquals(7, actual.minorVersion());
    Assert.assertEquals(29, actual.microVersion());
  }

  @Test
  public void testMariaDB_V10_5() {
    actual = MySQLDatabaseMetadata.parse("5.5.5-10.5.3-MariaDB-1:10.5.3+maria~bionic");
    Assert.assertEquals("5.5.5-10.5.3-MariaDB-1:10.5.3+maria~bionic", actual.fullVersion());
    Assert.assertEquals("MariaDB", actual.productName());
    Assert.assertEquals(10, actual.majorVersion());
    Assert.assertEquals(5, actual.minorVersion());
    Assert.assertEquals(3, actual.microVersion());
  }

  @Test
  public void testMariaDB_V10_1() {
    actual = MySQLDatabaseMetadata.parse("5.5.5-10.1.45-MariaDB-1~bionic");
    Assert.assertEquals("5.5.5-10.1.45-MariaDB-1~bionic", actual.fullVersion());
    Assert.assertEquals("MariaDB", actual.productName());
    Assert.assertEquals(10, actual.majorVersion());
    Assert.assertEquals(1, actual.minorVersion());
    Assert.assertEquals(45, actual.microVersion());
  }

  @Test
  public void testMariaDB_V11_0_2() {
    actual = MySQLDatabaseMetadata.parse("11.0.2-MariaDB-1:11.0.2+maria~ubu2204");
    Assert.assertEquals("11.0.2-MariaDB-1:11.0.2+maria~ubu2204", actual.fullVersion());
    Assert.assertEquals("MariaDB", actual.productName());
    Assert.assertEquals(11, actual.majorVersion());
    Assert.assertEquals(0, actual.minorVersion());
    Assert.assertEquals(2, actual.microVersion());
  }

  @Test
  public void testPercona_V8_0() {
    actual = MySQLDatabaseMetadata.parse("8.0.19-10");
    Assert.assertEquals("8.0.19-10", actual.fullVersion());
    Assert.assertEquals("MySQL", actual.productName());
    Assert.assertEquals(8, actual.majorVersion());
    Assert.assertEquals(0, actual.minorVersion());
    Assert.assertEquals(19, actual.microVersion());
  }

  @Test
  public void testTiDB_V3() {
    actual = MySQLDatabaseMetadata.parse("5.7.25-TiDB-v3.0.14");
    Assert.assertEquals("5.7.25-TiDB-v3.0.14", actual.fullVersion());
    Assert.assertEquals("MySQL", actual.productName());
    Assert.assertEquals(5, actual.majorVersion());
    Assert.assertEquals(7, actual.minorVersion());
    Assert.assertEquals(25, actual.microVersion());
  }

  @Test
  public void testVitess() {
    actual = MySQLDatabaseMetadata.parse("5.7.9-Vitess");
    Assert.assertEquals("5.7.9-Vitess", actual.fullVersion());
    Assert.assertEquals("MySQL", actual.productName());
    Assert.assertEquals(5, actual.majorVersion());
    Assert.assertEquals(7, actual.minorVersion());
    Assert.assertEquals(9, actual.microVersion());
  }

  @Test
  public void testFacebook_V8_0() {
    actual = MySQLDatabaseMetadata.parse("8.0.17 Source distribution");
    Assert.assertEquals("8.0.17 Source distribution", actual.fullVersion());
    Assert.assertEquals("MySQL", actual.productName());
    Assert.assertEquals(8, actual.majorVersion());
    Assert.assertEquals(0, actual.minorVersion());
    Assert.assertEquals(17, actual.microVersion());
  }
}
