package com.julienviet.pgclient;


import org.junit.Test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JdbcTest extends JdbcTestBase {

  @Test
  public void testInsertBatch() throws SQLException {

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

    ps.executeBatch();

    assertEquals(-1, ps.getUpdateCount());

    ps.close();

    assertEquals(true, ps.isClosed());

  }

  @Test
  public void testInsertPreparedStmtWithId() throws SQLException {
    PreparedStatement ps = con.prepareStatement("INSERT INTO Fortune (id , message) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
    ps.setInt(1, 9000);
    ps.setString(2, "Hello World");
    assertEquals(1, ps.executeUpdate());
    ResultSet re = ps.getGeneratedKeys();
    re.next();
    assertEquals(9000, re.getInt(1));
    assertEquals("Hello World", re.getString(2));
    ps.close();
  }

  @Test
  public void testUpdatePreparedStmtWithId() throws SQLException {
    con.setAutoCommit(false);
    PreparedStatement ps = con.prepareStatement("UPDATE Fortune SET message = ? WHERE id = ?", Statement.RETURN_GENERATED_KEYS);
    ps.setString(1, "Hello World");
    ps.setInt(2, 1);
    assertEquals(1, ps.executeUpdate());
    con.commit();
    ResultSet re = ps.getGeneratedKeys();
    re.next();
    assertEquals(1, re.getInt(1));
    assertEquals("Hello World", re.getString(2));
    ps.close();
  }

  @Test
  public void testCursor() throws SQLException {

    con.setAutoCommit(false);

    Statement ps = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

    ps.setFetchSize(3);

    ResultSet rs = ps.executeQuery("SELECT * FROM Fortune");
    int count = 0;
    while (rs.next()) {
      count++;
    }
    System.out.println("got result " + count);

  }
}
