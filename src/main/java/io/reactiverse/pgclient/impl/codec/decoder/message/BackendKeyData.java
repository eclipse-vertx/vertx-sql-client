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

import java.util.Objects;

/**
 *
 * <p>
 * This message provides secret-key data that the frontend must save if it wants to be able to issue cancel requests
 * later. The frontend should not respond to this message, but should continue listening
 * for a {@link ReadyForQuery} message.
 *
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 *
 */

public class BackendKeyData implements InboundMessage {

  private final int processId;
  private final int secretKey;

  public BackendKeyData(int processId, int secretKey) {
    this.processId = processId;
    this.secretKey = secretKey;
  }

  public int getProcessId() {
    return processId;
  }

  public int getSecretKey() {
    return secretKey;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BackendKeyData that = (BackendKeyData) o;
    return processId == that.processId &&
      secretKey == that.secretKey;
  }

  @Override
  public int hashCode() {
    return Objects.hash(processId, secretKey);
  }


  @Override
  public String toString() {
    return "BackendKeyData{" +
      "processId=" + processId +
      ", secretKey=" + secretKey +
      '}';
  }
}
