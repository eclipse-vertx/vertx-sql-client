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

import com.julienviet.pgclient.*;
import io.vertx.core.Vertx;
import io.vertx.core.net.PemTrustOptions;
import io.vertx.docgen.Source;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@Source
public class Examples {

  public void gettingStarted() {

    // Pool options
    PgPoolOptions options = new PgPoolOptions()
      .setPort(5432)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUsername("user")
      .setPassword("secret")
      .setMaxSize(5);

    // Create the pool
    PgPool pool = PgPool.pool(options);

    // A simple query
    pool.query("SELECT * FROM users WHERE id='julien'", ar -> {
      if (ar.succeeded()) {
        PgResult<Row> result = ar.result();
        System.out.println("Got " + result.size() + " results ");
      } else {
        System.out.println("Failure: " + ar.cause().getMessage());
      }

      // Close now the pool
      pool.close();
    });
  }

  public void connecting01() {

    // Pool options
    PgPoolOptions options = new PgPoolOptions()
      .setPort(5432)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUsername("user")
      .setPassword("secret")
      .setMaxSize(5);

    // Create the pool
    PgPool pool = PgPool.pool(options);
  }

  public void connecting02(Vertx vertx) {

    // Pool options
    PgPoolOptions options = new PgPoolOptions()
      .setPort(5432)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUsername("user")
      .setPassword("secret")
      .setMaxSize(5);

    // Create the pool
    PgPool pool = PgPool.pool(vertx, options);
  }

  public void connecting03(PgPool pool) {

    // Close the pool and all the associated resources
    pool.close();
  }

  public void connecting04(Vertx vertx) {

    // Pool options
    PgConnectOptions options = new PgConnectOptions()
      .setPort(5432)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUsername("user")
      .setPassword("secret");

    // Close the pool and all the associated resources
    PgConnection.connect(vertx, options, res -> {
      if (res.succeeded()) {

        System.out.println("Connected");

        // Obtain our connection
        PgConnection conn = res.result();
      } else {
        System.out.println("Could not connect: " + res.cause().getMessage());
      }
    });
  }

  public void queries01(PgClient pool) {
    pool.query("SELECT * FROM users WHERE id='julien'", ar -> {
      if (ar.succeeded()) {
        PgResult<Row> result = ar.result();
        System.out.println("Got " + result.size() + " results ");
      } else {
        System.out.println("Failure: " + ar.cause().getMessage());
      }
    });
  }

  public void queries02(PgClient pool) {
    pool.preparedQuery("SELECT * FROM users WHERE id=$1", Tuple.of("julien"),  ar -> {
      if (ar.succeeded()) {
        PgResult<Row> result = ar.result();
        System.out.println("Got " + result.size() + " results ");
      } else {
        System.out.println("Failure: " + ar.cause().getMessage());
      }
    });
  }

  public void queries03(PgClient pool) {
    pool.preparedQuery("SELECT first_name, last_name FROM users", ar -> {
      if (ar.succeeded()) {
        PgResult<Row> result = ar.result();
        for (Row row : result) {
          System.out.println("User " + row.getString(0) + " " + row.getString(1));
        }
      } else {
        System.out.println("Failure: " + ar.cause().getMessage());
      }
    });
  }

