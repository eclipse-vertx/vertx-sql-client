package io.vertx.pgclient.codec.encoder.message;

import io.vertx.pgclient.codec.Message;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class Bind implements Message {

  private String statement;
  private String portal;
  private final byte[][] paramValues;

  public Bind(byte[][] paramValues) {
    this.paramValues = paramValues;
  }

  public Bind setStatement(String statement) {
    this.statement = statement;
    return this;
  }

  public Bind setPortal(String portal) {
    this.portal = portal;
    return this;
  }

  public String getStatement() {
    return statement;
  }

  public String getPortal() {
    return portal;
  }

  public byte[][] getParamValues() {
    return paramValues;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Bind bind = (Bind) o;
    return Objects.equals(statement, bind.statement) &&
      Objects.equals(portal, bind.portal) &&
      Arrays.equals(paramValues, bind.paramValues);
  }

  @Override
  public int hashCode() {
    return Objects.hash(statement, portal, paramValues);
  }

  @Override
  public String toString() {
    return "Bind{" +
      "statement='" + statement + '\'' +
      ", portal='" + portal + '\'' +
      ", paramValues=" + Arrays.toString(paramValues) +
      '}';
  }

}
