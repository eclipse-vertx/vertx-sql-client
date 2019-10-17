package io.vertx.mssqlclient.impl.protocol;

public enum MessageType {

  SQL_BATCH(1),
  PRE_TDS7_LOGIN(2),
  RPC(3),
  TABULAR_RESULT(4),
  ATTENTION_SIGNAL(6),
  BULK_LOAD_DATA(7),
  FEDERATED_AUTHENTICATION_TOKEN(8),
  TRANSACTION_MANAGER_REQUEST(14),
  TDS7_LOGIN(16),
  SSPI(17),
  PRE_LOGIN(18);

  private final int value;

  MessageType(int value) {
    this.value = value;
  }

  public static MessageType valueOf(int value) {
    switch (value) {
      case 1:
        return SQL_BATCH;
      case 2:
        return PRE_TDS7_LOGIN;
      case 3:
        return RPC;
      case 4:
        return TABULAR_RESULT;
      case 6:
        return ATTENTION_SIGNAL;
      case 7:
        return BULK_LOAD_DATA;
      case 8:
        return FEDERATED_AUTHENTICATION_TOKEN;
      case 14:
        return TRANSACTION_MANAGER_REQUEST;
      case 16:
        return TDS7_LOGIN;
      case 17:
        return SSPI;
      case 18:
        return PRE_LOGIN;
      default:
        throw new IllegalArgumentException("Unknown message type value");
    }
  }

  public int value() {
    return this.value;
  }
}
