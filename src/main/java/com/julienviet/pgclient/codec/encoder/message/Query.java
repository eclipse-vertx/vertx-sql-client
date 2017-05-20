package com.julienviet.pgclient.codec.encoder.message;
import com.julienviet.pgclient.codec.decoder.message.DataRow;
import com.julienviet.pgclient.codec.decoder.message.NoticeResponse;
import com.julienviet.pgclient.codec.decoder.message.ReadyForQuery;
import com.julienviet.pgclient.codec.Message;
import com.julienviet.pgclient.codec.decoder.message.CommandComplete;
import com.julienviet.pgclient.codec.decoder.message.EmptyQueryResponse;
import com.julienviet.pgclient.codec.decoder.message.ErrorResponse;
import com.julienviet.pgclient.codec.decoder.message.RowDescription;

import java.util.Objects;

/**
 * <p>
 * This message includes an SQL command (or commands) expressed as a text string.
 *
 * <p>
 * The possible response messages from the backend are
 * {@link CommandComplete}, {@link RowDescription}, {@link DataRow}, {@link EmptyQueryResponse}, {@link ErrorResponse},
 * {@link ReadyForQuery} and {@link NoticeResponse}
 *
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

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
