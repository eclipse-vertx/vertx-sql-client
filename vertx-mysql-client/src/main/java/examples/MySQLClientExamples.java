package examples;

import io.vertx.core.Vertx;
import io.vertx.docgen.Source;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLConnection;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.mysqlclient.MySQLSetOption;
import io.vertx.sqlclient.Cursor;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.PreparedQuery;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.RowStream;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.SqlResult;
import io.vertx.sqlclient.Transaction;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.data.Numeric;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Source
public class MySQLClientExamples {
  public void gettingStarted() {

    // Connect options
    MySQLConnectOptions connectOptions = new MySQLConnectOptions()
      .setPort(3306)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUser("user")
      .setPassword("secret");

    // Pool options
    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(5);

    // Create the client pool
    MySQLPool client = MySQLPool.pool(connectOptions, poolOptions);

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

  public void connecting01() {

    // Connect options
    MySQLConnectOptions connectOptions = new MySQLConnectOptions()
      .setPort(3306)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUser("user")
      .setPassword("secret");

    // Pool options
    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(5);

    // Create the pooled client
    MySQLPool client = MySQLPool.pool(connectOptions, poolOptions);
  }


  public void connecting02(Vertx vertx) {

    // Connect options
    MySQLConnectOptions connectOptions = new MySQLConnectOptions()
      .setPort(3306)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUser("user")
      .setPassword("secret");

    // Pool options
    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(5);
    // Create the pooled client
    MySQLPool client = MySQLPool.pool(vertx, connectOptions, poolOptions);
  }

  public void connecting03(Pool pool) {

    // Close the pool and all the associated resources
    pool.close();
  }

  public void connecting04(Vertx vertx) {

    // Connect options
    MySQLConnectOptions connectOptions = new MySQLConnectOptions()
      .setPort(3306)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUser("user")
      .setPassword("secret");

    // Pool options
    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(5);

    // Create the pooled client
    MySQLPool client = MySQLPool.pool(vertx, connectOptions, poolOptions);

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

  public void booleanExample01(SqlClient client) {
    client.query("SELECT graduated FROM students WHERE id = 0", ar -> {
      if (ar.succeeded()) {
        RowSet rowSet = ar.result();
        for (Row row : rowSet) {
          int pos = row.getColumnIndex("graduated");
          Byte value = row.get(Byte.class, pos);
          Boolean graduated = row.getBoolean("graduated");
        }
      } else {
        System.out.println("Failure: " + ar.cause().getMessage());
      }
    });
  }

  public void booleanExample02(SqlClient client) {
    client.preparedQuery("UPDATE students SET graduated = ? WHERE id = 0", Tuple.of(true), ar -> {
      if (ar.succeeded()) {
        System.out.println("Updated with the boolean value");
      } else {
        System.out.println("Failure: " + ar.cause().getMessage());
      }
    });
  }

  public void numericExample(Row row) {
    Numeric numeric = row.get(Numeric.class, 0);
    if (numeric.isNaN()) {
      // Handle NaN
    } else {
      BigDecimal value = numeric.bigDecimalValue();
    }
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

  public void pingExample(MySQLConnection connection) {
    connection.ping(ar -> {
      System.out.println("The server has responded to the PING");
    });
  }

  public void resetConnectionExample(MySQLConnection connection) {
    connection.resetConnection(ar -> {
      if (ar.succeeded()) {
        System.out.println("Connection has been reset now");
      } else {
        System.out.println("Failure: " + ar.cause().getMessage());
      }
    });
  }

  public void changeUserExample(MySQLConnection connection) {
    MySQLConnectOptions authenticationOptions = new MySQLConnectOptions()
      .setUser("newuser")
      .setPassword("newpassword")
      .setDatabase("newdatabase");
    connection.changeUser(authenticationOptions, ar -> {
      if (ar.succeeded()) {
        System.out.println("User of current connection has been changed.");
      } else {
        System.out.println("Failure: " + ar.cause().getMessage());
      }
    });
  }

  public void initDbExample(MySQLConnection connection) {
    connection.specifySchema("newschema", ar -> {
      if (ar.succeeded()) {
        System.out.println("Default schema changed to newschema");
      } else {
        System.out.println("Failure: " + ar.cause().getMessage());
      }
    });
  }

  public void statisticsExample(MySQLConnection connection) {
    connection.getInternalStatistics(ar -> {
      if (ar.succeeded()) {
        System.out.println("Statistics: " + ar.result());
      } else {
        System.out.println("Failure: " + ar.cause().getMessage());
      }
    });
  }

  public void debugExample(MySQLConnection connection) {
    connection.debug(ar -> {
      if (ar.succeeded()) {
        System.out.println("Debug info dumped to server's STDOUT");
      } else {
        System.out.println("Failure: " + ar.cause().getMessage());
      }
    });
  }

  public void setOptionExample01(MySQLConnection connection) {
    connection.setOption(MySQLSetOption.MYSQL_OPTION_MULTI_STATEMENTS_OFF, ar -> {
      if (ar.succeeded()) {
        System.out.println("CLIENT_MULTI_STATEMENTS is off now");
      } else {
        System.out.println("Failure: " + ar.cause().getMessage());
      }
    });
  }
}
