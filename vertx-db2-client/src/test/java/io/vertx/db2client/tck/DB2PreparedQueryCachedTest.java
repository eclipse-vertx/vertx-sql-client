package io.vertx.db2client.tck;

import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.runner.RunWith;

import io.vertx.db2client.junit.DB2Resource;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.tck.PreparedQueryCachedTestBase;

@Ignore // TODO @AGG get this TCK test passing
@RunWith(VertxUnitRunner.class)
public class DB2PreparedQueryCachedTest extends PreparedQueryCachedTestBase {
  @ClassRule
  public static DB2Resource rule = DB2Resource.SHARED_INSTANCE;

  @Override
  protected void initConnector() {
    options = rule.options();
    connector = ClientConfig.CONNECT.connect(vertx, options);
  }

  @Override
  protected String statement(String... parts) {
    return String.join("?", parts);
  }

}
