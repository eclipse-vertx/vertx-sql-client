package io.vertx.pgclient.codec.encoder.message;


/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

import io.vertx.pgclient.codec.Message;

import java.util.Objects;

public class Query implements Message {

  final String sql;

  public Query(String sql) {
    this.sql = sql;
  }

  public String getQuery() {
    return sql;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Query that = (Query) o;
    return Objects.equals(sql, that.sql);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sql);
  }


  @Override
  public String toString() {
    return "Query{" +
      "sql='" + sql + '\'' +
      '}';
  }
}
