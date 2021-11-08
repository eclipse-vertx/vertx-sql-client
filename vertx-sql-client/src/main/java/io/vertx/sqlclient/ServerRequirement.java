package io.vertx.sqlclient;

public enum ServerRequirement {
  ANY,
  PRIMARY,
  REPLICA,
  PREFER_REPLICA;
}
