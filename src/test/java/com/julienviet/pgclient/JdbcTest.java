package com.julienviet.pgclient;


import org.junit.Test;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class JdbcTest extends JdbcTestBase {

  @Test
  public void testInsertBatch() throws SQLException {

    Statement stmt = con.createStatement();

    stmt.execute("BEGIN");

    assertEquals(false, stmt.isClosed());

    PreparedStatement ps = con.prepareStatement("INSERT INTO Fortune (id, message) VALUES (?, ?)");

    ps.setInt(1, 2000);
    ps.setString(2, "Hello");
    ps.addBatch();

    ps.setInt(1, 2001);
    ps.setString(2, "Vert.x");
    ps.addBatch();

    ps.setInt(1, 2002);
    ps.setString(2, "World");
    ps.addBatch();


    assertEquals(-1, ps.getUpdateCount());

    ps.close();

    assertEquals(true, ps.isClosed());

    stmt.execute("COMMIT");

    stmt.close();

    assertEquals(true, stmt.isClosed());
  }

  @Test
  public void testInsertPreparedStmt() throws SQLException {
    PreparedStatement ps = con.prepareStatement("INSERT INTO Fortune (id, message) VALUES (?, ?)");
    ps.setInt(1,3000);
    ps.setString(2, "Hello INSERT");
    assertEquals(false , ps.execute());
    ps.close();
  }
}
