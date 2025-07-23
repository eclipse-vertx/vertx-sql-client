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

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RsaPublicKeyEncryptor {
  /**
   * Encrypt the NULL-terminated password with the nonce and RSA public key provided by the server.
   */
  public static byte[] encrypt(byte[] password, byte[] nonce, String serverRsaPublicKey) throws Exception {
    RSAPublicKey rsaPublicKey = generateRsaPublicKey(serverRsaPublicKey);
    byte[] obfuscatedPassword = obfuscate(password, nonce);
    return encrypt(rsaPublicKey, obfuscatedPassword);
  }

  private static RSAPublicKey generateRsaPublicKey(String serverRsaPublicKey) throws InvalidKeySpecException, NoSuchAlgorithmException {
    String content = serverRsaPublicKey.replace("-----BEGIN PUBLIC KEY-----", "")
      .replace("-----END PUBLIC KEY-----", "")
      .replaceAll("\\n", "");

    byte[] key = Base64.getDecoder().decode(content.getBytes());
    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(key);

    KeyFactory keyFactory = KeyFactory.getInstance("RSA");

    return (RSAPublicKey) keyFactory.generatePublic(keySpec);
  }

  private static byte[] obfuscate(byte[] password, byte[] nonce) {
    // the password input can be mutated here
    for (int i = 0; i < password.length; i++) {
      password[i] = (byte) (password[i] ^ nonce[i % nonce.length]);
    }
    return password;
  }

  private static byte[] encrypt(PublicKey key, byte[] plainData) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
    Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    cipher.init(Cipher.ENCRYPT_MODE, key);
    return cipher.doFinal(plainData);
  }
}
