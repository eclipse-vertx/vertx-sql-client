/*
 * Copyright (C) 2017 Julien Viet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
