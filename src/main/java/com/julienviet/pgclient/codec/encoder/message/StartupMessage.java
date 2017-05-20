package com.julienviet.pgclient.codec.encoder.message;

import com.julienviet.pgclient.codec.Message;

import java.util.Objects;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    StartupMessage that = (StartupMessage) o;
    return Objects.equals(username, that.username) &&
      Objects.equals(database, that.database);
  }

  @Override
  public int hashCode() {
    return Objects.hash(username, database);
  }


  @Override
  public String toString() {
    return "StartupMessage{" +
      "username='" + username + '\'' +
      ", database='" + database + '\'' +
      '}';
  }
}
