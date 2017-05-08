package io.vertx.pgclient.codec.encoder.message;


import io.vertx.pgclient.codec.Message;

public class QueryMessage implements Message {
  final String sql;
  public QueryMessage(String sql) {
    this.sql = sql;
  }

  public String getQuery() {
    return sql;
  }
}
