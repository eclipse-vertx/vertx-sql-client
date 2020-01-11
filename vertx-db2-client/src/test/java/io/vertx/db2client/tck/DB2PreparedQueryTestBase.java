package io.vertx.db2client.tck;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;

import io.vertx.core.Vertx;
import io.vertx.db2client.junit.DB2Resource;
import io.vertx.ext.unit.TestContext;
import io.vertx.sqlclient.tck.PreparedQueryTestBase;

public abstract class DB2PreparedQueryTestBase extends PreparedQueryTestBase {
	
	@ClassRule
	public static DB2Resource rule = DB2Resource.SHARED_INSTANCE;

	@Before
	public void setUp(TestContext ctx) throws Exception {
		vertx = Vertx.vertx();
		initConnector();
		cleanTestTable(ctx);
	}

	private void cleanTestTable(TestContext ctx) {
		// use DELETE FROM because DB2 does not support TRUNCATE TABLE
		connect(ctx.asyncAssertSuccess(conn -> {
			conn.query("DELETE FROM mutable", ctx.asyncAssertSuccess(result -> {
				conn.close();
			}));
		}));
	}

	@Override
	protected String statement(String... parts) {
		return String.join("?", parts);
	}

	@Test
	@Ignore // TODO: Enable this test after implementing error path handling
	@Override
	public void testPrepareError(TestContext ctx) {
	}

	@Test
	@Ignore // TODO: Enable this test after implementing error path handling
	@Override
	public void testPreparedQueryParamCoercionTypeError(TestContext ctx) {
	}

	@Test
	@Ignore // TODO: Enable this test after implementing error path handling
	@Override
	public void testPreparedQueryParamCoercionQuantityError(TestContext ctx) {
	}

	@Test
	@Ignore // TODO: Enable this test after implementing error path handling
	@Override
	public void testPreparedUpdateWithNullParams(TestContext ctx) {
	}

	// NOTE: for the following cursor tests, the base TCK class issues a BEGIN
	// statement because PostgreSQL requires a transaction for cursors. On DB2
	// this fails (even with JDBC) because there is no END statement to terminate
	// the SQL procedure. Since DB2 gives cursors all the time, when we enable
	// these tests just issue the prepared query and don't begin a procedure

	@Test
	@Ignore // TODO: Enable this test after implementing incremental cursor fetch
	@Override
	public void testQueryCursor(TestContext ctx) {
	}

	@Test
	@Ignore // TODO: Enable this test after implementing incremental cursor fetch
	@Override
	public void testQueryCloseCursor(TestContext ctx) {
	}

	@Test
	@Ignore // TODO: Enable this test after implementing incremental cursor fetch
	@Override
	public void testQueryStreamCloseCursor(TestContext ctx) {
	}

	@Test
	@Ignore // TODO: Enable this test after implementing incremental cursor fetch
	@Override
	public void testStreamQuery(TestContext ctx) {
	}

	@Test
	@Ignore // TODO: Enable this test after implementing incremental cursor fetch
	@Override
	public void testStreamQueryPauseInBatch(TestContext ctx) {
	}

	@Test
	@Ignore // TODO: Enable this test after implementing incremental cursor fetch
	@Override
	public void testStreamQueryPauseInBatchFromAnotherThread(TestContext ctx) {
	}
}
