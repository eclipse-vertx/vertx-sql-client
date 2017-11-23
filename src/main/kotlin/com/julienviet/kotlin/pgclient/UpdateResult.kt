package com.julienviet.kotlin.pgclient

import com.julienviet.pgclient.UpdateResult

fun UpdateResult(
  keys: io.vertx.core.json.JsonArray? = null,
  updated: Int? = null): UpdateResult = com.julienviet.pgclient.UpdateResult().apply {

  if (keys != null) {
    this.setKeys(keys)
  }
  if (updated != null) {
    this.setUpdated(updated)
  }
}

