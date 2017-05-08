package io.vertx.pgclient.codec.encoder.message;

import io.vertx.pgclient.codec.Message;


public class StartupMessage implements Message {

  final String username;
  final String database;

  public StartupMessage(String username, String database) {
    this.username = username;
    this.database = database;
  }

  public String getUsername() {
    return username;
  }

  public String getDatabase() {
    return database;
  }
}
