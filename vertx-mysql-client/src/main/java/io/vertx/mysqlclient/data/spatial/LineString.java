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

import java.util.ArrayList;
import java.util.List;

/**
 * A LineString is a Curve with linear interpolation between points, it may represents a Line or a LinearRing.
 */
public class LineString extends Geometry {
  private List<Point> points;

  public LineString() {
  }

  public LineString(LineString other) {
    super(other);
    this.points = new ArrayList<>(other.points);
  }

  public LineString(long SRID, List<Point> points) {
    super(SRID);
    this.points = points;
  }

  public LineString setPoints(List<Point> points) {
    this.points = points;
    return this;
  }

  public List<Point> getPoints() {
    return points;
  }
}
