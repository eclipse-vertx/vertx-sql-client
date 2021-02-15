package io.vertx.clickhouse.clikhousenative;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.SqlConnectOptions;

@DataObject(generateConverter = true)
public class ClickhouseNativeConnectOptions extends SqlConnectOptions {
  public static final int DEFAULT_PIPELINING_LIMIT = 256;

  private int pipeliningLimit = DEFAULT_PIPELINING_LIMIT;

  public ClickhouseNativeConnectOptions(JsonObject json) {
    super(json);
    ClickhouseNativeConnectOptionsConverter.fromJson(json, this);
  }

  public ClickhouseNativeConnectOptions(SqlConnectOptions other) {
    super(other);
    if (other instanceof ClickhouseNativeConnectOptions) {
      ClickhouseNativeConnectOptions opts = (ClickhouseNativeConnectOptions) other;
    }
  }

  public ClickhouseNativeConnectOptions(ClickhouseNativeConnectOptions other) {
    super(other);
  }

  public int getPipeliningLimit() {
    return pipeliningLimit;
  }
}
