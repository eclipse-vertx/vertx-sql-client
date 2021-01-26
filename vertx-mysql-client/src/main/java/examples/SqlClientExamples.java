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

import io.netty.buffer.ByteBuf;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.tracing.TracingPolicy;
import io.vertx.docgen.Source;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.typecodec.MySQLDataTypeDefaultCodecs;
import io.vertx.mysqlclient.typecodec.MySQLType;
import io.vertx.sqlclient.Cursor;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.PreparedStatement;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.RowStream;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Transaction;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.codec.DataType;
import io.vertx.sqlclient.codec.DataTypeCodec;
import io.vertx.sqlclient.codec.DataTypeCodecRegistry;
import io.vertx.sqlclient.impl.codec.CommonCodec;

import java.nio.charset.Charset;
import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Source
public class SqlClientExamples {

  public void queries01(SqlClient client) {
    client
      .query("SELECT * FROM users WHERE id='julien'")
      .execute(ar -> {
      if (ar.succeeded()) {
        RowSet<Row> result = ar.result();
        System.out.println("Got " + result.size() + " rows ");
      } else {
        System.out.println("Failure: " + ar.cause().getMessage());
      }
    });
  }


  public void queries02(SqlClient client) {
    client
      .preparedQuery("SELECT * FROM users WHERE id=?")
      .execute(Tuple.of("julien"), ar -> {
      if (ar.succeeded()) {
        RowSet<Row> rows = ar.result();
        System.out.println("Got " + rows.size() + " rows ");
      } else {
        System.out.println("Failure: " + ar.cause().getMessage());
      }
    });
  }

  public void queries03(SqlClient client) {
    client
      .preparedQuery("SELECT first_name, last_name FROM users")
      .execute(ar -> {
      if (ar.succeeded()) {
        RowSet<Row> rows = ar.result();
        for (Row row : rows) {
          System.out.println("User " + row.getString(0) + " " + row.getString(1));
        }
      } else {
        System.out.println("Failure: " + ar.cause().getMessage());
      }
    });
  }

