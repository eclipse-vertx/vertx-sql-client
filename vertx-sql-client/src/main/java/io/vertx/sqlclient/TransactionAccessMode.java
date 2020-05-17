package io.vertx.sqlclient;

//TODO codegen
public enum TransactionAccessMode {
  READ_WRITE("READ WRITE"),
  READ_ONLY("READ ONLY");

  private final String literal;

  TransactionAccessMode(String literal) {
    this.literal = literal;
  }

  public String literal() {
    return literal;
  }
}
