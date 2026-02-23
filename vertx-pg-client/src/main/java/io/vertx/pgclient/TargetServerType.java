/*
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
 */
package io.vertx.pgclient;

/**
 * Defines the target server type when connecting to a PostgreSQL cluster with multiple hosts.
 * This is similar to the {@code targetServerType} parameter in the PostgreSQL JDBC driver
 * and {@code target_session_attrs} in libpq.
 */
public enum TargetServerType {

  /**
   * Connect to any server (default).
   */
  ANY,

  /**
   * Connect only to a primary (read-write) server.
   */
  PRIMARY,

  /**
   * Connect only to a secondary (read-only/hot standby) server.
   */
  SECONDARY,

  /**
   * Prefer connecting to a primary server, but fall back to a secondary if no primary is available.
   */
  PREFER_PRIMARY,

  /**
   * Prefer connecting to a secondary server, but fall back to a primary if no secondary is available.
   */
  PREFER_SECONDARY
}
