package com.julienviet.kotlin.pgclient

import com.julienviet.pgclient.PgResultSet
import io.vertx.ext.sql.ResultSet

/**
 * A function providing a DSL for building [com.julienviet.pgclient.PgResultSet] objects.
 *
 *
 * @param columnNames 
 * @param next 
 * @param output 
 * @param results 
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [com.julienviet.pgclient.PgResultSet original] using Vert.x codegen.
 */
fun PgResultSet(
  columnNames: Iterable<String>? = null,
  next: io.vertx.ext.sql.ResultSet? = null,
  output: io.vertx.core.json.JsonArray? = null,
  results: Iterable<io.vertx.core.json.JsonArray>? = null): PgResultSet = com.julienviet.pgclient.PgResultSet().apply {

  if (columnNames != null) {
    this.setColumnNames(columnNames.toList())
  }
  if (next != null) {
    this.setNext(next)
  }
  if (output != null) {
    this.setOutput(output)
  }
  if (results != null) {
    this.setResults(results.toList())
  }
}

