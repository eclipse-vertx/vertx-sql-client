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

import com.julienviet.pgclient.PgBatch;
import com.julienviet.pgclient.PgClient;
import com.julienviet.pgclient.PgClientOptions;
import com.julienviet.pgclient.PgConnection;
import com.julienviet.pgclient.PgConnectionPool;
import com.julienviet.pgclient.PgPoolOptions;
import com.julienviet.pgclient.PgPreparedStatement;
import com.julienviet.pgclient.PgQuery;
import io.vertx.core.Vertx;
import io.vertx.docgen.Source;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.UpdateResult;

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

        conn.query("SELECT * FROM USERS", ar -> {

          if (ar.succeeded()) {

            // Use result set
            ResultSet rs = ar.result();
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

    PgConnectionPool pool = client.createPool(new PgPoolOptions().setMaxSize(20));

    pool.getConnection(res -> {
      if (res.succeeded()) {

        // Obtained a connection
        PgConnection conn = res.result();

        conn.query("SELECT * FROM USERS", ar -> {

          if (ar.succeeded()) {

            // Use result set
            ResultSet rs = ar.result();
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

  public void ex3(PgConnectionPool pool) {

    // Close the pool and the connection it maintains
    pool.close();
  }

  public void ex4(PgConnection conn) {
    PgPreparedStatement preparedStatement = conn.prepare("SELECT * FROM USERS WHERE user_id=$1");

    // Create a query : bind parameters
    PgQuery query = preparedStatement.query("julien");

    // Execute query
    query.execute(res -> {
      if (res.succeeded()) {

        // Get result
        ResultSet result = res.result();
      } else {
        System.out.println("Query failed " + res.cause());
      }
    });
  }

  public void ex5(PgPreparedStatement preparedStatement) {
    preparedStatement.close();
  }

  public void ex6(PgConnection conn) {
    PgPreparedStatement preparedStatement = conn.prepare("SELECT * FROM USERS");

    // Create a query : bind parameters
    PgQuery query = preparedStatement.query();

    // Get at most 100 rows
    query.fetch(100);

    // Execute query
    query.execute(res -> {
      if (res.succeeded()) {

        // Get result
        ResultSet result = res.result();

        //
        if (query.completed()) {
          // We are done
        } else {

          // Fetch 100 more
          query.execute(res2 -> {
            // And the beat goes on...
          });
        }
      } else {
        System.out.println("Query failed " + res.cause());
      }
    });
  }

  public void ex7(PgConnection conn) {
    PgPreparedStatement preparedStatement = conn.prepare("SELECT * FROM USERS");

    // Create a query : bind parameters
    PgQuery query = preparedStatement.query();

    // Get at most 100 rows
    query.fetch(100);

    // Execute query
    query.execute(res -> {
      if (res.succeeded()) {

        // Get result
        ResultSet result = res.result();

        // Close the query
        query.close();
      } else {
        System.out.println("Query failed " + res.cause());
      }
    });
  }

  public void ex8(PgConnection conn) {
    PgPreparedStatement preparedStatement = conn.prepare("INSERT INTO USERS (id, name) VALUES ($1, $2)");

    // Create a query : bind parameters
    PgBatch batch = preparedStatement.batch();

    // Add commands to the batch
    batch.add("julien", "Julien Viet");
    batch.add("emad", "Emad Alblueshi");

    batch.execute(res -> {
      if (res.succeeded()) {

        // Process results
        List<UpdateResult> results = res.result();
      } else {
        System.out.println("Batch failed " + res.cause());
      }
    });
  }
}
