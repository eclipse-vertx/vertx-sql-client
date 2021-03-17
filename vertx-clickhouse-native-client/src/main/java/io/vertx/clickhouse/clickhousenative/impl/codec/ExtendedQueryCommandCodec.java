package io.vertx.clickhouse.clickhousenative.impl.codec;

import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeSocketConnection;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.impl.command.ExtendedQueryCommand;

import java.time.temporal.Temporal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static io.vertx.clickhouse.clickhousenative.ClickhouseConstants.OPTION_MAX_BLOCK_SIZE;

public class ExtendedQueryCommandCodec<T> extends SimpleQueryCommandCodec<T> {
  private static final Logger LOG = LoggerFactory.getLogger(SimpleQueryCommandCodec.class);

  public ExtendedQueryCommandCodec(ExtendedQueryCommand<T> cmd, ClickhouseNativeSocketConnection conn) {
    super(cmd, conn, cmd.fetch() > 0);
  }

  @Override
  protected String sql() {
    ExtendedQueryCommand<T> ecmd = ecmd();
    return insertParamValuesIntoQuery(ecmd.sql(), ecmd.params());
  }

  //TODO: maybe switch to antlr4
  private static String insertParamValuesIntoQuery(String parametrizedSql, Tuple paramsList) {
    StringBuilder bldr = new StringBuilder();
    if (paramsList.size() == 0) {
      return parametrizedSql;
    }
    int prevIdx = 0;
    int newIdx;
    while(prevIdx < parametrizedSql.length() && (newIdx = parametrizedSql.indexOf('$', prevIdx)) != -1) {
      if (newIdx - 1 == 0 || parametrizedSql.charAt(newIdx - 1) != '\\') {
        int paramIdxStartPos = newIdx + 1;
        int paramIdxEndPos = paramIdxStartPos;
        while (paramIdxEndPos < parametrizedSql.length() && Character.isDigit(parametrizedSql.charAt(paramIdxEndPos))) {
          ++paramIdxEndPos;
        }
        if (paramIdxStartPos == paramIdxEndPos) {
          throw new IllegalArgumentException("$ without digit at pos " + paramIdxStartPos + " in query " + parametrizedSql);
        }
        int paramIndex = Integer.parseInt(parametrizedSql.substring(paramIdxStartPos, paramIdxEndPos)) - 1;
        Object paramValue = paramsList.getValue(paramIndex);
        bldr.append(parametrizedSql, prevIdx, newIdx);
        Class<?> paramClass = paramValue == null ? null : paramValue.getClass();
        if (paramClass != null) {
          if (CharSequence.class.isAssignableFrom(paramClass) || paramClass == Character.class || Temporal.class.isAssignableFrom(paramClass)) {
            bldr.append('\'').append(paramValue).append('\'');
          } else if (paramClass == Double.class) {
            //DB parser gets mad at 4.9e-322 or smaller. Using cast to cure
            bldr.append(String.format("CAST('%s', 'Float64')", paramValue.toString()));
          } else if (paramClass == Float.class) {
            bldr.append(String.format("CAST('%s', 'Float32')", paramValue.toString()));
          } else {
            bldr.append(paramValue);
          }
        } else {
          bldr.append(paramValue);
        }
        newIdx = paramIdxEndPos;
      }
      prevIdx = newIdx;
    }
    bldr.append(parametrizedSql, prevIdx, parametrizedSql.length());
    return bldr.toString();
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
    if (!"0".equals(fetchSize)) {
      if (!Objects.equals(defaultFetchSize, fetchSize)) {
        if (LOG.isWarnEnabled() && defaultFetchSize != null) {
          LOG.warn("overriding " + OPTION_MAX_BLOCK_SIZE + " option with new value " + fetchSize + ", was " + defaultSettings.get(OPTION_MAX_BLOCK_SIZE));
        }
        defaultSettings = new HashMap<>(defaultSettings);
        defaultSettings.put(OPTION_MAX_BLOCK_SIZE, fetchSize);
      }
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
