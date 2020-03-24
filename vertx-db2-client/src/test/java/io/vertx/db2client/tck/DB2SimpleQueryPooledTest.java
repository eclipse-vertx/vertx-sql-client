package io.vertx.db2client.tck;

import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.db2client.junit.DB2Resource;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.tck.SimpleQueryTestBase;

@RunWith(VertxUnitRunner.class)
public class DB2SimpleQueryPooledTest extends SimpleQueryTestBase {
    @ClassRule
    public static DB2Resource rule = DB2Resource.SHARED_INSTANCE;

    @Override
    protected void initConnector() {
        connector = ClientConfig.POOLED.connect(vertx, rule.options());
    }

    @Override
    protected void cleanTestTable(TestContext ctx) {
        connect(ctx.asyncAssertSuccess(conn -> {
            conn.query("DELETE FROM mutable").execute(ctx.asyncAssertSuccess(result -> {
                conn.close();
            }));
        }));
    }

    @Override
    @Test
    @Ignore // TODO implement error path handling properly
    public void testQueryError(TestContext ctx) {
        super.testQueryError(ctx);
    }
}
