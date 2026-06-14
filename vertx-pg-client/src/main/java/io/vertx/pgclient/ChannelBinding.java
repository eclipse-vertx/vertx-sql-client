/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.pgclient;

import io.vertx.codegen.annotations.VertxGen;

/**
 * The different values for the Channel Binding parameter provide different levels of
 * protection. Channel binding is a method for the server to authenticate itself to the client.
 * It is only supported over SSL connections with PostgreSQL 11 or later servers using the
 * SCRAM authentication method.
 *
 * @see <a href=
 *      "https://www.postgresql.org/docs/current/libpq-connect.html#LIBPQ-CONNECT-CHANNEL-BINDING">
 *      libpq channel_binding</a>
 */
@VertxGen
public enum ChannelBinding {

  /**
   * Prevents the use of channel binding
   */
  DISABLE("disable"),

  /**
   * Means that the client will choose channel binding if available.
   */
  PREFER("prefer"),

  /**
   * Means that the connection must employ channel binding.
   */
  REQUIRE("require");

  public static final ChannelBinding[] VALUES = ChannelBinding.values();

  public final String value;

  ChannelBinding(String value) {
    this.value = value;
  }

  public static ChannelBinding of(String value) {
    for (ChannelBinding channelBinding : VALUES) {
      if (channelBinding.value.equalsIgnoreCase(value)) {
        return channelBinding;
      }
    }

    throw new IllegalArgumentException("Could not find an appropriate Channel Binding mode for the value [" + value + "].");
  }
}
