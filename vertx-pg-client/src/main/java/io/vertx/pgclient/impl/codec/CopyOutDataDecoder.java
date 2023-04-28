package io.vertx.pgclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.core.buffer.Buffer;
import java.util.function.BiConsumer;
import java.util.stream.Collector;

public class CopyOutDataDecoder {

  private final Collector<ByteBuf, Buffer, Buffer> collector;
  private BiConsumer<Buffer, ByteBuf> accumulator;

  private int size;
  private Buffer container;
  private Throwable failure;
  private Buffer result;

  protected CopyOutDataDecoder(Collector<ByteBuf, Buffer, Buffer> collector) {
    this.collector = collector;
    reset();
  }

  public int size() {
    return size;
  }

  public void handleChunk(ByteBuf in) {
    if (failure != null) {
      return;
    }
    if (accumulator == null) {
      try {
        accumulator = collector.accumulator();
      } catch (Exception e) {
        failure = e;
        return;
      }
    }
    try {
      accumulator.accept(container, in);
    } catch (Exception e) {
      failure = e;
      return;
    }
    size++;
  }

  public Buffer result() {
    return result;
  }

  public Throwable complete() {
    try {
      result = collector.finisher().apply(container);
    } catch (Exception e) {
      failure = e;
    }
    return failure;
  }

  public void reset() {
    size = 0;
    failure = null;
    result = null;
    try {
      this.container = collector.supplier().get();
    } catch (Exception e) {
      failure = e;
    }
  }
}
