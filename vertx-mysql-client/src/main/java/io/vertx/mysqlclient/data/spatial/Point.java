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

/**
 * A Point is a geometry that represents a single location in coordinate space.
 */
public class Point extends Geometry {
  private double x, y;

  public Point() {
  }

  public Point(Point other) {
    super(other);
    this.x = other.x;
    this.y = other.y;
  }

  public Point(long SRID, double x, double y) {
    super(SRID);
    this.x = x;
    this.y = y;
  }

  public double getX() {
    return x;
  }

  public Point setX(double x) {
    this.x = x;
    return this;
  }

  public Point setY(double y) {
    this.y = y;
    return this;
  }

  public double getY() {
    return y;
  }
}
