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
 * A MultiPoint is a geometry collection composed of Point elements. The points are not connected or ordered in any way.
 */
public class MultiPoint extends Geometry {
  private List<Point> points;

  public MultiPoint() {
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
}
