/*
 * Copyright (C) 2018 Julien Viet
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
package io.reactiverse.pgclient.data;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * A Postgresql point.
 */
@DataObject(generateConverter = true)
public class Point {

  public double x, y;

  public Point() {
    this(0, 0);
  }

  public Point(double x, double y) {
    this.x = x;
    this.y = y;
  }

  public Point(JsonObject json) {
    PointConverter.fromJson(json, this);
  }

  public double getX() {
    return x;
  }

  public Point setX(double x) {
    this.x = x;
    return this;
  }

  public double getY() {
    return y;
  }

  public Point setY(double y) {
    this.y = y;
    return this;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Point) {
      Point that = (Point) obj;
      return x == that.x && y == that.y;
    }
    return false;
  }

  @Override
  public String toString() {
    return "Point(" + x + "," + y + ")";
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    PointConverter.toJson(this, json);
    return json;
  }
}
