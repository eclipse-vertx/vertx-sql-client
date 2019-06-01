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

package io.vertx.pgclient;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PgException extends RuntimeException {

  private final String severity;
  private final String code;
  private final String detail;

  public PgException(String message, String severity, String code, String detail) {
    super(message);
    this.severity = severity;
    this.code = code;
    this.detail = detail;
  }

  public String getSeverity() {
    return severity;
  }

  public String getCode() {
    return code;
  }

  /**
   * @return the detail error message
   */
  public String getDetail() {
    return detail;
  }
}
