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

package io.vertx.oracleclient;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.json.annotations.JsonGen;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.PrepareOptions;

@DataObject
@JsonGen(publicConverter = false)
public class OraclePrepareOptions extends PrepareOptions {

  public static final boolean DEFAULT_AUTO_GENERATED_KEY = true;

  private boolean autoGeneratedKeys = DEFAULT_AUTO_GENERATED_KEY;
  private JsonArray autoGeneratedKeysIndexes;

  public OraclePrepareOptions() {
  }

  public OraclePrepareOptions(OraclePrepareOptions options) {
    super(options);
    this.autoGeneratedKeys = options.autoGeneratedKeys;
    this.autoGeneratedKeysIndexes = options.autoGeneratedKeysIndexes != null ? options.autoGeneratedKeysIndexes.copy() : null;
  }

  public OraclePrepareOptions(JsonObject json) {
    super(json);
    OraclePrepareOptionsConverter.fromJson(json, this);
  }

  /**
   * Creates a new instance of {@link OraclePrepareOptions} from the provided {@link PrepareOptions}.
   *
   * @param prepareOptions an instance of {@link PrepareOptions}
   * @return a copy of {@code prepareOptions} or {@code null} if {@code prepareOptions} is {@code null}
   */
  public static OraclePrepareOptions createFrom(PrepareOptions prepareOptions) {
    OraclePrepareOptions oraclePrepareOptions;
    if (prepareOptions == null) {
      oraclePrepareOptions = null;
    } else if (prepareOptions instanceof OraclePrepareOptions) {
      oraclePrepareOptions = new OraclePrepareOptions((OraclePrepareOptions) prepareOptions);
    } else {
      oraclePrepareOptions = new OraclePrepareOptions(prepareOptions.toJson());
    }
    return oraclePrepareOptions;
  }

  public boolean isAutoGeneratedKeys() {
    return autoGeneratedKeys;
  }

  public OraclePrepareOptions setAutoGeneratedKeys(boolean autoGeneratedKeys) {
    this.autoGeneratedKeys = autoGeneratedKeys;
    return this;
  }

  public JsonArray getAutoGeneratedKeysIndexes() {
    return autoGeneratedKeysIndexes;
  }

  public OraclePrepareOptions setAutoGeneratedKeysIndexes(JsonArray autoGeneratedKeysIndexes) {
    this.autoGeneratedKeysIndexes = autoGeneratedKeysIndexes;
    return this;
  }

  @Override
  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    OraclePrepareOptionsConverter.toJson(this, jsonObject);
    return jsonObject;
  }

  @Override
  public String toString() {
    return "OraclePrepareOptions{" +
      "autoGeneratedKeys=" + autoGeneratedKeys +
      ", autoGeneratedKeysIndexes=" + autoGeneratedKeysIndexes +
      '}';
  }
}
