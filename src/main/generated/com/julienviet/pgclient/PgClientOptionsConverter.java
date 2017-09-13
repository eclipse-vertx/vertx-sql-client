/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.julienviet.pgclient;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;

/**
 * Converter for {@link com.julienviet.pgclient.PgClientOptions}.
 *
 * NOTE: This class has been automatically generated from the {@link com.julienviet.pgclient.PgClientOptions} original class using Vert.x codegen.
 */
public class PgClientOptionsConverter {

  public static void fromJson(JsonObject json, PgClientOptions obj) {
    if (json.getValue("cachePreparedStatements") instanceof Boolean) {
      obj.setCachePreparedStatements((Boolean)json.getValue("cachePreparedStatements"));
    }
    if (json.getValue("database") instanceof String) {
      obj.setDatabase((String)json.getValue("database"));
    }
    if (json.getValue("host") instanceof String) {
      obj.setHost((String)json.getValue("host"));
    }
    if (json.getValue("password") instanceof String) {
      obj.setPassword((String)json.getValue("password"));
    }
    if (json.getValue("pipeliningLimit") instanceof Number) {
      obj.setPipeliningLimit(((Number)json.getValue("pipeliningLimit")).intValue());
    }
    if (json.getValue("port") instanceof Number) {
      obj.setPort(((Number)json.getValue("port")).intValue());
    }
    if (json.getValue("username") instanceof String) {
      obj.setUsername((String)json.getValue("username"));
    }
  }

  public static void toJson(PgClientOptions obj, JsonObject json) {
    json.put("cachePreparedStatements", obj.getCachePreparedStatements());
    if (obj.getDatabase() != null) {
      json.put("database", obj.getDatabase());
    }
    if (obj.getHost() != null) {
      json.put("host", obj.getHost());
    }
    if (obj.getPassword() != null) {
      json.put("password", obj.getPassword());
    }
    json.put("pipeliningLimit", obj.getPipeliningLimit());
    json.put("port", obj.getPort());
    if (obj.getUsername() != null) {
      json.put("username", obj.getUsername());
    }
  }
}