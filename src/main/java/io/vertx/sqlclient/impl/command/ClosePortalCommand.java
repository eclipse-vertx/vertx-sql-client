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

package io.vertx.sqlclient.impl.command;

import io.vertx.sqlclient.impl.PreparedStatement;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class ClosePortalCommand extends CommandBase<Void> {

  private final String portal;
  private final PreparedStatement ps;

  public ClosePortalCommand(String portal, PreparedStatement ps) {
    this.portal = portal;
    this.ps = ps;
  }

  public String portal() {
    return portal;
  }

  public PreparedStatement ps() {
    return ps;
  }
}