  public void queries04(PgClient pool) {
    pool.preparedQuery("\"INSERT INTO users (first_name, last_name) VALUES ($1, $2)", Tuple.of("Julien", "Viet"),  ar -> {
      if (ar.succeeded()) {
        PgResult<Row> result = ar.result();
        System.out.println(result.updatedCount());
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

  public void transaction01(PgPool pool) {
    pool.connect(res -> {
      if (res.succeeded()) {

        // Transaction must use a connection
        PgConnection conn = res.result();

        // Begin the transaction
        conn.begin();

        // Statements
        conn.query("INSERT INTO Users (first_name,last_name) VALUES ('Julien','Viet')", ar2 ->{});
        conn.query("INSERT INTO Users (first_name,last_name) VALUES ('Emad','Alblueshi')", ar2 ->{});

        // Commit the transaction
        conn.commit(ar -> {
          if (ar.succeeded()) {
            System.out.println("Transaction succeeded");
          } else {
            System.out.println("Transaction failed " + ar.cause().getMessage());
          }
        });
      }
    });
  }

  public void transaction02(PgPool pool) {
    pool.connect(res -> {
      if (res.succeeded()) {

        // Transaction must use a connection
        PgConnection conn = res.result();

        // Begin the transaction
        conn.begin();

        // Statements
        conn.query("INSERT INTO Users (first_name,last_name) VALUES ('Julien','Viet')", ar -> {});

        // Triggers a transaction rollback
        conn.query("a statement that fails", ar -> {});

        // Attempt to commit the transaction
        conn.commit(ar -> {
          // This won't be executed (?)
          // should it be executed or not ?
        });
      }
    });
  }

  public void ex1(Vertx vertx) {

    // Create options
    PgConnectOptions options = new PgConnectOptions()
      .setPort(5432)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUsername("user")
      .setPassword("secret");

    // Connect
    PgConnection.connect(vertx, options, res -> {
      if (res.succeeded()) {

        // Connected
        PgConnection conn = res.result();

        conn.createQuery("SELECT * FROM USERS").execute(ar -> {

          if (ar.succeeded()) {

            // Use result
            PgResult<Row> result = ar.result();
          } else {
            System.out.println("It failed");
          }

          // Close the connection
          conn.close();
        });
      } else {
        System.out.println("Could not connect " + res.cause());
      }
    });
  }

  public void ex2(Vertx vertx) {

    PgPoolOptions options = new PgPoolOptions()
      .setPort(5432)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUsername("user")
      .setPassword("secret")
      .setMaxSize(20);

    // Create a pool with 20 connections max
    PgPool pool = PgPool.pool(vertx, options);

    pool.connect(res -> {
      if (res.succeeded()) {

        // Obtained a connection
        PgConnection conn = res.result();

        conn.createQuery("SELECT * FROM USERS").execute(ar -> {

          if (ar.succeeded()) {

            // Use result set
            PgResult<Row> result = ar.result();
          } else {
            System.out.println("It failed");
          }

          // Return the connection to the pool
          conn.close();
        });
      } else {
        System.out.println("Could not obtain a connection " + res.cause());
      }
    });
  }

  public void ex3(PgPool pool) {

    // Close the pool and the connection it maintains
    pool.close();
  }

  public void ex4_(PgPool conn) {

    // Prepare (when not cached)
    // Execute
    conn.preparedQuery("SELECT * FROM USERS WHERE user_id=$1", Tuple.of("julien"), ar -> {

      if (ar.succeeded()) {

        // Get result
        PgResult<Row> result = ar.result();
      } else {
        System.out.println("Query failed " + ar.cause());
      }
    });
  }

  public void ex4(PgConnection conn) {
    conn.prepare("SELECT * FROM USERS WHERE user_id=$1", ar1 -> {

      if (ar1.succeeded()) {
        PgPreparedStatement preparedStatement = ar1.result();

        // Create a query : bind parameters
        PgQuery query = preparedStatement.createQuery(Tuple.of("julien"));

        // Execute query
        query.execute(ar2 -> {
          if (ar2.succeeded()) {

            // Get result
            PgResult<Row> result = ar2.result();
          } else {
            System.out.println("Query failed " + ar2.cause());
          }
        });
      } else {
        System.out.println("Could not prepare statement " + ar1.cause());
      }
    });
  }

  public void ex5(PgPreparedStatement preparedStatement) {
    preparedStatement.close();
  }

  public void ex6(PgConnection conn) {
    conn.prepare("SELECT * FROM USERS", ar1 -> {
      if (ar1.succeeded()) {

        PgPreparedStatement preparedStatement = ar1.result();

        // Create a query : bind parameters
        PgQuery query = preparedStatement.createQuery()
          .fetch(100); // Get at most 100 rows at a time

        query.execute(ar2 -> {

          if (ar2.succeeded()) {
            System.out.println("Got at most 100 rows");

            if (query.hasMore()) {
              // Get results
              PgResult<Row> result = ar2.result();

              System.out.println("Get next 100");
              query.execute(ar3 -> {
                // Continue...
              });
            } else {
              // We are done
            }
          } else {
            System.out.println("Query failed " + ar2.cause());
          }
        });
      } else {
        System.out.println("Could not prepare statement " + ar1.cause());
      }
    });
  }

  public void ex7(PgConnection conn) {
    conn.prepare("SELECT * FROM USERS", ar1 -> {

      if (ar1.succeeded()) {
        PgPreparedStatement preparedStatement = ar1.result();

        // Create a query : bind parameters
        PgQuery query = preparedStatement.createQuery();

        // Get at most 100 rows
        query.fetch(100);

        // Execute query
        query.execute(res -> {
          if (res.succeeded()) {

            // Get result
            PgResult<Row> result = res.result();

            // Close the query
            query.close();
          } else {
            System.out.println("Query failed " + res.cause());
          }
        });
      } else {
        System.out.println("Could not prepare statement " + ar1.cause());
      }
    });
  }

  public void ex8(PgConnection conn) {

    // Prepare (when not cached)
    // Execute
    conn.preparedQuery("UPDATE USERS SET name=$1 WHERE id=$2", Tuple.of(2, "EMAD ALBLUESHI"), ar -> {

      if(ar.succeeded()) {
        // Process results
        PgResult<Row> result = ar.result();
      } else {
        System.out.println("Update failed " + ar.cause());
      }
    });
  }

  public void ex9(PgConnection conn) {
    conn.prepare("INSERT INTO USERS (id, name) VALUES ($1, $2)", ar1 -> {
      if (ar1.succeeded()) {
        PgPreparedStatement preparedStatement = ar1.result();

        // Create a query : bind parameters
        PgBatch batch = preparedStatement.createBatch();

        // Add commands to the createBatch
        batch.add(Tuple.of("julien", "Julien Viet"));
        batch.add(Tuple.of("emad", "Emad Alblueshi"));

        batch.execute(res -> {
          if (res.succeeded()) {

            // Process results
            PgBatchResult<Row> results = res.result();
          } else {
            System.out.println("Batch failed " + res.cause());
          }
        });
      } else {
        System.out.println("Could not prepare statement " + ar1.cause());
      }
    });
  }

  public void ex9_(PgConnection conn) {

    List<Tuple> batch = new ArrayList<>();
    batch.add(Tuple.of("julien", "Julien Viet"));
    batch.add(Tuple.of("emad", "Emad Alblueshi"));

    conn.preparedBatch("INSERT INTO USERS (id, name) VALUES ($1, $2)", batch, ar -> {
      if (ar.succeeded()) {

        // Process results
        PgBatchResult<Row> results = ar.result();
      } else {
        System.out.println("Batch failed " + ar.cause());
      }
    });
  }

  public void ex10(Vertx vertx) {

    PgConnectOptions options = new PgConnectOptions()
      .setPort(5432)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUsername("user")
      .setPassword("secret")
      .setSsl(true)
      .setPemTrustOptions(new PemTrustOptions().addCertPath("/path/to/cert.pem"));

    PgConnection.connect(vertx, options, res -> {
      if (res.succeeded()) {
        // Connected with SSL
      } else {
        System.out.println("Could not connect " + res.cause());
      }
    });
  }
}
