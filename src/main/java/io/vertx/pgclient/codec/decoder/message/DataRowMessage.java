package io.vertx.pgclient.codec.decoder.message;

import io.vertx.pgclient.codec.Message;

public class DataRowMessage implements Message {
  final byte[][] values;
  public DataRowMessage(byte[][] values) {
    this.values = values;
  }
  public byte[] getValue(int i) {
    return values[i];
  }
}
