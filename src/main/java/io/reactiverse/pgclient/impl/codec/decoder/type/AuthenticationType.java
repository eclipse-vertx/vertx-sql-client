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

package io.reactiverse.pgclient.impl.codec.decoder.type;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class AuthenticationType {
  public static final int OK = 0;
  public static final int KERBEROS_V5 = 2;
  public static final int CLEARTEXT_PASSWORD = 3;
  public static final int MD5_PASSWORD = 5;
  public static final int SCM_CREDENTIAL = 6;
  public static final int GSS = 7;
  public static final int GSS_CONTINUE = 8;
  public static final int SSPI = 9;
}
