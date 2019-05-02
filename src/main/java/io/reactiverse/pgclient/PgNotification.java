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
package io.reactiverse.pgclient;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * A notification emited by Postgres.
 */
@DataObject(generateConverter = true)
public class PgNotification {

  private int processId;
  private String channel;
  private String payload;

  public PgNotification() {
  }

  public PgNotification(JsonObject json) {
    PgNotificationConverter.fromJson(json, this);
  }

  /**
   * @return the notification process id
   */
  public int getProcessId() {
    return processId;
  }

  /**
   * Set the process id.
   *
   * @return a reference to this, so the API can be used fluently
   */
  public PgNotification setProcessId(int processId) {
    this.processId = processId;
    return this;
  }

  /**
   * @return the notification channel value
   */
  public String getChannel() {
    return channel;
  }

  /**
   * Set the channel value.
   *
   * @return a reference to this, so the API can be used fluently
   */
  public PgNotification setChannel(String channel) {
    this.channel = channel;
    return this;
  }

  /**
   * @return the notification payload value
   */
  public String getPayload() {
    return payload;
  }

  /**
   * Set the payload value.
   *
   * @return a reference to this, so the API can be used fluently
   */
  public PgNotification setPayload(String payload) {
    this.payload = payload;
    return this;
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    PgNotificationConverter.toJson(this, json);
    return json;
  }
}
