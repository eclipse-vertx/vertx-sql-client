/*
 * Copyright (c) 2011-2021 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.sqlclient;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * Options for preparing a statement.
 * <p>
 * Currently empty, custom options might be used by implementations to customize specific behavior.
 */
@DataObject
public class PrepareOptions {

  private JsonObject json;

  public PrepareOptions() {
  }

  public PrepareOptions(PrepareOptions other) {
    json = other.json == null ? null : other.json.copy();
  }

  public PrepareOptions(JsonObject json) {
    this();
    this.json = json.copy();
  }

  /**
   * @return a JSON representation of these options
   */
  public JsonObject toJson() {
    return json != null ? json.copy() : new JsonObject();
  }

  @Override
  public String toString() {
    return "PrepareOptions{" +
      "json=" + json +
      '}';
  }
}
