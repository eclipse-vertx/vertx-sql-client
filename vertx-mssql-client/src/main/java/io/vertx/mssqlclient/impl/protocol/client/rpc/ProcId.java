package io.vertx.mssqlclient.impl.protocol.client.rpc;

/**
 * The number identifying the special stored procedure to be executed.
 */
public final class ProcId {
  public static final int Sp_Cursor = 1;
  public static final int Sp_CursorOpen = 2;
  public static final int Sp_cursorPrepare = 3;
  public static final int Sp_CursorExecute = 4;
  public static final int Sp_CursorPrepExec = 5;
  public static final int Sp_CursorUnprepare = 6;
  public static final int Sp_CursorFetch = 7;
  public static final int Sp_CursorOption = 8;
  public static final int Sp_CursorClose = 9;
  public static final int Sp_ExecuteSql = 10;
  public static final int Sp_Prepare = 11;
  public static final int Sp_Execute = 12;
  public static final int Sp_PrepExec = 13;
  public static final int Sp_PrepExecRpc = 14;
  public static final int Sp_Unprepare = 15;
}
