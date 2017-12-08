/*
 * Copyright (C) 2017 Julien Viet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.julienviet.pgclient.impl.codec.util;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static java.nio.charset.StandardCharsets.*;

public class MD5Authentication {

  public static String encode(String username, String password, byte[] salt) {

    HexBinaryAdapter hex = new HexBinaryAdapter();

    byte[] digest, passDigest;

    MessageDigest messageDigest;

    try {
      messageDigest = MessageDigest.getInstance("MD5");
    }
    catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }

    messageDigest.update(password.getBytes(UTF_8));
    messageDigest.update(username.getBytes(UTF_8));
    digest = messageDigest.digest();

    byte[] hexDigest = hex.marshal(digest).toLowerCase().getBytes(US_ASCII);

    messageDigest.update(hexDigest);
    messageDigest.update(salt);
    passDigest = messageDigest.digest();

    return "md5" + hex.marshal(passDigest).toLowerCase();
  }
}
