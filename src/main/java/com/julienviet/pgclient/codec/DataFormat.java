package com.julienviet.pgclient.codec;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public enum  DataFormat {
  TEXT(0),
  BINARY(1);
  final int id;
  DataFormat(int id) {
    this.id = id;
  }
  public static DataFormat valueOf(int id) {
    return (id == 0) ? DataFormat.TEXT : DataFormat.BINARY;
  }
}
