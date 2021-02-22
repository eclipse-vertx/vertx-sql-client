package io.vertx.clickhouse.clickhousenative;

import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeConnectionUriParser;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.SqlConnectOptions;

@DataObject(generateConverter = true)
public class ClickhouseNativeConnectOptions extends SqlConnectOptions {
  public static final int DEFAULT_PIPELINING_LIMIT = 256;

  private int pipeliningLimit = DEFAULT_PIPELINING_LIMIT;

  public static ClickhouseNativeConnectOptions fromUri(String connectionUri) throws IllegalArgumentException {
    JsonObject parsedConfiguration = ClickhouseNativeConnectionUriParser.parse(connectionUri);
    return new ClickhouseNativeConnectOptions(parsedConfiguration);
  }

  public ClickhouseNativeConnectOptions() {
    super();
  }

  public ClickhouseNativeConnectOptions(JsonObject json) {
    super(json);
    ClickhouseNativeConnectOptionsConverter.fromJson(json, this);
  }

  public ClickhouseNativeConnectOptions(SqlConnectOptions other) {
    super(other);
  }

  public ClickhouseNativeConnectOptions(ClickhouseNativeConnectOptions other) {
    super(other);
  }

  public int getPipeliningLimit() {
    return pipeliningLimit;
  }
}
