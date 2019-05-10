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
package io.vertx.pgclient.impl.codec;

import io.vertx.sqlclient.impl.command.ClosePortalCommand;

class ClosePortalCommandCodec extends PgCommandCodec<Void, ClosePortalCommand> {

  ClosePortalCommandCodec(ClosePortalCommand cmd) {
    super(cmd);
  }

  @Override
  public void encode(PgEncoder out) {
    out.writeClosePortal(cmd.portal());
    out.writeSync();
  }

  @Override
  public void handleCloseComplete() {
    // Expected
  }
}
