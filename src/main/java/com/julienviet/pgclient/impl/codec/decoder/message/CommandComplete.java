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

package com.julienviet.pgclient.impl.codec.decoder.message;


import com.julienviet.pgclient.PgResult;
import com.julienviet.pgclient.impl.codec.decoder.InboundMessage;

import java.util.Objects;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class CommandComplete implements InboundMessage {

  // private final String command;
  private final PgResult<?> result;

  public CommandComplete(/*String command, */PgResult<?> result) {
    // this.command = command;
    this.result = result;
  }

  /*
  public String getCommand() {
    return command;
  }
  */

  public PgResult<?> result() {
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CommandComplete that = (CommandComplete) o;
    return Objects.equals(result, that.result);
  }

  @Override
  public int hashCode() {
    return result == null ? 0 : result.hashCode();
  }

  @Override
  public String toString() {
    return "CommandComplete{" +
      // "command='" + command + '\'' +
      ", result=" + result +
      '}';
  }

}
