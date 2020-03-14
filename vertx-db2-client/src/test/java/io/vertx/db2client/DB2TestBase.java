package io.vertx.db2client;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.db2client.junit.DB2Resource;
import io.vertx.db2client.tck.ClientConfig;
import io.vertx.ext.unit.TestContext;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.tck.Connector;

public abstract class DB2TestBase {

	@ClassRule
	public static DB2Resource rule = DB2Resource.SHARED_INSTANCE;

	protected Vertx vertx;
	protected Connector<SqlConnection> connector;
	protected DB2ConnectOptions options;

	@Before
	public void setUp(TestContext ctx) throws Exception {
		vertx = Vertx.vertx();
		initConnector();
		cleanTestTable(ctx);
	}

	@After
	public void tearDown(TestContext ctx) {
		connector.close();
		vertx.close(ctx.asyncAssertSuccess());
	}

	protected void initConnector() {
		options = rule.options();
		connector = ClientConfig.CONNECT.connect(vertx, options);
	}

	protected void connect(Handler<AsyncResult<SqlConnection>> handler) {
		connector.connect(handler);
	}

	protected void cleanTestTable(TestContext ctx) {
		connect(ctx.asyncAssertSuccess(conn -> {
			conn.query("DELETE FROM mutable", ctx.asyncAssertSuccess(result -> {
				conn.close();
			}));
		}));
	}

}
