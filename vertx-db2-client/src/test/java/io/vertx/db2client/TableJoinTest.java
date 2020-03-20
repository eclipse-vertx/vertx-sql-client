package io.vertx.db2client;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.Row;

/**
 * Tests for table joins which are documented here:
 * https://www.ibm.com/support/knowledgecenter/SSEPEK_10.0.0/intro/src/tpc/db2z_joindatafromtables.html
 */
@RunWith(VertxUnitRunner.class)
public class TableJoinTest extends DB2TestBase {
	
	@Test
	public void testColumnRename(TestContext ctx) {
		connect(ctx.asyncAssertSuccess(conn -> {
			conn.query("SELECT immutable.id AS \"IMM ID\"," +
		                      "immutable.message AS IMM_MSG," +
					          "Fortune.id AS FORT_ID," +
		                      "Fortune.message AS \"FORT ID\" FROM immutable " + 
            		"INNER JOIN Fortune ON (immutable.id + 1) = Fortune.id " + 
					"WHERE immutable.id=1", 
					ctx.asyncAssertSuccess(rowSet -> {
				ctx.assertEquals(1, rowSet.size());
				// TODO This is consistent with how JDBC behaves, but we may want to add an API
				// to retrieve column labels like JDBC has
				ctx.assertEquals(Arrays.asList("ID", "MESSAGE", "ID", "MESSAGE"), rowSet.columnsNames());
				Row row = rowSet.iterator().next();
				ctx.assertEquals(1, row.getInteger(0));
				ctx.assertEquals("fortune: No such file or directory", row.getString(1));
				ctx.assertEquals(2, row.getInteger(2));
				ctx.assertEquals("A computer scientist is someone who fixes things that aren't broken.", row.getString(3));
				conn.close();
			}));
		}));
	}
	
	@Test
	public void testInnerJoin(TestContext ctx) {
		connect(ctx.asyncAssertSuccess(conn -> {
			conn.query("SELECT immutable.id,immutable.message,Fortune.id,Fortune.message FROM immutable " + 
            		"INNER JOIN Fortune ON (immutable.id + 1) = Fortune.id " + 
					"WHERE immutable.id=1", 
					ctx.asyncAssertSuccess(rowSet -> {
				ctx.assertEquals(1, rowSet.size());
				ctx.assertEquals(Arrays.asList("ID", "MESSAGE", "ID", "MESSAGE"), rowSet.columnsNames());
				Row row = rowSet.iterator().next();
				ctx.assertEquals(1, row.getInteger(0));
				ctx.assertEquals("fortune: No such file or directory", row.getString(1));
				ctx.assertEquals(2, row.getInteger(2));
				ctx.assertEquals("A computer scientist is someone who fixes things that aren't broken.", row.getString(3));
				conn.close();
			}));
		}));
	}
	
	@Test
	public void testInnerJoinPrepared(TestContext ctx) {
		testJoin(ctx, "INNER JOIN");
	}
	
	@Test
	public void testLeftOuterJoin(TestContext ctx) {
		testJoin(ctx, "LEFT OUTER JOIN");
	}
	
	@Test
	public void testRightOuterJoin(TestContext ctx) {
		testJoin(ctx, "RIGHT OUTER JOIN");
	}
	
	@Test
	public void testFullOuterJoin(TestContext ctx) {
		testJoin(ctx, "FULL OUTER JOIN");
	}
	
	private void testJoin(TestContext ctx, String joinType) {
		connect(ctx.asyncAssertSuccess(conn -> {
			conn.preparedQuery("SELECT * FROM immutable " + 
            		joinType + " Fortune ON (immutable.id + 1) = Fortune.id " + 
					"WHERE immutable.id=1", 
					ctx.asyncAssertSuccess(rowSet -> {
				ctx.assertEquals(1, rowSet.size());
				ctx.assertEquals(Arrays.asList("ID", "MESSAGE", "ID", "MESSAGE"), rowSet.columnsNames());
				Row row = rowSet.iterator().next();
				ctx.assertEquals(1, row.getInteger(0));
				ctx.assertEquals("fortune: No such file or directory", row.getString(1));
				ctx.assertEquals(2, row.getInteger(2));
				ctx.assertEquals("A computer scientist is someone who fixes things that aren't broken.", row.getString(3));
				conn.close();
			}));
		}));
	}

}
