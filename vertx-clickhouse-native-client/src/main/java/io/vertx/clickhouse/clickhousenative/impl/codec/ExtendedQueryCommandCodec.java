package io.vertx.clickhouse.clickhousenative.impl.codec;

import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeSocketConnection;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.sqlclient.impl.command.ExtendedQueryCommand;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static io.vertx.clickhouse.clickhousenative.ClickhouseConstants.OPTION_MAX_BLOCK_SIZE;

public class ExtendedQueryCommandCodec<T> extends SimpleQueryCommandCodec<T> {
  private static final Logger LOG = LoggerFactory.getLogger(SimpleQueryCommandCodec.class);

  public ExtendedQueryCommandCodec(ExtendedQueryCommand<T> cmd, ClickhouseNativeSocketConnection conn) {
    super(cmd, conn, true);
  }

  @Override
  void encode(ClickhouseNativeEncoder encoder) {
    String ourCursorId = ecmd().cursorId();
    if (conn.getPendingCursorId() == null) {
      conn.setPendingCursorId(ourCursorId);
    } else {
      conn.throwExceptionIfBusy(ourCursorId);
    }
    super.encode(encoder);
  }

  @Override
  protected Map<String, String> settings() {
    String fetchSize = Integer.toString(ecmd().fetch());
    Map<String, String> defaultSettings = super.settings();
    String defaultFetchSize = defaultSettings.get(OPTION_MAX_BLOCK_SIZE);
    if (!Objects.equals(defaultFetchSize, fetchSize)) {
      if (LOG.isWarnEnabled() && defaultFetchSize != null) {
        LOG.warn("overriding " + OPTION_MAX_BLOCK_SIZE + " option with new value " + fetchSize + ", was " + defaultSettings.get(OPTION_MAX_BLOCK_SIZE));
      }
      defaultSettings = new HashMap<>(defaultSettings);
      defaultSettings.put(OPTION_MAX_BLOCK_SIZE, fetchSize);
    }
    return defaultSettings;
  }

  @Override
  protected void checkIfBusy() {
    conn.throwExceptionIfBusy(ecmd().cursorId());
  }

  @Override
  protected boolean isSuspended() {
    return ecmd().isSuspended();
  }

  private ExtendedQueryCommand<T> ecmd() {
    return (ExtendedQueryCommand<T>)cmd;
  }
}
