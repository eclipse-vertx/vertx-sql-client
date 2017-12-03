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
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@Source
public class Examples {

  public void ex1(Vertx vertx) {

    PgClient client = PgClient.create(vertx, new PgClientOptions()
      .setPort(5432)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUsername("user")
      .setPassword("secret")
    );

    client.connect(res -> {
      if (res.succeeded()) {

        // Connected
        PgConnection conn = res.result();

        conn.query("SELECT * FROM USERS").execute(ar -> {

          if (ar.succeeded()) {

            // Use result
            PgResult<Tuple> result = ar.result();
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

    PgClient client = PgClient.create(vertx, new PgClientOptions()
      .setPort(5432)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUsername("user")
      .setPassword("secret")
    );

    PgPool pool = client.createPool(new PgPoolOptions().setMaxSize(20));

    pool.getConnection(res -> {
      if (res.succeeded()) {

        // Obtained a connection
        PgConnection conn = res.result();

        conn.query("SELECT * FROM USERS").execute(ar -> {

          if (ar.succeeded()) {

            // Use result set
            PgResult<Tuple> result = ar.result();
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

  public void ex4_(PgConnection conn) {

    // Prepare (when not cached)
    // Execute
    conn.preparedQuery("SELECT * FROM USERS WHERE user_id=$1", Tuple.of("julien"), ar -> {

      if (ar.succeeded()) {

        // Get result
        PgResult<Tuple> result = ar.result();
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
        PgQuery query = preparedStatement.query(Tuple.of("julien"));

        // Execute query
        query.execute(ar2 -> {
          if (ar2.succeeded()) {

            // Get result
            PgResult<Tuple> result = ar2.result();
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
        PgQuery query = preparedStatement.query()
          .fetch(100); // Get at most 100 rows at a time

        query.execute(ar2 -> {

          if (ar2.succeeded()) {
            System.out.println("Got at most 100 rows");

            if (query.hasNext()) {
              // Get results
              PgResult<Tuple> result = ar2.result();

              System.out.println("Get next 100");
              query.next(ar3 -> {
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
        PgQuery query = preparedStatement.query();

        // Get at most 100 rows
        query.fetch(100);

        // Execute query
        query.execute(res -> {
          if (res.succeeded()) {

            // Get result
            PgResult<Tuple> result = res.result();

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
        PgResult<Tuple> result = ar.result();
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
        PgBatch batch = preparedStatement.batch();

        // Add commands to the batch
        batch.add(Tuple.of("julien", "Julien Viet"));
        batch.add(Tuple.of("emad", "Emad Alblueshi"));

        batch.execute(res -> {
          if (res.succeeded()) {

            // Process results
            PgBatchResult<Tuple> results = res.result();
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
        PgBatchResult<Tuple> results = ar.result();
      } else {
        System.out.println("Batch failed " + ar.cause());
      }
    });
  }

  public void ex10(Vertx vertx) {

    PgClient client = PgClient.create(vertx, new PgClientOptions()
      .setPort(5432)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUsername("user")
      .setPassword("secret")
      .setSsl(true)
      .setPemTrustOptions(new PemTrustOptions().addCertPath("/path/to/cert.pem"))
    );

    client.connect(res -> {
      if (res.succeeded()) {
        // Connected with SSL
      } else {
        System.out.println("Could not connect " + res.cause());
      }
    });
  }
}
