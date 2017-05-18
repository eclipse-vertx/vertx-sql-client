package io.vertx.pgclient.codec.encoder.message;

import io.vertx.pgclient.codec.Message;

import java.util.Objects;

/**
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
