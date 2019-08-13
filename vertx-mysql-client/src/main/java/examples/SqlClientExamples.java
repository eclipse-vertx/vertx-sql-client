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
package examples;

import io.vertx.core.Vertx;
import io.vertx.docgen.Source;
import io.vertx.sqlclient.Cursor;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.PreparedQuery;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.RowStream;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Transaction;
import io.vertx.sqlclient.Tuple;

import java.util.ArrayList;
import java.util.List;

@Source
public class SqlClientExamples {

  public void queries01(SqlClient client) {
    client.query("SELECT * FROM users WHERE id='julien'", ar -> {
      if (ar.succeeded()) {
        RowSet result = ar.result();
        System.out.println("Got " + result.size() + " rows ");
      } else {
        System.out.println("Failure: " + ar.cause().getMessage());
      }
    });
  }


  public void queries02(SqlClient client) {
    client.preparedQuery("SELECT * FROM users WHERE id=?", Tuple.of("julien"), ar -> {
      if (ar.succeeded()) {
        RowSet rows = ar.result();
        System.out.println("Got " + rows.size() + " rows ");
      } else {
        System.out.println("Failure: " + ar.cause().getMessage());
      }
    });
  }

  public void queries03(SqlClient client) {
    client.preparedQuery("SELECT first_name, last_name FROM users", ar -> {
      if (ar.succeeded()) {
        RowSet rows = ar.result();
        for (Row row : rows) {
          System.out.println("User " + row.getString(0) + " " + row.getString(1));
        }
      } else {
        System.out.println("Failure: " + ar.cause().getMessage());
      }
    });
  }

  public void queries04(SqlClient client) {
    client.preparedQuery("INSERT INTO users (first_name, last_name) VALUES (?, ?)", Tuple.of("Julien", "Viet"), ar -> {
      if (ar.succeeded()) {
        RowSet rows = ar.result();
        System.out.println(rows.rowCount());
      } else {
        System.out.println("Failure: " + ar.cause().getMessage());
      }
    });
  }

  public void queries05(Row row) {
    System.out.println("User " + row.getString(0) + " " + row.getString(1));
  }

  public void queries06(Row row) {
    System.out.println("User " + row.getString("first_name") + " " + row.getString("last_name"));
  }

  public void queries07(Row row) {

    String firstName = row.getString("first_name");
    Boolean male = row.getBoolean("male");
    Integer age = row.getInteger("age");

    // ...

  }

  public void queries08(SqlClient client) {
    // TODO
  }

  public void queries09(Vertx vertx, SqlConnectOptions connectOptions, PoolOptions poolOptions) {
    // TODO
  }

  public void usingConnections01(Vertx vertx, Pool pool) {

    pool.getConnection(ar1 -> {
      if (ar1.succeeded()) {
        SqlConnection connection = ar1.result();

        connection.query("SELECT * FROM users WHERE id='julien'", ar2 -> {
          if (ar1.succeeded()) {
            connection.query("SELECT * FROM users WHERE id='paulo'", ar3 -> {
              // Do something with rows and return the connection to the pool
              connection.close();
            });
          } else {
            // Return the connection to the pool
            connection.close();
          }
        });
      }
    });
  }

  public void usingConnections02(SqlConnection connection) {
    connection.prepare("SELECT * FROM users WHERE first_name LIKE ?", ar1 -> {
      if (ar1.succeeded()) {
        PreparedQuery pq = ar1.result();
        pq.execute(Tuple.of("julien"), ar2 -> {
          if (ar2.succeeded()) {
            // All rows
            RowSet rows = ar2.result();
          }
        });
      }
    });
  }

  public void usingConnections03(SqlConnection connection) {
    // TODO
  }

  public void transaction01(Pool pool) {
    // TODO
  }

  public void transaction02(Transaction tx) {
    // TODO
  }

  public void transaction03(Pool pool) {
    // TODO
  }

  public void usingCursors01(SqlConnection connection) {
    connection.prepare("SELECT * FROM users WHERE age > ?", ar1 -> {
      if (ar1.succeeded()) {
        PreparedQuery pq = ar1.result();

        // Create a cursor
        Cursor cursor = pq.cursor(Tuple.of(18));

        // Read 50 rows
        cursor.read(50, ar2 -> {
          if (ar2.succeeded()) {
            RowSet rows = ar2.result();

            // Check for more ?
            if (cursor.hasMore()) {
              // Repeat the process...
            } else {
              // No more rows - close the cursor
              cursor.close();
            }
          }
        });
      }
    });
  }

  public void usingCursors02(Cursor cursor) {
    cursor.read(50, ar2 -> {
      if (ar2.succeeded()) {
        // Close the cursor
        cursor.close();
      }
    });
  }

  public void usingCursors03(SqlConnection connection) {
    connection.prepare("SELECT * FROM users WHERE age > ?", ar1 -> {
      if (ar1.succeeded()) {
        PreparedQuery pq = ar1.result();

        // Fetch 50 rows at a time
        RowStream<Row> stream = pq.createStream(50, Tuple.of(18));

        // Use the stream
        stream.exceptionHandler(err -> {
          System.out.println("Error: " + err.getMessage());
        });
        stream.endHandler(v -> {
          System.out.println("End of stream");
        });
        stream.handler(row -> {
          System.out.println("User: " + row.getString("last_name"));
        });
      }
    });
  }
}
