package io.vertx.clickhouse.clickhousenative.impl.codec;

import io.vertx.sqlclient.impl.ParamDesc;

import java.util.List;

public class ClickhouseNativeParamDesc extends ParamDesc {
  private final List<ClickhouseNativeColumnDescriptor> paramDescr;

  public ClickhouseNativeParamDesc(List<ClickhouseNativeColumnDescriptor> paramDescr) {
    this.paramDescr = paramDescr;
  }
}
