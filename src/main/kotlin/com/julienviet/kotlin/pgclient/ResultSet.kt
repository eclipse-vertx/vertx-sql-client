package com.julienviet.kotlin.pgclient

import com.julienviet.pgclient.ResultSet

fun ResultSet(
  columnNames: Iterable<String>? = null,
  output: io.vertx.core.json.JsonArray? = null,
  results: Iterable<io.vertx.core.json.JsonArray>? = null): ResultSet = com.julienviet.pgclient.ResultSet().apply {

  if (columnNames != null) {
    this.setColumnNames(columnNames.toList())
  }
  if (output != null) {
    this.setOutput(output)
  }
  if (results != null) {
    this.setResults(results.toList())
  }
}

