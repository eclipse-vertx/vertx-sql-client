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

package io.reactiverse.pgclient.impl.codec.decoder;

import java.util.Objects;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class NotificationResponse {

  private final int processId;
  private final String channel;
  private final String payload;

  public NotificationResponse(int processId, String channel, String payload) {
    this.processId = processId;
    this.channel = channel;
    this.payload = payload;
  }

  public int getProcessId() {
    return processId;
  }

  public String getChannel() {
    return channel;
  }

  public String getPayload() {
    return payload;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    NotificationResponse that = (NotificationResponse) o;
    return processId == that.processId &&
      Objects.equals(channel, that.channel) &&
      Objects.equals(payload, that.payload);
  }

  @Override
  public int hashCode() {
    return Objects.hash(processId, channel, payload);
  }

  @Override
  public String toString() {
    return "NotificationResponse{" +
      "processId=" + processId +
      ", channel='" + channel + '\'' +
      ", payload='" + payload + '\'' +
      '}';
  }
}
