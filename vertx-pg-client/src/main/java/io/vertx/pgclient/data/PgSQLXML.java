package io.vertx.pgclient.data;

import io.vertx.codegen.annotations.Nullable;

import java.util.Objects;

public class PgSQLXML {

  @Nullable final String xmlData;

  public PgSQLXML(String xmlData) {
    this.xmlData = xmlData;
  }

  public String getXmlData() {
    return xmlData;
  }

  @Override
  public String toString() {
    return xmlData;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PgSQLXML pgSQLXML = (PgSQLXML) o;
    return Objects.equals(xmlData, pgSQLXML.xmlData);
  }

  @Override
  public int hashCode() {
    return Objects.hash(xmlData);
  }
}
