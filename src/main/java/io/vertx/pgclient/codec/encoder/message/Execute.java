package io.vertx.pgclient.codec.encoder.message;

import io.vertx.pgclient.codec.Message;

import java.util.Objects;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class Execute implements Message {

  private String statement;
  private String portal;
  private int rowLimit;

  public String getStatement() {
    return statement;
  }

  public String getPortal() {
    return portal;
  }

  public int getRowLimit() {
    return rowLimit;
  }

  public Execute setStatement(String statement) {
    this.statement = statement;
    return this;
  }

  public Execute setPortal(String portal) {
    this.portal = portal;
    return this;
  }

  public Execute setRowLimit(int rowLimit) {
    this.rowLimit = rowLimit;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Execute execute = (Execute) o;
    return rowLimit == execute.rowLimit &&
      Objects.equals(statement, execute.statement) &&
      Objects.equals(portal, execute.portal);
  }

  @Override
  public int hashCode() {
    return Objects.hash(statement, portal, rowLimit);
  }


  @Override
  public String toString() {
    return "Execute{" +
      "statement='" + statement + '\'' +
      ", portal='" + portal + '\'' +
      ", rowLimit=" + rowLimit +
      '}';
  }

}
