package com.julienviet.pgclient.codec.encoder.message;

import com.julienviet.pgclient.codec.Message;
import com.julienviet.pgclient.codec.decoder.message.CloseComplete;
import com.julienviet.pgclient.codec.decoder.message.ErrorResponse;

import java.util.Objects;

/**
 *
 * <p>
 * The message closes an existing prepared statement or portal and releases resources.
 * Note that closing a prepared statement implicitly closes any open portals that were constructed from that statement.
 *
 * <p>
 * The response is either {@link CloseComplete} or {@link ErrorResponse}
 *
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class Close implements Message {

  private String statement;
  private String portal;


  public String getStatement() {
    return statement;
  }

  public String getPortal() {
    return portal;
  }


  public Close setStatement(String statement) {
    this.statement = statement;
    return this;
  }

  public Close setPortal(String portal) {
    this.portal = portal;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Close close = (Close) o;
    return Objects.equals(statement, close.statement) &&
      Objects.equals(portal, close.portal);
  }

  @Override
  public int hashCode() {
    return Objects.hash(statement, portal);
  }


  @Override
  public String toString() {
    return "Close{" +
      "statement='" + statement + '\'' +
      ", portal='" + portal + '\'' +
      '}';
  }
}
