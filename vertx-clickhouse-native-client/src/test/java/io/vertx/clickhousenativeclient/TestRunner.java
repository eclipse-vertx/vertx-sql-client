/*
 *
 *  Copyright (c) 2021 Vladimir Vishnevskii
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Eclipse Public License 2.0 which is available at
 *  http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 *  which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *  SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package io.vertx.clickhousenativeclient;

import com.fasterxml.jackson.core.Base64Variants;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.vertx.clickhouse.clickhousenative.ClickhouseConstants;
import io.vertx.clickhouse.clickhousenative.ClickhouseNativeConnectOptions;
import io.vertx.clickhouse.clickhousenative.ClickhouseNativeConnection;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.columns.ClickhouseColumns;
import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.sqlclient.*;
import io.vertx.sqlclient.impl.ArrayTuple;
import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestRunner {
  private static final Logger LOG = LoggerFactory.getLogger(TestRunner.class);


  public static void main(String[] args) throws Throwable {
    //deserializeBlock();
    //System.exit(0);

    //testCompression();
    //System.exit(1);

    //ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);
    ClickhouseNativeConnectOptions opts = new ClickhouseNativeConnectOptions(
      new SqlConnectOptions()
        .setPort(9000)
        .setHost("localhost")
        .setUser("default")
        .setPassword("default")
        .setDatabase("default")
        .addProperty(ClickhouseConstants.OPTION_APPLICATION_NAME, "jython-driver")
        //.addProperty(ClickhouseConstants.OPTION_COMPRESSOR, "lz4_safe")
        .addProperty(ClickhouseConstants.OPTION_INITIAL_HOSTNAME, "bhorse")
        .addProperty(ClickhouseConstants.OPTION_SEND_LOGS_LEVEL, "debug")
        .addProperty(ClickhouseConstants.OPTION_APPLICATION_NAME, "jython-driver")
        .addProperty(ClickhouseConstants.OPTION_DEFAULT_ZONE_ID, "Europe/Oslo")
    );
    Vertx vertx = Vertx.vertx();
    ClickhouseNativeConnection.connect(vertx, opts, ar0 -> {
        LOG.info("conn succeeded: " + ar0.succeeded(), ar0.cause());
        if (ar0.succeeded()) {
          selectTest(ar0.result(), vertx);
        }
      });
    //Thread.sleep(25 * 1000);
    //vertx.close(r -> LOG.info("vertx closed"));
  }

  private static void deserializeBlock() throws IOException {
    String filename = "/tmp/forged_nested.bin";
    try (InputStream os = new FileInputStream(filename)) {
      byte[] buf = new byte[(int) new File(filename).length()];
      System.err.println("len: " + buf.length);
      int nRead = os.read(buf);
      System.err.println(nRead);
      System.err.println("serialized:");
      System.err.println(Base64Variants.MIME.encode(buf).replace("\\n", "\n"));
    }
  }

  private static void serializeBlock() throws IOException {
    String src1 = "AlEAAAANREI6OkV4Y2VwdGlvbjdEQjo6RXhjZXB0aW9uOiBEYXRhYmFzZSBgaW52YWxpZERhdGFi\n" +
      "  YXNlYCBkb2Vzbid0IGV4aXN0pwQwLiBEQjo6VENQSGFuZGxlcjo6cnVuSW1wbCgpIEAgMHhmOGJi\n" +
      "  NmRmIGluIC91c3IvYmluL2NsaWNraG91c2UKMS4gREI6OlRDUEhhbmRsZXI6OnJ1bigpIEAgMHhm\n" +
      "  OGNiZWM5IGluIC91c3IvYmluL2NsaWNraG91c2UKMi4gUG9jbzo6TmV0OjpUQ1BTZXJ2ZXJDb25u\n" +
      "  ZWN0aW9uOjpzdGFydCgpIEAgMHgxMWY4MDFhZiBpbiAvdXNyL2Jpbi9jbGlja2hvdXNlCjMuIFBv\n" +
      "  Y286Ok5ldDo6VENQU2VydmVyRGlzcGF0Y2hlcjo6cnVuKCkgQCAweDExZjgxYmMxIGluIC91c3Iv\n" +
      "  YmluL2NsaWNraG91c2UKNC4gUG9jbzo6UG9vbGVkVGhyZWFkOjpydW4oKSBAIDB4MTIwYjgyZTkg\n" +
      "  aW4gL3Vzci9iaW4vY2xpY2tob3VzZQo1LiBQb2NvOjpUaHJlYWRJbXBsOjpydW5uYWJsZUVudHJ5\n" +
      "  KHZvaWQqKSBAIDB4MTIwYjQxNGEgaW4gL3Vzci9iaW4vY2xpY2tob3VzZQo2LiBzdGFydF90aHJl\n" +
      "  YWQgQCAweDhlYTcgaW4gL2xpYi94ODZfNjQtbGludXgtZ251L2xpYnB0aHJlYWQtMi4zMS5zbwo3\n" +
      "  LiBfX2Nsb25lIEAgMHhmZGRlZiBpbiAvbGliL3g4Nl82NC1saW51eC1nbnUvbGliYy0yLjMxLnNv\n" +
      "  CgA=";
    String src = src1.replace(" ", "").replace("\n", "");
    byte[] bytes = Base64Variants.MIME.decode(src1);
    try (OutputStream os = new FileOutputStream("/tmp/forged_nested.bin")) {
      os.write(bytes);
    }
    String encoded = Base64Variants.MIME.encode(bytes);
    System.err.println("src");
    System.err.println(src);
    System.err.println("encoded:");
    System.err.println(encoded);
    System.err.println(encoded.equals(src));
  }

  private static void testCompression() throws IOException {
    int nItems = 256;
    int[] b1 = new int[nItems];
    for (int i = 0; i < nItems; ++i) {
      b1[i] = Integer.MAX_VALUE - 512 + i;
    }

    ByteBuf buf = Unpooled.wrappedBuffer(new byte[b1.length * Integer.BYTES]);
    buf.writerIndex(0);
    Arrays.stream(b1).forEach(buf::writeIntLE);

    compressAndPrintStats(buf.array());
    String s = Arrays.stream(b1).boxed().map(Object::toString).collect(Collectors.joining(","));
    compressAndPrintStats(s.getBytes(StandardCharsets.US_ASCII));
  };

  private static void compressAndPrintStats(byte[] bytes) {
    LZ4Factory factory = LZ4Factory.unsafeInstance();
    LZ4Compressor compr = factory.fastCompressor();
    byte[] compressed = compr.compress(bytes);
    System.err.printf("uncompr: %d, compressed: %d%n", bytes.length, compressed.length);
  }

  private static void insert_test2(ClickhouseNativeConnection conn, Vertx vertx) {
    List<Tuple> batch1 = Arrays.asList(
      Tuple.of(1, (Object) new Object[][][]{ {{"str1_1", "str1_2", null}, {null}}, {{"str1_3", "str1_4", null}, {null}} }),
      Tuple.of(2, (Object) new Object[][][]{ {{"str2_1", "str2_2", null}, {null}} }),
      Tuple.of(3, (Object) new Object[][][]{ {{"str3_1", "str3_2", null}, {null}} })
    );

    List<Tuple> batch2 = Arrays.asList(
      Tuple.of(1, (Object) new Object[] { "str1_1", "str1_2", null }),
      Tuple.of(2, (Object) new Object[] { "str2_1", "str2_2", null, null }),
      Tuple.of(3, (Object) new Object[] { "str3_1", "str3_2", null, null })
    );

    OffsetDateTime dt = OffsetDateTime.of(2019, 1, 1, 0, 1, 1, 1234, ZoneOffset.ofHours(3));
    List<Tuple> batch3 = Arrays.asList(Tuple.of(1, dt));

    List<Tuple> batch4 = Arrays.asList(Tuple.of(1, 0));


    conn.preparedQuery("INSERT INTO vertx_test_string (id, nullable_array3_lc_t) VALUES").executeBatch(batch1, result -> {
      LOG.info("result: " + result.succeeded());
      if (result.failed()) {
        LOG.error("error inserting", result.cause());
      }
      conn.close();
      vertx.close();
    });
  }

  private static void insert_test(ClickhouseNativeConnection conn, Vertx vertx) {
    List<Tuple> batch = Arrays.asList(Tuple.of(1, "a", 1), Tuple.of(null, "b", 2), Tuple.of(3, null, 3), Tuple.of(4, "", 4));
    conn.preparedQuery("INSERT INTO insert_select_testtable (a, b, c) VALUES (1, 'a', 1)").execute(result -> {
      LOG.info("result: " + result.succeeded());
      if (result.failed()) {
        LOG.error("error inserting", result.cause());
      }
      conn.close();
      vertx.close();
    });
  }

  private static void test4(ClickhouseNativeConnection conn, Vertx vertx) {
    List<ClickhouseNativeColumnDescriptor> types = Stream.of("Int8", "Int16")
      .flatMap(el -> Stream.of("Nullable(" + el + ")", "Nullable(U" + el + ")"))
      .map(nm -> ClickhouseColumns.columnDescriptorForSpec(nm, "fake_name"))
      .collect(Collectors.toList());
    List<String> typeNames = types.stream()
      .map(ClickhouseNativeColumnDescriptor::getUnparsedNativeType).collect(Collectors.toList());
    Iterator<ClickhouseNativeColumnDescriptor> typesIter = types.iterator();
    ClickhouseNativeColumnDescriptor type = typesIter.next();
    String query = String.format("SELECT CAST(%s, '%s') as min_val, CAST(%s, '%s') as max_val",
      type.getMinValue(), type.getUnparsedNativeType(), type.getMaxValue(), type.getUnparsedNativeType());
    conn.query(query).execute(
      res -> {
        ClickhouseNativeColumnDescriptor type2 = typesIter.next();
        String query2 = String.format("SELECT CAST(%s, '%s') as min_val, CAST(%s, '%s') as max_val",
          type2.getMinValue(), type2.getUnparsedNativeType(), type2.getMaxValue(), type2.getUnparsedNativeType());
        conn.query(query2).execute(res2 -> {
          conn.close();
          vertx.close();
        });
      }
    );
  }

  private static void test3(ClickhouseNativeConnection conn, Vertx vertx) {
    //String query = "select RESOURCE, MGR_ID from amazon_train limit 55";
    String query = "select name, value from (SELECT name, value from vertx_cl_test_table limit 55) t1 order by name desc";
    AtomicLong l = new AtomicLong(0);
    conn.prepare(query, ar1 -> {
      if (ar1.succeeded()) {
        PreparedStatement pq = ar1.result();
        // Fetch 50 rows at a time
        RowStream<Row> stream = pq.createStream(50, ArrayTuple.EMPTY);
        // Use the stream
        stream.exceptionHandler(err -> {
          System.out.println("Error: " + err.getMessage());
        });
        stream.endHandler(v -> {
          System.out.println("got End of stream");
          vertx.close();
        });
        stream.handler(row -> {
          long val = l.incrementAndGet();
          System.out.println("val: " + val + "; RESOURCE: " + row.getString("name") + "; MGR_ID: " + row.getInteger("value"));
        });
      }
    });
  }

  private static void test2(ClickhouseNativeConnection conn) {
    conn.prepare("select RESOURCE, MGR_ID from amazon_train limit 5", ar1 -> {
      LOG.info("prepare succeeded: " + ar1.succeeded(), ar1.cause());
      if (ar1.succeeded()) {
        PreparedStatement pq = ar1.result();
      }
    });
  }

  private static void selectTest(ClickhouseNativeConnection conn, Vertx vertx) {
    //String query = "SHOW TABLES FROM system LIKE '%user%'"
    //String query = "select RESOURCE, MGR_ID from amazon_train limit 5";
    //String query = "select RESOURCE, MGR_ID, 'aa' as str_col1, CAST('abcdef', 'FixedString(6)') as str_col2 from amazon_train limit 5";
    //String query = "SELECT CAST(4, 'Nullable(UInt64)') AS RESOURCE, 'aa' AS str_col1, CAST('abcdef', 'FixedString(6)') AS str_col2 " +
    //               "UNION ALL " +
    //               "SELECT CAST(255, 'Nullable(UInt64)') AS RESOURCE, 'aa' AS str_col1, CAST(NULL, 'Nullable(FixedString(6))') AS str_col2 ";
    //String query = "SELECT array(array(1,2), array(3,NULL)) AS RESOURCE, 'aa' AS str_col2";
    //String query = "select CAST(array(4), 'Array(LowCardinality(Nullable(UInt8)))')";
    //String query = "select CAST(4, 'LowCardinality(Nullable(UInt8))') AS RESOURCE, 'aa' AS str_col1";
    //String query = "select CAST(array(4), 'Array(LowCardinality(Nullable(UInt8)))') AS RESOURCE";
    //String query = "select name, value from (SELECT name, value from vertx_cl_test_table limit 55) t1 order by name desc";
    //String query = "select CAST('2019-01-01 12:13:14.123456' AS Nullable(DateTime64(6))) AS name, 4 AS value";
    //String query = "select cast('047ae584-b8f3-4d7d-be90-257de2ab35b9', 'UINSERTUID') as name";
    //String query = "select cast(32.2, 'Decimal128(10)') as name";
    //String query = "select CAST('aa', 'Nullable(Enum16(\'aa\' = 1))')"
    //String query = "SELECT tuple(1,NULL) AS x, toTypeName(x) as name";
    //String query = "SELECT CAST('bb', 'Nullable(Enum16(\\'aa\\' = 1,\\'bb\\' = 2))') as name, 'r1' as col_name  UNION ALL " +
    //  " SELECT CAST(NULL, 'Nullable(Enum16(\\'aa\\' = 1,\\'bb\\' = 2))') as name, 'r2' as col_name  UNION ALL " +
    //  " SELECT CAST('aa', 'Nullable(Enum16(\\'aa\\' = 1,\\'bb\\' = 2))') as name, 'r3' as col_name";
    //"Array(Array(LowCardinality(Nullable(String))))"
    //String query = "select CAST('aa', 'LowCardinality(Nullable(String))') as name, 'r1' as col_name UNION ALL " +
    //              " select CAST(NULL, 'LowCardinality(Nullable(String))') as name, 'r2' as col_name UNION ALL " +
    //              " select CAST('bb', 'LowCardinality(Nullable(String))') as name, 'r3' as col_name";
    //String query = "INSERT INTO insert_select_testtable (*) VALUES";
    //String query = "SELECT array(array('A', 'B'), array('C', 'D', 'E', 'F', NULL)) AS name, 'r1' AS col_name";
    //String query = "SELECT array('A', 'B') AS name, 'r1' as col_name";
    //String query = "SELECT array(array(),array(4, NULL)) AS name, 'r1' as col_name";
    //String query = "select 0.9 as name, Inf as v2, NaN as v3, 'r1' as col_name";
    //String query = "SELECT test_char FROM basicdatatype WHERE id = 1";
    //String query = "SELECT test_int_2,test_int_4,test_int_8,test_float_4,test_float_8,test_numeric,test_decimal,test_boolean,test_char,test_varchar,test_date from basicdatatype where id = 1";
    //String query = "SELECT CAST('1999-12-31', 'Date') as test_numeric";
    //SELECT toTypeName(INTERVAL 4 DAY) //interval test
    //select * from system.data_type_families where name like 'Interval%';
    //String query = "select toIPv6('2a02:aa08:ecolValue2000:3100::2') as col1, toIPv4('255.168.21.255') as col2";
    //String query = "select nullable_array3_lc_t from test_insert_string where id=1 limit 1";";
    //String query = "SELECT CAST('[[[\\'1_str1_1\\',\\'str1_2\\',NULL], [NULL]], [[\\'str1_3\\',\\'str1_4\\',NULL],[NULL]] ]', 'Array(Array(Array(Nullable(String))))') UNION ALL " +
    //  "SELECT CAST('[[[\\'2_str2_1\\',\\'str2_2\\',NULL],[NULL]] ]', 'Array(Array(Array(Nullable(String))))') UNION ALL " +
    //  "SELECT CAST('[[[\\'3_str2_1\\',\\'str2_2\\',NULL],[NULL]] ]', 'Array(Array(Array(Nullable(String))))')";
    //String query = "select CAST('2019-01-01 00:01:01.123456789', 'DateTime64(9, \\'Europe/Moscow\\')')";
    //String query = "select simple_t from vertx_test_enum8 order by id";
    //String query = "select * from (select [[[]],[[]]] as a UNION ALL select [[[]],[[]]] as a) t1;";
    String query = "select arrayJoin(x), x from (select groupArray(number) as x from (select * from system.numbers limit 10000) as t) limit 3000";
    long start = System.currentTimeMillis();
    conn.query(query).execute(ret1 -> {
      LOG.info("query succeeded: " + ret1.succeeded());
      if (ret1.succeeded()) {
        RowSet<Row> rSet = ret1.result();
        LOG.info("rowDesc: " + rSet.columnDescriptors() + " row count: " + rSet.rowCount());
        for (Row row : rSet) {
          Object colValue1 = row.get(long[].class, 1);
          //Object colValue2 = row.getValue(1);
          //Object v2 = row.getValue("v2");
          //Object v3 = row.getValue("v3");
          LOG.info("got row: \"" + colValue1 + "\"; tp: " + (colValue1 == null ? null : colValue1.getClass()));
        }
      }
      //81037 if read elements on arrival
      LOG.info("closing vertx, total time: " + (System.currentTimeMillis() - start) + " millis");
      conn.close();
      vertx.close();
    });
  }

  private static void prepareQueryTest(ClickhouseNativeConnection conn, Vertx vertx) {
    //String query = "SELECT test_char FROM basicdatatype WHERE id = 1";
    //String query = "SELECT test_date FROM basicdatatype WHERE id = 1";
    String query = "SELECT test_float_8 FROM basicdatatype WHERE id = 1";
    conn.preparedQuery(query)
      .execute(result -> {
        if (result.failed()) {
          LOG.error("error executing query", result.cause());
          vertx.close();
        }
        Row row = result.result().iterator().next();
        LOG.info("col1: " + row.getValue(0));
        vertx.close();
      });
  }
}
