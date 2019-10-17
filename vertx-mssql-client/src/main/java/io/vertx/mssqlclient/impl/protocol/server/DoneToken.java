package io.vertx.mssqlclient.impl.protocol.server;

public final class DoneToken {
  public static final short STATUS_DONE_FINAL = 0x00;
  public static final short STATUS_DONE_MORE = 0x1;
  public static final short STATUS_DONE_ERROR = 0x2;
  public static final short STATUS_DONE_DONE_INXACT = 0x4;
  public static final short STATUS_DONE_COUNT = 0x10;
  public static final short STATUS_DONE_ATTN = 0x20;
  public static final short STATUS_DONE_SRVERROR = 0x100;
}
