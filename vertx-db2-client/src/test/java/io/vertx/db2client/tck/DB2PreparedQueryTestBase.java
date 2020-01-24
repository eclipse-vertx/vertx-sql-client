package io.vertx.db2client.tck;

import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;

import io.vertx.db2client.junit.DB2Resource;
import io.vertx.ext.unit.TestContext;
import io.vertx.sqlclient.tck.PreparedQueryTestBase;

public abstract class DB2PreparedQueryTestBase extends PreparedQueryTestBase {
	
	@ClassRule
	public static DB2Resource rule = DB2Resource.SHARED_INSTANCE;
	
	@Override
	protected void cleanTestTable(TestContext ctx) {
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
	
	@Override
	protected boolean cursorRequiresTx() {
	    return false;
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
}