  public void queries04(SqlClient client) {
    client
      .preparedQuery("INSERT INTO users (first_name, last_name) VALUES (?, ?)")
      .execute(Tuple.of("Julien", "Viet"), ar -> {
      if (ar.succeeded()) {
        RowSet<Row> rows = ar.result();
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

    // Add commands to the batch
    List<Tuple> batch = new ArrayList<>();
    batch.add(Tuple.of("julien", "Julien Viet"));
    batch.add(Tuple.of("emad", "Emad Alblueshi"));

    // Execute the prepared batch
    client
      .preparedQuery("INSERT INTO USERS (id, name) VALUES (?, ?)")
      .executeBatch(batch, res -> {
      if (res.succeeded()) {

        // Process rows
        RowSet<Row> rows = res.result();
      } else {
        System.out.println("Batch failed " + res.cause());
      }
    });
  }

  public void queries09(SqlClient client, SqlConnectOptions connectOptions) {

    // Enable prepare statements caching
    connectOptions.setCachePreparedStatements(true);
    client
      .preparedQuery("SELECT * FROM users WHERE id = ?")
      .execute(Tuple.of("julien"), ar -> {
        if (ar.succeeded()) {
          RowSet<Row> rows = ar.result();
          System.out.println("Got " + rows.size() + " rows ");
        } else {
          System.out.println("Failure: " + ar.cause().getMessage());
        }
      });
  }

  public void queries10(SqlConnection sqlConnection) {
    sqlConnection
      .prepare("SELECT * FROM users WHERE id = ?", ar -> {
        if (ar.succeeded()) {
          PreparedStatement preparedStatement = ar.result();
          preparedStatement.query()
            .execute(Tuple.of("julien"), ar2 -> {
              if (ar2.succeeded()) {
                RowSet<Row> rows = ar2.result();
                System.out.println("Got " + rows.size() + " rows ");
                preparedStatement.close();
              } else {
                System.out.println("Failure: " + ar2.cause().getMessage());
              }
            });
        } else {
          System.out.println("Failure: " + ar.cause().getMessage());
        }
      });
  }

  public void usingConnections01(Vertx vertx, Pool pool) {

    pool
      .getConnection()
      .compose(connection ->
        connection
          .preparedQuery("INSERT INTO Users (first_name,last_name) VALUES (?, ?)")
          .executeBatch(Arrays.asList(
            Tuple.of("Julien", "Viet"),
            Tuple.of("Emad", "Alblueshi")
          ))
          .compose(res -> connection
            // Do something with rows
            .query("SELECT COUNT(*) FROM Users")
            .execute()
            .map(rows -> rows.iterator().next().getInteger(0)))
          // Return the connection to the pool
          .eventually(v -> connection.close())
      ).onSuccess(count -> {
      System.out.println("Insert users, now the number of users is " + count);
    });
  }

  public void usingConnections02(SqlConnection connection) {
    connection
      .prepare("SELECT * FROM users WHERE first_name LIKE ?")
      .compose(pq ->
        pq.query()
          .execute(Tuple.of("Julien"))
          .eventually(v -> pq.close())
      ).onSuccess(rows -> {
      // All rows
    });
  }

  public void usingConnections03(Pool pool) {
    pool.withConnection(connection ->
      connection
        .preparedQuery("INSERT INTO Users (first_name,last_name) VALUES (?, ?)")
        .executeBatch(Arrays.asList(
          Tuple.of("Julien", "Viet"),
          Tuple.of("Emad", "Alblueshi")
        ))
        .compose(res -> connection
          // Do something with rows
          .query("SELECT COUNT(*) FROM Users")
          .execute()
          .map(rows -> rows.iterator().next().getInteger(0)))
    ).onSuccess(count -> {
      System.out.println("Insert users, now the number of users is " + count);
    });
  }

  public void transaction01(Pool pool) {
    pool.getConnection()
      // Transaction must use a connection
      .onSuccess(conn -> {
        // Begin the transaction
        conn.begin()
          .compose(tx -> conn
            // Various statements
            .query("INSERT INTO Users (first_name,last_name) VALUES ('Julien','Viet')")
            .execute()
            .compose(res2 -> conn
              .query("INSERT INTO Users (first_name,last_name) VALUES ('Emad','Alblueshi')")
              .execute())
            // Commit the transaction
            .compose(res3 -> tx.commit()))
          // Return the connection to the pool
          .eventually(v -> conn.close())
          .onSuccess(v -> System.out.println("Transaction succeeded"))
          .onFailure(err -> System.out.println("Transaction failed: " + err.getMessage()));
      });
  }

  public void transaction02(Transaction tx) {
    tx.completion()
      .onFailure(err -> {
        System.out.println("Transaction failed => rolled back");
      });
  }

  public void transaction03(Pool pool) {
    // Acquire a transaction and begin the transaction
    pool.withTransaction(client -> client
      .query("INSERT INTO Users (first_name,last_name) VALUES ('Julien','Viet')")
      .execute()
      .flatMap(res -> client
        .query("INSERT INTO Users (first_name,last_name) VALUES ('Emad','Alblueshi')")
        .execute()
        // Map to a message result
        .map("Users inserted")))
      .onSuccess(v -> System.out.println("Transaction succeeded"))
      .onFailure(err -> System.out.println("Transaction failed: " + err.getMessage()));
  }

  public void usingCursors01(SqlConnection connection) {
    connection.prepare("SELECT * FROM users WHERE age > ?", ar1 -> {
      if (ar1.succeeded()) {
        PreparedStatement pq = ar1.result();

        // Create a cursor
        Cursor cursor = pq.cursor(Tuple.of(18));

        // Read 50 rows
        cursor.read(50, ar2 -> {
          if (ar2.succeeded()) {
            RowSet<Row> rows = ar2.result();

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
        PreparedStatement pq = ar1.result();

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

  public void tracing01(MySQLConnectOptions options) {
    options.setTracingPolicy(TracingPolicy.ALWAYS);
  }

  public void customDataTypeCodecExample01(SqlConnection connection) {
    // usecase01-byte as boolean
    DataTypeCodec<Boolean, Boolean> tinyintCodec = new DataTypeCodec<Boolean, Boolean>() {

      private final DataType<Boolean, Boolean> tinyintDatatype = new DataType<Boolean, Boolean>() {
        @Override
        public int identifier() {
          return MySQLType.TINYINT.identifier();
        }

        @Override
        public JDBCType jdbcType() {
          return JDBCType.TINYINT;
        }

        @Override
        public Class<Boolean> encodingJavaClass() {
          return Boolean.class;
        }

        @Override
        public Class<Boolean> decodingJavaClass() {
          return Boolean.class;
        }
      };

      @Override
      public DataType<Boolean, Boolean> dataType() {
        return tinyintDatatype;
      }

      @Override
      public void encode(ByteBuf buffer, Boolean value) {
        buffer.writeBoolean(value);
      }

      @Override
      public Boolean binaryDecode(ByteBuf buffer, int readerIndex, long length, Charset charset) {
        return buffer.readBoolean();
      }

      @Override
      public Boolean textualDecode(ByteBuf buffer, int readerIndex, long length, Charset charset) {
        byte b = (byte) CommonCodec.decodeDecStringToLong(readerIndex, (int) length, buffer);
        return b != 0;
      }
    };

    DataTypeCodecRegistry dataTypeCodecRegistry = connection.dataTypeCodecRegistry();
    dataTypeCodecRegistry.unregister(MySQLDataTypeDefaultCodecs.TinyIntTypeCodec.INSTANCE);
    dataTypeCodecRegistry.register(tinyintCodec);
  }

  public void customDataTypeCodecExample02(SqlConnection connection) {
    // usecase02-custom JSON https://github.com/eclipse-vertx/vertx-sql-client/issues/862
    DataTypeCodec<CustomJSON, CustomJSON> jsonCustomTypeCodec = new DataTypeCodec<CustomJSON, CustomJSON>() {

      private final DataType<CustomJSON, CustomJSON> customJSONDatatype = new DataType<CustomJSON, CustomJSON>() {
        @Override
        public int identifier() {
          return MySQLType.JSON.identifier();
        }

        @Override
        public JDBCType jdbcType() {
          return JDBCType.OTHER;
        }

        @Override
        public Class<CustomJSON> encodingJavaClass() {
          return CustomJSON.class;
        }

        @Override
        public Class<CustomJSON> decodingJavaClass() {
          return CustomJSON.class;
        }
      };

      @Override
      public DataType<CustomJSON, CustomJSON> dataType() {
        return customJSONDatatype;
      }

      @Override
      public void encode(ByteBuf buffer, CustomJSON value) {
        // write to bytebuf directly from the custom json
        // ...
      }

      @Override
      public CustomJSON binaryDecode(ByteBuf buffer, int readerIndex, long length, Charset charset) {
        // read from bytebuf directly
        // ...
      }

      @Override
      public CustomJSON textualDecode(ByteBuf buffer, int readerIndex, long length, Charset charset) {
        // read from bytebuf directly
        // ...
      }
    };

    DataTypeCodecRegistry dataTypeCodecRegistry = connection.dataTypeCodecRegistry();
    dataTypeCodecRegistry.unregister(MySQLDataTypeDefaultCodecs.JsonTypeCodec.INSTANCE);
    dataTypeCodecRegistry.register(jsonCustomTypeCodec);
  }

  public void customDataTypeCodecExample03(SqlConnection connection) {
    // usecase03 - row values direct buffer
    DataTypeCodec<ByteBuf, ByteBuf> rowValueRawBufCodec = new DataTypeCodec<ByteBuf, ByteBuf>() {

      private final DataType<ByteBuf, ByteBuf> tinyintDatatype = new DataType<ByteBuf, ByteBuf>() {
        @Override
        public int identifier() {
          return MySQLType.TINYINT.identifier();
        }

        @Override
        public JDBCType jdbcType() {
          return JDBCType.TINYINT;
        }

        @Override
        public Class<ByteBuf> encodingJavaClass() {
          return ByteBuf.class;
        }

        @Override
        public Class<ByteBuf> decodingJavaClass() {
          return ByteBuf.class;
        }
      };

      @Override
      public DataType<ByteBuf, ByteBuf> dataType() {
        return tinyintDatatype;
      }

      @Override
      public void encode(ByteBuf buffer, ByteBuf value) {
        buffer.writeBytes(value);
      }

      @Override
      public ByteBuf binaryDecode(ByteBuf buffer, int readerIndex, long length, Charset charset) {
        return buffer.readRetainedSlice((int) length);
      }

      @Override
      public ByteBuf textualDecode(ByteBuf buffer, int readerIndex, long length, Charset charset) {
        return buffer.readRetainedSlice((int) length);
      }
    };

    DataTypeCodecRegistry dataTypeCodecRegistry = connection.dataTypeCodecRegistry();
    dataTypeCodecRegistry.unregister(MySQLDataTypeDefaultCodecs.TinyIntTypeCodec.INSTANCE);
    dataTypeCodecRegistry.register(rowValueRawBufCodec);
  }
}
