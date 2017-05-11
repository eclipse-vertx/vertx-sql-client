package io.vertx.pgclient.codec.decoder.message;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public enum ColumnFormat {
  TEXT((short)0),
  BINARY((short)1),
  UNKNOWN((short)-1);
  final short id;
  ColumnFormat(short id) {
    this.id = id;
  }
  public static ColumnFormat get(short id) {
    for (ColumnFormat type : values()) {
      if (type.id == id) {
        return type;
      }
    }
    return ColumnFormat.UNKNOWN;
  }
}