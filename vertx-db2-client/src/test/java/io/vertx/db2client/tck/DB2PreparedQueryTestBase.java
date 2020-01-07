package io.vertx.db2client.tck;

import io.vertx.core.Vertx;
import io.vertx.db2client.junit.DB2Resource;
import io.vertx.ext.unit.TestContext;
import io.vertx.sqlclient.tck.PreparedQueryTestBase;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;

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
    @Ignore
    @Override
    public void testPreparedQueryParamCoercionTypeError(TestContext ctx) {
        // Does not pass, we can't achieve this feature on MySQL for now, see
        // io.vertx.mysqlclient.impl.codec.MySQLParamDesc#prepare for reasons.
        super.testPreparedQueryParamCoercionTypeError(ctx);
    }
}
