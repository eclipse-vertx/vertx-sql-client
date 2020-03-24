package io.vertx.db2client;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;

@RunWith(VertxUnitRunner.class)
public class GeneratedKeysTest extends DB2TestBase {

	@Test
	public void testSelectGeneratedKeyPrepared(TestContext ctx) {
		final String msg = "Some data from testSelectGeneratedKeyPrepared";
		connect(ctx.asyncAssertSuccess(conn -> {
			conn.preparedQuery("SELECT * FROM FINAL TABLE ( INSERT INTO Fortune (message) VALUES (?) )")
        .execute(Tuple.of(msg),  ctx.asyncAssertSuccess(rowSet -> {
				ctx.assertEquals(1, rowSet.size());
				Row row = rowSet.iterator().next();
				// Generated ID should be >= 13 because init.sql always adds the first 12 rows
				ctx.assertTrue(row.getInteger(0) >= 13, "Generated row ID should be >= 13 but was: " + row.getInteger(0));
				ctx.assertEquals(msg, row.getString(1));
				conn.close();
			}));
		}));
	}

	@Test
	public void testSelectGeneratedKey(TestContext ctx) {
		final String msg = "Some data from testSelectGeneratedKey";
		connect(ctx.asyncAssertSuccess(conn -> {
			conn.query("SELECT * FROM FINAL TABLE ( INSERT INTO Fortune (message) VALUES ('" + msg +  "') )").execute(
					ctx.asyncAssertSuccess(rowSet -> {
				ctx.assertEquals(1, rowSet.size());
				Row row = rowSet.iterator().next();
				// Generated ID should be >= 13 because init.sql always adds the first 12 rows
				ctx.assertTrue(row.getInteger(0) >= 13, "Generated row ID should be >= 13 but was: " + row.getInteger(0));
				ctx.assertEquals(msg, row.getString(1));
				conn.close();
			}));
		}));
	}

}
