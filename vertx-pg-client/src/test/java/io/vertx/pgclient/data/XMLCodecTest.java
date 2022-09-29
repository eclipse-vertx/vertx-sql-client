package io.vertx.pgclient.data;

import io.vertx.ext.unit.TestContext;
import io.vertx.pgclient.PgConnection;
import io.vertx.pgclient.PgException;
import io.vertx.sqlclient.*;
import org.junit.Test;

import java.util.function.BiFunction;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class XMLCodecTest extends DataTypeTestBase {

  @Test
  public void testBinaryEncodePgSQLXMLAsVarcharOrXML(TestContext ctx) {
    String asVarchar = "<message><to><be><validated></validated></be></to></message>";
    String asXML = "<message><to><be><validated><again></again></validated></be></to></message>";

    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("SELECT ($1::xml)::VARCHAR, ($2::xml)").execute(Tuple.of(asVarchar, asXML),
        ctx.asyncAssertSuccess(rows -> {
          ctx.assertEquals(1, rows.size());
          Row row = rows.iterator().next();
          String v1 = row.getString(0);
          String v2 = (String)row.getValue(1);
          ctx.assertEquals("<message><to><be><validated></validated></be></to></message>", v1);
          ctx.assertEquals(asXML, v2);
        })
      );
    }));
  }

  @Test
  public void testBinaryEncodePgSQLXMLMalformed(TestContext ctx) {
    String malformedXml = "<message><to><be><validated><malformed>>></validated></be></to></message>";

    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("SELECT ($1::xml)").execute(Tuple.of(malformedXml),
        ctx.asyncAssertFailure(err -> {
          assertThat(((PgException) err).getCode(), is(equalTo("2200N")));
        })
      );
    }));
  }

  @Test
  public void testTextDecodePgSQLXML(TestContext ctx) {
    testDecodePgSQLXML(ctx, SqlClient::query);
  }

  @Test
  public void testBinaryDecodePgSQLXML(TestContext ctx) {
    testDecodePgSQLXML(ctx, SqlClient::preparedQuery);
  }

  private void testDecodePgSQLXML(TestContext ctx, BiFunction<SqlClient, String, Query<RowSet<Row>>> a) {
    String first = "<message><to><be><validated></validated></be></to></message>";

    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      a.apply(conn, "SELECT '<message><to><be><validated></validated></be></to></message>'::xml")
        .execute(ctx.asyncAssertSuccess(rows -> {
          ctx.assertEquals(1, rows.size());
          Row row = rows.iterator().next();
          String v1 = (String) row.getValue(0);
          ctx.assertEquals(first, v1);
        }));
    }));
  }

  @Test
  public void testBinaryDecodePgSQLXMLArray(TestContext ctx) throws Exception {
    String first = "<message><to><be><validated></validated></be></to></message>";
    String second = "<message><to><be><validated><again></again></validated></be></to></message>";

    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("SELECT ARRAY['<message><to><be><validated></validated></be></to></message>'::xml,'<message><to><be><validated><again></again></validated></be></to></message>'::xml]")
        .execute(ctx.asyncAssertSuccess(rows -> {
          ctx.assertEquals(1, rows.size());
          Row row = rows.iterator().next();
          String[] array = (String[]) row.getValue(0);
          String v1 = array[0];
          String v2 = array[1];
          ctx.assertEquals(first, v1);
          ctx.assertEquals(second, v2);
        }));
    }));
  }

  @Test
  public void testBinaryEncodePgSQLXMLArray(TestContext ctx) {
    String first = "<message><to><be><validated></validated></be></to></message>";
    String second = "<message><to><be><validated><again></again></validated></be></to></message>";


    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("SELECT ($1::xml[])::VARCHAR[]").execute(Tuple.of(
          new String[]{
            "<message><to><be><validated></validated></be></to></message>",
            "<message><to><be><validated><again></again></validated></be></to></message>"}
        ),
        ctx.asyncAssertSuccess(rows -> {
          ctx.assertEquals(1, rows.size());
          Row row = rows.iterator().next();
          String[] array = row.getArrayOfStrings(0);
          String v1 = array[0];
          String v2 = array[1];
          ctx.assertEquals(first, v1);
          ctx.assertEquals(second, v2);
        }));
    }));
  }
}
