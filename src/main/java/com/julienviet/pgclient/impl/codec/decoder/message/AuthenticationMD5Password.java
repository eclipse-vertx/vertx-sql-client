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

package com.julienviet.pgclient.impl.codec.decoder.message;

import com.julienviet.pgclient.impl.codec.decoder.InboundMessage;

import java.util.Arrays;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class AuthenticationMD5Password implements InboundMessage {

  private final byte[] salt;

  public AuthenticationMD5Password(byte[] salt) {
    this.salt = salt;
  }

  public byte[] getSalt() {
    return salt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AuthenticationMD5Password that = (AuthenticationMD5Password) o;
    return Arrays.equals(salt, that.salt);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(salt);
  }

  @Override
  public String toString() {
    return "AuthenticationMD5Password{" +
      "salt=" + Arrays.toString(salt) +
      '}';
  }
}
