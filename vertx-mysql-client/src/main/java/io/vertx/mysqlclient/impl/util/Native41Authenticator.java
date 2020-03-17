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

public class Native41Authenticator {
  /**
   * Native authentication method 'mysql_native_password'
   * Calculate method: SHA1( password ) XOR SHA1( "20-bytes random data from server" <concat> SHA1( SHA1( password ) ) )
   *
   * @param password password value
   * @param salt     20 byte random challenge from server
   * @return scrambled password
   */
  public static byte[] encode(byte[] password, byte[] salt) {
    MessageDigest messageDigest;
    try {
      messageDigest = MessageDigest.getInstance("SHA-1");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }

    // SHA1(password)
    byte[] passwordHash1 = messageDigest.digest(password);
    messageDigest.reset();
    // SHA1(SHA1(password))
    byte[] passwordHash2 = messageDigest.digest(passwordHash1);
    messageDigest.reset();

    // SHA1("20-bytes random data from server" <concat> SHA1(SHA1(password))
    messageDigest.update(salt);
    messageDigest.update(passwordHash2);
    byte[] passwordHash3 = messageDigest.digest();

    // result = passwordHash1 XOR passwordHash3
    for (int i = 0; i < passwordHash1.length; i++) {
      passwordHash1[i] = (byte) (passwordHash1[i] ^ passwordHash3[i]);
    }
    return passwordHash1;
  }
}
