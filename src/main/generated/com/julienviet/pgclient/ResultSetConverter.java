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
 * Converter for {@link com.julienviet.pgclient.ResultSet}.
 *
 * NOTE: This class has been automatically generated from the {@link com.julienviet.pgclient.ResultSet} original class using Vert.x codegen.
 */
public class ResultSetConverter {

  public static void fromJson(JsonObject json, ResultSet obj) {
    if (json.getValue("columnNames") instanceof JsonArray) {
      java.util.ArrayList<java.lang.String> list = new java.util.ArrayList<>();
      json.getJsonArray("columnNames").forEach( item -> {
        if (item instanceof String)
          list.add((String)item);
      });
      obj.setColumnNames(list);
    }
    if (json.getValue("output") instanceof JsonArray) {
      obj.setOutput(((JsonArray)json.getValue("output")).copy());
    }
    if (json.getValue("results") instanceof JsonArray) {
      java.util.ArrayList<io.vertx.core.json.JsonArray> list = new java.util.ArrayList<>();
      json.getJsonArray("results").forEach( item -> {
        if (item instanceof JsonArray)
          list.add(((JsonArray)item).copy());
      });
      obj.setResults(list);
    }
    if (json.getValue("rows") instanceof JsonArray) {
    }
  }

  public static void toJson(ResultSet obj, JsonObject json) {
    if (obj.getColumnNames() != null) {
      JsonArray array = new JsonArray();
      obj.getColumnNames().forEach(item -> array.add(item));
      json.put("columnNames", array);
    }
    json.put("numColumns", obj.getNumColumns());
    json.put("numRows", obj.getNumRows());
    if (obj.getOutput() != null) {
      json.put("output", obj.getOutput());
    }
    if (obj.getResults() != null) {
      JsonArray array = new JsonArray();
      obj.getResults().forEach(item -> array.add(item));
      json.put("results", array);
    }
    if (obj.getRows() != null) {
      JsonArray array = new JsonArray();
      obj.getRows().forEach(item -> array.add(item));
      json.put("rows", array);
    }
  }
}