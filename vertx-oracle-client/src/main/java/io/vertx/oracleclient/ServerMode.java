/*
 * Copyright (c) 2011-2022 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.oracleclient;

import io.vertx.codegen.annotations.VertxGen;

/**
 * Describes the server connection mode.
 */
@VertxGen
public enum ServerMode {

  DEDICATED("dedicated"), SHARED("shared");

  private final String mode;

  ServerMode(String mode) {
    this.mode = mode;
  }

  public static ServerMode of(String mode) {
    return DEDICATED.mode.equalsIgnoreCase(mode) ? DEDICATED : SHARED.mode.equalsIgnoreCase(mode) ? SHARED : null;
  }

  @Override
  public String toString() {
    return mode;
  }
}
