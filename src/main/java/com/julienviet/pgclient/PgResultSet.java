package com.julienviet.pgclient;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;

import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@DataObject
public class PgResultSet extends ResultSet {

  private boolean complete;

  public PgResultSet() {
  }

  public PgResultSet(ResultSet other) {
    super(other);
  }

  public PgResultSet(List<String> columnNames, List<JsonArray> results, ResultSet next) {
    super(columnNames, results, next);
  }

  public PgResultSet(JsonObject json) {
    super(json);
  }

  @Override
  public PgResultSet setResults(List<JsonArray> results) {
    return (PgResultSet) super.setResults(results);
  }

  @Override
  public PgResultSet setOutput(JsonArray output) {
    return (PgResultSet) super.setOutput(output);
  }

  @Override
  public PgResultSet setColumnNames(List<String> columnNames) {
    return (PgResultSet) super.setColumnNames(columnNames);
  }

  @Override
  public PgResultSet setNext(ResultSet next) {
    return (PgResultSet) super.setNext(next);
  }

  public boolean isComplete() {
    return complete;
  }

  public PgResultSet setComplete(boolean complete) {
    this.complete = complete;
    return this;
  }
}
