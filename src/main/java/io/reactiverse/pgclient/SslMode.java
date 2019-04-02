/*
 * Copyright (C) 2018 Julien Viet
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
package io.reactiverse.pgclient;

/**
 * The different values for the sslmode parameter provide different levels of protection.
 * See more information in <a href="https://www.postgresql.org/docs/current/libpq-ssl.html#LIBPQ-SSL-PROTECTION">Protection Provided in Different Modes</a>.
 */
public enum SslMode {

  /**
   * only try a non-SSL connection.
   */
  DISABLE("disable"),

  /**
   * first try a non-SSL connection; if that fails, try an SSL connection.
   */
  ALLOW("allow"),

  /**
   * first try an SSL connection; if that fails, try a non-SSL connection.
   */
  PREFER("prefer"),

  /**
   * only try an SSL connection. If a root CA file is present, verify the certificate in the same way as if verify-ca was specified.
   */
  REQUIRE("require"),

  /**
   * only try an SSL connection, and verify that the server certificate is issued by a trusted certificate authority (CA).
   */
  VERIFY_CA("verify-ca"),

  /**
   * only try an SSL connection, verify that the server certificate is issued by a trusted CA and that the requested server host name matches that in the certificate.
   */
  VERIFY_FULL("verify-full");

  public static final SslMode[] VALUES = SslMode.values();

  public final String value;

  SslMode(String value) {
    this.value = value;
  }

  public static SslMode of(String value) {
    for (SslMode sslMode : VALUES) {
      if (sslMode.value.equalsIgnoreCase(value)) {
        return sslMode;
      }
    }

    throw new IllegalArgumentException("Could not find an appropriate SSL mode for the value [" + value + "].");
  }
}
