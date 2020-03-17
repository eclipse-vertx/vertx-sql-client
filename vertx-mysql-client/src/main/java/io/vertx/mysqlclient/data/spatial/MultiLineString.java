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
 * A MultiLineString is a MultiCurve geometry collection composed of LineString elements.
 */
@DataObject(generateConverter = true)
public class MultiLineString extends Geometry {
  private List<LineString> lineStrings;

  public MultiLineString() {
  }

  public MultiLineString(JsonObject json) {
    super(json);
    MultiLineStringConverter.fromJson(json, this);
  }

  public MultiLineString(MultiLineString other) {
    super(other);
    this.lineStrings = new ArrayList<>(other.lineStrings);
  }

  public MultiLineString(long SRID, List<LineString> lineStrings) {
    super(SRID);
    this.lineStrings = lineStrings;
  }

  public MultiLineString setLineStrings(List<LineString> lineStrings) {
    this.lineStrings = lineStrings;
    return this;
  }

  public List<LineString> getLineStrings() {
    return lineStrings;
  }

  @Override
  public JsonObject toJson() {
    JsonObject json = super.toJson();
    MultiLineStringConverter.toJson(this, json);
    return json;
  }
}
