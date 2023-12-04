package io.vertx.mssqlclient;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.assertEquals;

/**
 * Test the generation of a SQL state code given a vendor error code
 */
@RunWith(Parameterized.class)
public class MSSQLExceptionTest {

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList( new Object[][] {
      // 22001: String data right truncation
      {8152, 1, "22001"},
      // 23000: Integrity constraint violation
      {515, 1, "23000"},
      {547, 1, "23000"},
      {2601, 1, "23000"},
      {2627, 1, "23000"},
      // S0001: Integrity constraint violation
      {2714, 1, "S0001"},
      // S0002: table not found
      {208, 1, "S0002"},
      // deadlock detected
      {1205, 1, "40001"},
      {-100, 54, "S0054"},
      {0, 5, "S0005"},
      {0, 127, "S0127"},
      {0, 0, "S0000"}
    } );
  }

  private int number;
  private byte databaseState;
  private String expectedSqlCode;

  public MSSQLExceptionTest(int vendorCode, int state, String sqlStateCode) {
    this.number = vendorCode;
    this.databaseState = (byte) state;
    this.expectedSqlCode = sqlStateCode;
  }

  @Test
  public void testSqlStateCodes() {
    MSSQLException mssqlException = new MSSQLException( number, databaseState, (byte) 0, null, null, null, 1 );
    assertEquals( expectedSqlCode, mssqlException.getSqlState() );
    assertEquals( number, mssqlException.getErrorCode() );
  }
}
