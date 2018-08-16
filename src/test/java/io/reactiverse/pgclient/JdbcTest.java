/*
 * Copyright (C) 2017 Julien Viet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.reactiverse.pgclient;


import org.junit.Ignore;
import org.junit.Test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;

public class JdbcTest extends JdbcTestBase {

  @Ignore
  @Test
  public void testInsertBatch() throws SQLException {

    PreparedStatement ps = con.prepareStatement("INSERT INTO Test (id, val) VALUES (?, ?)");

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

  @Ignore
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

  @Ignore
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
  public void testPreparedQuery() throws SQLException {
    PreparedStatement ps = con.prepareStatement("SELECT * FROM Fortune WHERE id=(?)");
    ps.setInt(1, 1);
    ResultSet resultSet = ps.executeQuery();
    ps.close();
  }

  @Test
  public void testPreparedQueryWithFetch() throws SQLException {
    con.setAutoCommit(false);
    PreparedStatement ps = con.prepareStatement(
      "SELECT * FROM World",
      ResultSet.TYPE_FORWARD_ONLY,
      ResultSet.CONCUR_READ_ONLY,
      ResultSet.FETCH_FORWARD);
    ps.setFetchSize(2);

    ResultSet resultSet = ps.executeQuery();
    int count = 4;
    while (resultSet.next()) {
      if (count-- == 0) {
        break;
      }
    }
    resultSet.close();
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
