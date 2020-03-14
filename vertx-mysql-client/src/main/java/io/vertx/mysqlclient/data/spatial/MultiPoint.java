/*
 * Copyright (c) 2011-2020 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mysqlclient.data.spatial;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A MultiPoint is a geometry collection composed of Point elements. The points are not connected or ordered in any way.
 */
@DataObject(generateConverter = true)
public class MultiPoint extends Geometry {
  private List<Point> points;

  public MultiPoint() {
  }

  public MultiPoint(JsonObject json) {
    super(json);
    MultiPointConverter.fromJson(json, this);
  }

  public MultiPoint(MultiPoint other) {
    super(other);
    this.points = new ArrayList<>(other.points);
  }

  public MultiPoint(long SRID, List<Point> points) {
    super(SRID);
    this.points = points;
  }

  public MultiPoint setPoints(List<Point> points) {
    this.points = points;
    return this;
  }

  public List<Point> getPoints() {
    return points;
  }

  @Override
  public JsonObject toJson() {
    JsonObject json = super.toJson();
    MultiPointConverter.toJson(this, json);
    return json;
  }
}
