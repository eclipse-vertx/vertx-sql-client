package io.vertx.pgclient.codec.encoder.message;

import io.vertx.pgclient.codec.Message;

import java.util.Objects;

/**
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
