/*
 * Copyright (c) 2011-2019 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mssqlclient.impl.protocol.client.prelogin;

public final class VersionOptionToken extends OptionToken {
  public static final byte TYPE = 0x00;

  private final short majorVersion;
  private final short minorVersion;
  private final int buildNumber;
  private final int subBuildNumber;

  public VersionOptionToken(short majorVersion, short minorVersion, int buildNumber, int subBuildNumber) {
    super(TYPE, 6);
    this.majorVersion = majorVersion;
    this.minorVersion = minorVersion;
    this.buildNumber = buildNumber;
    this.subBuildNumber = subBuildNumber;
  }

  public short majorVersion() {
    return majorVersion;
  }

  public short minorVersion() {
    return minorVersion;
  }

  public int buildNumber() {
    return buildNumber;
  }

  public int subBuildNumber() {
    return subBuildNumber;
  }
}
