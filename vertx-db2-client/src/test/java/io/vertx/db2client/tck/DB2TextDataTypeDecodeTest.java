package io.vertx.db2client.tck;

import java.math.BigDecimal;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.db2client.junit.DB2Resource;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.tck.TextDataTypeDecodeTestBase;

@RunWith(VertxUnitRunner.class)
public class DB2TextDataTypeDecodeTest extends TextDataTypeDecodeTestBase {
    @ClassRule
    public static DB2Resource rule = DB2Resource.SHARED_INSTANCE;

    @Override
    protected void initConnector() {
        connector = ClientConfig.CONNECT.connect(vertx, rule.options());
    }

    @Test
    public void testDecimal(TestContext ctx) {
        // In DB2 NUMERIC type == DECIMAL type so always return BigDecimal
        testDecodeGeneric(ctx, "test_decimal", BigDecimal.class, BigDecimal.valueOf(12345));
    }

    @Test
    public void testNumeric(TestContext ctx) {
        // In DB2 NUMERIC type == DECIMAL type so always return BigDecimal
        testDecodeGeneric(ctx, "test_numeric", BigDecimal.class, new BigDecimal("999.99"));
    }
    
}
