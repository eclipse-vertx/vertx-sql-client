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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CachingSha2Authenticator {
  /**
   * Caching SHA-2 pluggable authentication method 'caching_sha2_password'
   * Calculate method: XOR(SHA256(password), SHA256(SHA256(SHA256(password)), Nonce))
   *
   * @param password password value
   * @param nonce    20 byte long random data
   * @return scrambled password
   */
  public static byte[] encode(byte[] password, byte[] nonce) {
    MessageDigest messageDigest;
    try {
      messageDigest = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }

    // SHA256(password)
    byte[] passwordHash1 = messageDigest.digest(password);
    messageDigest.reset();

    // SHA256(SHA256(password))
    byte[] passwordHash2 = messageDigest.digest(passwordHash1);
    messageDigest.reset();

    // SHA256(SHA256(SHA256(password)), Nonce)
    messageDigest.update(passwordHash2);
    byte[] passwordDigest = messageDigest.digest(nonce);

    // result = passwordHash1 XOR passwordDigest
    for (int i = 0; i < passwordHash1.length; i++) {
      passwordHash1[i] = (byte) (passwordHash1[i] ^ passwordDigest[i]);
    }
    return passwordHash1;
  }
}
