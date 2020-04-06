package io.vertx.db2client;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.Tuple;

/**
 * Tests for subqueries which are documented here:
 * https://www.ibm.com/support/knowledgecenter/SSEPEK_10.0.0/intro/src/tpc/db2z_subqueries.html
 */
@RunWith(VertxUnitRunner.class)
public class QueryVariationsTest extends DB2TestBase {

	@Test
	public void testSubquery(TestContext ctx) {
		connect(ctx.asyncAssertSuccess(conn -> {
			conn.query("SELECT id,message FROM immutable " +
					"WHERE message IN " +
					"(SELECT message FROM immutable WHERE id = '4' OR id = '7')").execute(
					ctx.asyncAssertSuccess(rowSet -> {
				ctx.assertEquals(2, rowSet.size());
				ctx.assertEquals(Arrays.asList("ID", "MESSAGE"), rowSet.columnsNames());
				RowIterator<Row> rows = rowSet.iterator();
				ctx.assertTrue(rows.hasNext());
				Row row = rows.next();
				ctx.assertEquals(4, row.getInteger(0));
				ctx.assertEquals("A bad random number generator: 1, 1, 1, 1, 1, 4.33e+67, 1, 1, 1", row.getString(1));
				ctx.assertTrue(rows.hasNext());
				row = rows.next();
				ctx.assertEquals(7, row.getInteger(0));
				ctx.assertEquals("Any program that runs right is obsolete.", row.getString(1));
				conn.close();
			}));
		}));
	}

	@Test
	public void testSubqueryPrepared(TestContext ctx) {
		connect(ctx.asyncAssertSuccess(conn -> {
			conn.preparedQuery("SELECT id,message FROM immutable " +
					"WHERE message IN " +
					"(SELECT message FROM immutable WHERE id = ? OR id = ?)").execute(
					Tuple.of(4, 7),
					ctx.asyncAssertSuccess(rowSet -> {
				ctx.assertEquals(2, rowSet.size());
				ctx.assertEquals(Arrays.asList("ID", "MESSAGE"), rowSet.columnsNames());
				RowIterator<Row> rows = rowSet.iterator();
				ctx.assertTrue(rows.hasNext());
				Row row = rows.next();
				ctx.assertEquals(4, row.getInteger(0));
				ctx.assertEquals("A bad random number generator: 1, 1, 1, 1, 1, 4.33e+67, 1, 1, 1", row.getString(1));
				ctx.assertTrue(rows.hasNext());
				row = rows.next();
				ctx.assertEquals(7, row.getInteger(0));
				ctx.assertEquals("Any program that runs right is obsolete.", row.getString(1));
				conn.close();
			}));
		}));
	}

	@Test
	public void testLikeQuery(TestContext ctx) {
		connect(ctx.asyncAssertSuccess(conn -> {
			conn.query("SELECT id,message FROM immutable " +
					"WHERE message LIKE '%computer%'").execute(
					ctx.asyncAssertSuccess(rowSet -> {
				ctx.assertEquals(2, rowSet.size());
				ctx.assertEquals(Arrays.asList("ID", "MESSAGE"), rowSet.columnsNames());
				RowIterator<Row> rows = rowSet.iterator();
				ctx.assertTrue(rows.hasNext());
				Row row = rows.next();
				ctx.assertEquals(2, row.getInteger(0));
				ctx.assertEquals("A computer scientist is someone who fixes things that aren't broken.", row.getString(1));
				ctx.assertTrue(rows.hasNext());
				row = rows.next();
				ctx.assertEquals(5, row.getInteger(0));
				ctx.assertEquals("A computer program does what you tell it to do, not what you want it to do.", row.getString(1));
				conn.close();
			}));
		}));
	}
	
	/**
	 * Verify that the same connection issuing multiple different prepared statements
	 * has isolated result sections on the DB2 side. If the same section is reused for
	 * both statements, then query 1 might get the results from query 2 or vice versa
	 */
	@Test
	public void testSectionReuse(TestContext ctx) {
	  connect(ctx.asyncAssertSuccess(con -> {
        con.prepare("SELECT * FROM Fortune WHERE id=1")
          .flatMap(ps -> ps.query().execute())
          .onComplete(ctx.asyncAssertSuccess(rowSet -> {
            ctx.assertEquals(1, rowSet.size());
            ctx.assertEquals(Arrays.asList("ID", "MESSAGE"), rowSet.columnsNames());
            RowIterator<Row> rows = rowSet.iterator();
            ctx.assertTrue(rows.hasNext());
            Row row = rows.next();
            ctx.assertEquals(1, row.getInteger(0));
            ctx.assertEquals("fortune: No such file or directory", row.getString(1));
            ctx.assertFalse(rows.hasNext());
          }));
        con.prepare("SELECT * FROM immutable WHERE id=2")
          .flatMap(ps -> ps.query().execute())
          .onComplete(ctx.asyncAssertSuccess(rowSet -> {
            ctx.assertEquals(1, rowSet.size());
            ctx.assertEquals(Arrays.asList("ID", "MESSAGE"), rowSet.columnsNames());
            RowIterator<Row> rows = rowSet.iterator();
            ctx.assertTrue(rows.hasNext());
            Row row = rows.next();
            ctx.assertEquals(2, row.getInteger(0));
            ctx.assertEquals("A computer scientist is someone who fixes things that aren't broken.", row.getString(1));
            ctx.assertFalse(rows.hasNext());
          }));
	  }));
	}

}
