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

package io.vertx.mysqlclient.impl.util;

import io.netty.util.internal.StringUtil;
import org.junit.Assert;
import org.junit.Test;

public class CachingSha2AuthenticatorTest {
  private static final byte[] PASSWORD = "password".getBytes();

  private String scrambledPasswordHexStr;
  private byte[] nonce;

  @Test
  public void testEncode() {
    scrambledPasswordHexStr = "83ad20dc9b0c61f959fb93f451a42fc2d15ab811b882d667fe8f0fcd0a8acaeb";

    nonce = new byte[]{
      // part1
      0x36, 0x47, 0x48, 0x05, 0x03, 0x6c, 0x6e, 0x60,
      // part2
      0x54, 0x73, 0x50, 0x61, 0x2b, 0x1b, 0x12, 0x04, 0x6e, 0x5a, 0x79, 0x60
    };

    Assert.assertEquals(scrambledPasswordHexStr, StringUtil.toHexString(CachingSha2Authenticator.encode(PASSWORD, nonce)));
  }

  @Test
  public void testEncode2() {
    scrambledPasswordHexStr = "3d58c194ffdaf1f80a6c5ef67700608b07f9a29d37c9e2307aaf690c6e5526ab";

    nonce = new byte[]{
      // part1
      0x6f, 0x2d, 0x5c, 0x6d, 0x66, 0x04, 0x10, 0x70,
      // part2
      0x2b, 0x71, 0x37, 0x76, 0x63, 0x39, 0x2a, 0x02, 0x6d, 0x4a, 0x25, 0x47
    };

    Assert.assertEquals(scrambledPasswordHexStr, StringUtil.toHexString(CachingSha2Authenticator.encode(PASSWORD, nonce)));
  }
}
