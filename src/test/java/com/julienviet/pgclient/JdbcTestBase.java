package com.julienviet.pgclient;


import org.junit.After;
import org.junit.Before;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JdbcTestBase extends PgTestBase {

  Connection con;

  @Before
  public void setUp() throws Exception {
    con = DriverManager.getConnection("jdbc:postgresql://"
      + options.getHost() + ":"
      + options.getPort() + "/postgres", "postgres", "postgres");

  }

  @After
  public void tearDown() throws SQLException {
    if (con != null) {
      con.close();
    }
  }
}
