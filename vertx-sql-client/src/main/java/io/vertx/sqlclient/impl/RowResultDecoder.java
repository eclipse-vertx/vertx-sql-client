package io.vertx.sqlclient.impl;

import io.netty.buffer.ByteBuf;
import io.vertx.sqlclient.Row;

import java.util.function.BiConsumer;
import java.util.stream.Collector;

public abstract class RowResultDecoder<C, R> {

  protected final Collector<Row, C, R> collector;
  protected final boolean singleton;
  protected final BiConsumer<C, Row> accumulator;

  protected int size;
  protected C container;
  protected RowInternal row;

  public RowResultDecoder(Collector<Row, C, R> collector, boolean singleton) {
    this.collector = collector;
    this.singleton = singleton;
    this.accumulator = collector.accumulator();
  }

  public abstract void decodeRow(int len, ByteBuf in);

  public int size() {
    return size;
  }

  public R complete() {
    if (container == null) {
      container = collector.supplier().get();
    }
    return collector.finisher().apply(container);
  }

  public void reset() {
    container = null;
    size = 0;
  }

}
