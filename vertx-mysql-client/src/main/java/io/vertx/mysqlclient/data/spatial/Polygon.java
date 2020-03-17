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
 * A Polygon is a planar Surface representing a multisided geometry. It is defined by a single exterior boundary and zero or more interior boundaries, where each interior boundary defines a hole in the Polygon.
 */
@DataObject(generateConverter = true)
public class Polygon extends Geometry {
  private List<LineString> lineStrings;

  public Polygon() {
  }

  public Polygon(JsonObject json) {
    super(json);
    PolygonConverter.fromJson(json, this);
  }

  public Polygon(Polygon other) {
    super(other);
    this.lineStrings = new ArrayList<>(other.lineStrings);
  }

  public Polygon(long SRID, List<LineString> lineStrings) {
    super(SRID);
    this.lineStrings = lineStrings;
  }

  public Polygon setLineStrings(List<LineString> lineStrings) {
    this.lineStrings = lineStrings;
    return this;
  }

  public List<LineString> getLineStrings() {
    return lineStrings;
  }

  @Override
  public JsonObject toJson() {
    JsonObject json = super.toJson();
    PolygonConverter.toJson(this, json);
    return json;
  }
}
