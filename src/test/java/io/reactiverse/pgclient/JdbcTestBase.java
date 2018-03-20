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


import org.junit.After;
import org.junit.Before;
import org.postgresql.PGProperty;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class JdbcTestBase extends PgTestBase {

  Connection con;

  @Before
  public void setUp() throws Exception {
    Properties props = new Properties();
    PGProperty.PREPARE_THRESHOLD.set(props, -1);
    PGProperty.BINARY_TRANSFER.set(props, "true");
    // PGProperty.BINARY_TRANSFER_ENABLE.set(props, "true");
    PGProperty.USER.set(props, "postgres");
    PGProperty.PASSWORD.set(props, "postgres");
    con = DriverManager.getConnection("jdbc:postgresql://"
      + options.getHost() + ":"
      + options.getPort() + "/postgres", props);

  }

  @After
  public void tearDown() throws SQLException {
    if (con != null) {
      con.close();
    }
  }
}
