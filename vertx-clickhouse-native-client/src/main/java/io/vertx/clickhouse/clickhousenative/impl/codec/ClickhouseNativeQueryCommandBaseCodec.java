package io.vertx.clickhouse.clickhousenative.impl.codec;

import io.vertx.sqlclient.impl.command.QueryCommandBase;

abstract class ClickhouseNativeQueryCommandBaseCodec <T, C extends QueryCommandBase<T>> extends ClickhouseNativeCommandCodec<Boolean, C>{
  protected ClickhouseNativeQueryCommandBaseCodec(C cmd) {
    super(cmd);
  }
}
