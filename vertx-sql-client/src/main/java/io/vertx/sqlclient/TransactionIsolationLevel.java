package io.vertx.sqlclient;

//TODO codegen
public enum TransactionIsolationLevel {

  READ_UNCOMMITED("READ UNCOMMITTED"),
  READ_COMMITED("READ COMMITTED"),
  REPEATABLE_READ("REPEATABLE READ"),
  SERIALIZABLE("SERIALIZABLE");

  private final String literal;

  TransactionIsolationLevel(String literal) {
    this.literal = literal;
  }

  public String literal() {
    return literal;
  }
}
