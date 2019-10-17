package examples;

import io.vertx.mssqlclient.MSSQLConnectOptions;
import io.vertx.mssqlclient.MSSQLPool;
import io.vertx.core.Vertx;
import io.vertx.docgen.Source;
import io.vertx.sqlclient.*;

import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Source
public class MSSQLClientExamples {
  public void gettingStarted() {

    // Connect options
    MSSQLConnectOptions connectOptions = new MSSQLConnectOptions()
      .setPort(1433)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUser("user")
      .setPassword("secret");

    // Pool options
    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(5);

    // Create the client pool
    MSSQLPool client = MSSQLPool.pool(connectOptions, poolOptions);

    // A simple query
    client.query("SELECT * FROM users WHERE id='julien'", ar -> {
      if (ar.succeeded()) {
        RowSet result = ar.result();
        System.out.println("Got " + result.size() + " rows ");
      } else {
        System.out.println("Failure: " + ar.cause().getMessage());
      }

      // Now close the pool
      client.close();
    });
  }

  public void configureFromDataObject(Vertx vertx) {

    // Data object
    MSSQLConnectOptions connectOptions = new MSSQLConnectOptions()
      .setPort(1433)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUser("user")
      .setPassword("secret");

    // Pool Options
    PoolOptions poolOptions = new PoolOptions().setMaxSize(5);

    // Create the pool from the data object
    MSSQLPool pool = MSSQLPool.pool(vertx, connectOptions, poolOptions);

    pool.getConnection(ar -> {
      // Handling your connection
    });
  }

  public void connecting01() {

    // Connect options
    MSSQLConnectOptions connectOptions = new MSSQLConnectOptions()
      .setPort(1433)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUser("user")
      .setPassword("secret");

    // Pool options
    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(5);

    // Create the pooled client
    MSSQLPool client = MSSQLPool.pool(connectOptions, poolOptions);
  }


  public void connecting02(Vertx vertx) {

    // Connect options
    MSSQLConnectOptions connectOptions = new MSSQLConnectOptions()
      .setPort(1433)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUser("user")
      .setPassword("secret");

    // Pool options
    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(5);
    // Create the pooled client
    MSSQLPool client = MSSQLPool.pool(vertx, connectOptions, poolOptions);
  }

  public void connecting03(Pool pool) {

    // Close the pool and all the associated resources
    pool.close();
  }

  public void connecting04(Vertx vertx) {

    // Connect options
    MSSQLConnectOptions connectOptions = new MSSQLConnectOptions()
      .setPort(1433)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUser("user")
      .setPassword("secret");

    // Pool options
    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(5);

    // Create the pooled client
    MSSQLPool client = MSSQLPool.pool(vertx, connectOptions, poolOptions);

    // Get a connection from the pool
    client.getConnection(ar1 -> {

      if (ar1.succeeded()) {

        System.out.println("Connected");

        // Obtain our connection
        SqlConnection conn = ar1.result();

        // All operations execute on the same connection
        conn.query("SELECT * FROM users WHERE id='julien'", ar2 -> {
          if (ar2.succeeded()) {
            conn.query("SELECT * FROM users WHERE id='emad'", ar3 -> {
              // Release the connection to the pool
              conn.close();
            });
          } else {
            // Release the connection to the pool
            conn.close();
          }
        });
      } else {
        System.out.println("Could not connect: " + ar1.cause().getMessage());
      }
    });
  }

  public void collector01Example(SqlClient client) {

    // Create a collector projecting a row set to a map
    Collector<Row, ?, Map<Long, String>> collector = Collectors.toMap(
      row -> row.getLong("id"),
      row -> row.getString("last_name"));

    // Run the query with the collector
    client.query("SELECT * FROM users",
      collector,
      ar -> {
        if (ar.succeeded()) {
          SqlResult<Map<Long, String>> result = ar.result();

          // Get the map created by the collector
          Map<Long, String> map = result.value();
          System.out.println("Got " + map);
        } else {
          System.out.println("Failure: " + ar.cause().getMessage());
        }
      });
  }

  public void collector02Example(SqlClient client) {

    // Create a collector projecting a row set to a (last_name_1,last_name_2,...)
    Collector<Row, ?, String> collector = Collectors.mapping(
      row -> row.getString("last_name"),
      Collectors.joining(",", "(", ")")
    );

    // Run the query with the collector
    client.query("SELECT * FROM users",
      collector,
      ar -> {
        if (ar.succeeded()) {
          SqlResult<String> result = ar.result();

          // Get the string created by the collector
          String list = result.value();
          System.out.println("Got " + list);
        } else {
          System.out.println("Failure: " + ar.cause().getMessage());
        }
      });
  }
}
