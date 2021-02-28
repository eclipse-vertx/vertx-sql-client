package io.vertx.clickhouse.clickhousenative.impl.codec;

import java.util.HashMap;
import java.util.Map;

public enum ServerPacketType {
  //Name, version, revision.
  HELLO(0),

  //A block of data (compressed or not).
  DATA(1),

  //The exception during query execution.
  EXCEPTION(2),

  //Query execution progress: rows read, bytes read.
  PROGRESS( 3),

  //Ping response
  PONG(4),

  //All packets were transmitted
  END_OF_STREAM(5),

  //Packet with profiling info.
  PROFILE_INFO(6),

  //A block with totals (compressed or not).
  TOTALS(7),

  //A block with minimums and maximums (compressed or not).
  EXTREMES(8),

  //A response to TablesStatus request.
  TABLES_STATUS_RESPONSE(9),

  //System logs of the query execution
  LOG(10),

  //Columns' description for default values calculation
  TABLE_COLUMNS(11);

  private static final Map<Integer, ServerPacketType> CODE_INDEX = buildCodeIndex();

  private final int code;
  ServerPacketType(int code) {
    this.code = code;
  }

  public int code() {
    return code;
  }

  @Override
  public String toString() {
    return this.name() + "(" + this.code + ")";
  }

  public static ServerPacketType fromCode(int code) {
    ServerPacketType ret = CODE_INDEX.get(code);
    if (ret == null) {
      throw new IllegalArgumentException("unknown code: " + code + "(" + Integer.toHexString(code) + ")");
    }
    return ret;
  }

  private static Map<Integer, ServerPacketType> buildCodeIndex() {
    Map<Integer, ServerPacketType> ret = new HashMap<>();
    for (ServerPacketType pType : ServerPacketType.values()) {
      ret.put(pType.code(), pType);
    }
    return ret;
  }
}
