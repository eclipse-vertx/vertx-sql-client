package io.vertx.mssql;

import org.junit.*;

import java.math.BigDecimal;
import java.sql.*;

// More information in https://support.office.com/en-ie/article/equivalent-ansi-sql-data-types-7a0a6bef-ef25-45f9-8a9a-3c5f21b5c65d
// just for checking ansi sql data type in MSSQL currently
@Ignore("enable this test only in need")
public class MssqlTextDataTypeTest {
  Connection conn;

  @Before
  public void setup() throws Exception {
// FIXME  ATTENTION: manually start a docker container https://hub.docker.com/r/microsoft/mssql-server-linux/
    conn = DriverManager.getConnection("jdbc:sqlserver://localhost:1433", "sa", "yourStrong(!)Password");
  }

  @After
  public void tearDown() throws SQLException {
    if (conn != null) {
      conn.close();
    }
  }

  @Test
  public void testSmallInt() {
    testDecodeGeneric("32767", "SMALLINT", "test_int_2", (short) 32767);
  }

  @Test
  public void testInteger() {
    testDecodeGeneric("2147483647", "INTEGER", "test_int_4", 2147483647);
  }

  @Test
  public void testBigInt() {
    testDecodeGeneric("9223372036854775807", "BIGINT", "test_int_8", 9223372036854775807L);
  }

  @Test
  public void testFloat() {
    testDecodeGeneric("3.40282E38", "REAL", "test_float_4", (float) 3.40282e38F);
  }

  @Test
  public void testDouble() {
    testDecodeGeneric("1.7976931348623157E308", "FLOAT8", "test_float_8", 1.7976931348623157E308D);
  }

  @Test
  public void testNumeric() {
    testDecodeGeneric("999.99", "NUMERIC", "test_numeric", BigDecimal.valueOf(999.99));
  }

  @Test
  public void testDecimal() {
    testDecodeGeneric("12345", "DECIMAL", "test_decimal", BigDecimal.valueOf(12345));
  }

  @Test
  public void testChar() {
    testDecodeGeneric("testchar", "CHAR(8)", "test_char", "testchar");
  }

  @Test
  public void testVarchar() {
    testDecodeGeneric("testvarchar", "VARCHAR(20)", "test_varchar", "testvarchar");
  }

  protected <T> void testDecodeGeneric(String data,
                                       String dataType,
                                       String columnName,
                                       T expected) {
    try {
      Statement statement = conn.createStatement();
      ResultSet resultSet = statement.executeQuery("SELECT " + columnName + " FROM basicdatatype WHERE id = 1");
      resultSet.next();
      Assert.assertEquals(expected, resultSet.getObject(columnName));
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
