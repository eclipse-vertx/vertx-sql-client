package io.vertx.pgclient.codec.encoder.message;

import io.vertx.pgclient.codec.Message;
import io.vertx.pgclient.codec.decoder.message.ErrorResponse;
import io.vertx.pgclient.codec.decoder.message.ParseComplete;

import java.util.Objects;

/**
 * <p>
 * The message contains a textual SQL query string.
 *
 * <p>
 * The response is either {@link ParseComplete} or {@link ErrorResponse}
 *
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class Parse implements Message {

  private final String query;
  private String statement;

  public Parse(String query) {
    this.query = query;
  }

  public String getStatement() {
    return statement;
  }

  public String getQuery() {
    return query;
  }


  public Parse setStatement(String statement) {
    this.statement = statement;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Parse parse = (Parse) o;
    return Objects.equals(query, parse.query) &&
      Objects.equals(statement, parse.statement);
  }

  @Override
  public int hashCode() {
    return Objects.hash(query, statement);
  }


  @Override
  public String toString() {
    return "Parse{" +
      "query='" + query + '\'' +
      ", statement='" + statement + '\'' +
      '}';
  }
}
