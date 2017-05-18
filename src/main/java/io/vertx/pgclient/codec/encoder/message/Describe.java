package io.vertx.pgclient.codec.encoder.message;

import io.vertx.pgclient.codec.Message;
import io.vertx.pgclient.codec.decoder.message.ErrorResponse;
import io.vertx.pgclient.codec.decoder.message.NoData;
import io.vertx.pgclient.codec.decoder.message.ParameterDescription;
import io.vertx.pgclient.codec.decoder.message.RowDescription;

import java.util.Objects;

/**
 *
 * <p>
 * The message that using "statement" variant specifies the name of an existing prepared statement.
 *
 * <p>
 * The response is a {@link ParameterDescription} message describing the parameters needed by the statement,
 * followed by a {@link RowDescription} message describing the rows that will be returned when the statement is eventually
 * executed or a {@link NoData} message if the statement will not return rows.
 * {@link ErrorResponse} is issued if there is no such prepared statement.
 *
 * <p>
 * Note that since {@link Bind} has not yet been issued, the formats to be used for returned columns are not yet known to
 * the backend; the format code fields in the {@link RowDescription} message will be zeroes in this case.
 *
 * <p>
 * The message that using "portal" variant specifies the name of an existing portal.
 *
 * <p>
 * The response is a {@link RowDescription} message describing the rows that will be returned by executing the portal;
 * or a {@link NoData} message if the portal does not contain a query that will return rows; or {@link ErrorResponse}
 * if there is no such portal.
 *
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class Describe implements Message {

  private String statement;
  private String portal;

  public String getStatement() {
    return statement;
  }

  public String getPortal() {
    return portal;
  }

  public Describe setStatement(String statement) {
    this.statement = statement;
    return this;
  }

  public Describe setPortal(String portal) {
    this.portal = portal;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Describe describe = (Describe) o;
    return Objects.equals(statement, describe.statement) &&
      Objects.equals(portal, describe.portal);
  }

  @Override
  public int hashCode() {
    return Objects.hash(statement, portal);
  }


  @Override
  public String toString() {
    return "Describe{" +
      "statement='" + statement + '\'' +
      ", portal='" + portal + '\'' +
      '}';
  }
}
