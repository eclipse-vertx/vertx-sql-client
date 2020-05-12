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
 * Geometry is an abstract class which represents the base of MySQL geometry data type.
 */
public abstract class Geometry {
  private long SRID;

  public Geometry() {
  }

  public Geometry(Geometry other) {
    this.SRID = other.SRID;
  }

  public Geometry(long SRID) {
    this.SRID = SRID;
  }

  public long getSRID() {
    return SRID;
  }

  public Geometry setSRID(long SRID) {
    this.SRID = SRID;
    return this;
  }
}
