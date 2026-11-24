/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.tests.sqlclient.tck;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.data.FloatVector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

/**
 * Base class for testing vector data types.
 * <p>
 * Tests run against a table named {@code vertx_test_vector} with columns {@code (id, emb, notes)},
 * where the {@code emb} column holds single precision float vectors. The table must be seeded with
 * a row holding the vector returned by {@link #expectedFloatVector()} and a row holding a
 * {@code NULL} vector, at the ids {@link #FLOAT_VECTOR_ROW_ID} and {@link #NULL_ROW_ID}.
 * <p>
 * The exact DDL is database-specific and defined by the subclasses.
 */
public abstract class VectorDataTypeTestBase {

  /**
   * The id of the row holding the vector returned by {@link #expectedFloatVector()}.
   */
  protected static final int FLOAT_VECTOR_ROW_ID = 1;

  /**
   * The id of the row holding the {@code NULL} vector.
   */
  protected static final int NULL_ROW_ID = 2;

  /**
   * The id used by the encode tests, cleaned up after verification.
   */
  protected static final int ENCODED_ROW_ID = 3;

  protected Vertx vertx;
  protected Connector<SqlConnection> connector;

  protected abstract void initConnector();

  /**
   * Build a statement from the given parts, joining them with the parameter placeholders of
   * the database under test.
   */
  protected abstract String statement(String... parts);

  @Before
  public void setUp(TestContext ctx) throws Exception {
    vertx = Vertx.vertx();
    initConnector();
  }

  @After
  public void tearDown(TestContext ctx) {
    connector.close();
    vertx.close().onComplete(ctx.asyncAssertSuccess());
  }

  /**
   * @return the components of the vector held by the row at {@link #FLOAT_VECTOR_ROW_ID}
   */
  protected float[] expectedFloatVector() {
    return new float[]{0.1f, 0.2f, 0.3f};
  }

  /**
   * Assert the value decoded from the {@code emb} column of a {@code vertx_test_vector} row.
   * <p>
   * The default implementation expects the value to be auto-decoded to a {@link FloatVector};
   * subclasses may override it when the database under test delivers the value in another
   * representation.
   */
  protected void assertDecodedFloatVector(TestContext ctx, Row row, float[] expected) {
    Object value = row.getValue(0);
    ctx.assertTrue(value instanceof FloatVector, "Expected FloatVector but was: " + value);
    ctx.assertTrue(Arrays.equals(expected, ((FloatVector) value).toFloatArray()),
      "Expected " + Arrays.toString(expected) + " but was: " + value);
  }

  @Test
  public void testDecodeFloatVector(TestContext ctx) {
    String sql = "SELECT emb FROM vertx_test_vector WHERE id = " + FLOAT_VECTOR_ROW_ID;
    connector.connect(ctx.asyncAssertSuccess(conn -> {
      // text protocol
      conn.query(sql).execute().onComplete(ctx.asyncAssertSuccess(textResult -> {
        ctx.assertEquals(1, textResult.size());
        assertDecodedFloatVector(ctx, textResult.iterator().next(), expectedFloatVector());
        // binary protocol
        conn.preparedQuery(sql).execute().onComplete(ctx.asyncAssertSuccess(binaryResult -> {
          ctx.assertEquals(1, binaryResult.size());
          assertDecodedFloatVector(ctx, binaryResult.iterator().next(), expectedFloatVector());
          conn.close();
        }));
      }));
    }));
  }

  @Test
  public void testDecodeNullVector(TestContext ctx) {
    connector.connect(ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT emb FROM vertx_test_vector WHERE id = " + NULL_ROW_ID)
        .execute()
        .onComplete(ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ctx.assertNull(row.getValue(0));
          conn.close();
        }));
    }));
  }

  @Test
  public void testEncodeFloatVector(TestContext ctx) {
    float[] inserted = {0.25f, -0.5f, 3.0f};
    connector.connect(ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery(statement("INSERT INTO vertx_test_vector (id, emb, notes) VALUES (", ", ", ", ", ")"))
        .execute(Tuple.of(ENCODED_ROW_ID, FloatVector.create(inserted), "tck"))
        .onComplete(ctx.asyncAssertSuccess(insertResult -> {
          ctx.assertEquals(1, insertResult.rowCount());
          conn.preparedQuery("SELECT emb FROM vertx_test_vector WHERE id = " + ENCODED_ROW_ID)
            .execute()
            .onComplete(ctx.asyncAssertSuccess(result -> {
              ctx.assertEquals(1, result.size());
              assertDecodedFloatVector(ctx, result.iterator().next(), inserted);
              conn.preparedQuery("DELETE FROM vertx_test_vector WHERE id = " + ENCODED_ROW_ID)
                .execute()
                .onComplete(ctx.asyncAssertSuccess(deleteResult -> {
                  conn.close();
                }));
            }));
        }));
    }));
  }

  @Test
  public void testGetFloatVectorByClass(TestContext ctx) {
    connector.connect(ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("SELECT emb FROM vertx_test_vector WHERE id = " + FLOAT_VECTOR_ROW_ID)
        .execute()
        .onComplete(ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          FloatVector vector = row.get(FloatVector.class, 0);
          ctx.assertNotNull(vector, "Expected a FloatVector convertible value");
          ctx.assertTrue(Arrays.equals(expectedFloatVector(), vector.toFloatArray()));
          conn.close();
        }));
    }));
  }
}
