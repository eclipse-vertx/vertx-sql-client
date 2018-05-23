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

package io.reactiverse.pgclient.impl.codec.decoder.message;


import io.reactiverse.pgclient.impl.codec.decoder.InboundMessage;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class CommandComplete implements InboundMessage {

  private final int updated;

  public CommandComplete(/*String command, */int updated) {
    // this.command = command;
    this.updated = updated;
  }

  /*
  public String getCommand() {
    return command;
  }
  */

  public int updated() {
    return updated;
  }

  @Override
  public String toString() {
    return "CommandComplete{" +
      // "command='" + command + '\'' +
      "updated=" + updated +
      '}';
  }

}
