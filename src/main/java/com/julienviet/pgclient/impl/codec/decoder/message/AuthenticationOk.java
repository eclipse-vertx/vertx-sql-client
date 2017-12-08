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

/**
 *
 * The Authentication is succeeded
 *
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class AuthenticationOk implements InboundMessage {

  public static final AuthenticationOk INSTANCE = new AuthenticationOk();

  private AuthenticationOk() {}

  @Override
  public String toString() {
    return "AuthenticationOk{}";
  }
}
