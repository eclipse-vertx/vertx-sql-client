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

package com.julienviet.pgclient.codec.decoder.message;


import com.julienviet.pgclient.codec.decoder.InboundMessage;
import io.vertx.core.json.JsonArray;

import java.util.List;
import java.util.Objects;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class CommandComplete implements InboundMessage {

  private final String command;
  private final int rowsAffected;

  public CommandComplete(String command, int rowsAffected) {
    this.command = command;
    this.rowsAffected = rowsAffected;
  }

  public String getCommand() {
    return command;
  }

  public int getRowsAffected() {
    return rowsAffected;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CommandComplete that = (CommandComplete) o;
    return Objects.equals(command, that.command) &&
      Objects.equals(rowsAffected, that.rowsAffected);
  }

  @Override
  public int hashCode() {
    return Objects.hash(command, rowsAffected);
  }

  @Override
  public String toString() {
    return "CommandComplete{" +
      "command='" + command + '\'' +
      ", rowsAffected=" + rowsAffected +
      '}';
  }

}
