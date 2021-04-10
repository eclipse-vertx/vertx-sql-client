/*
 *
 *  Copyright (c) 2021 Vladimir Vishnevskii
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Eclipse Public License 2.0 which is available at
 *  http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 *  which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *  SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package io.vertx.clickhouse.clickhousenative;

import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeConnectionUriParser;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.SqlConnectOptions;

@DataObject(generateConverter = true)
public class ClickhouseNativeConnectOptions extends SqlConnectOptions {

  public static ClickhouseNativeConnectOptions fromUri(String connectionUri) throws IllegalArgumentException {
    JsonObject parsedConfiguration = ClickhouseNativeConnectionUriParser.parse(connectionUri);
    return new ClickhouseNativeConnectOptions(parsedConfiguration);
  }

  public ClickhouseNativeConnectOptions() {
    super();
  }

  public ClickhouseNativeConnectOptions(JsonObject json) {
    super(json);
    ClickhouseNativeConnectOptionsConverter.fromJson(json, this);
  }

  public ClickhouseNativeConnectOptions(SqlConnectOptions other) {
    super(other);
  }

  public ClickhouseNativeConnectOptions(ClickhouseNativeConnectOptions other) {
    super(other);
  }

}
