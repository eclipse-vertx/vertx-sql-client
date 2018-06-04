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

package io.reactiverse.pgclient.impl;

import io.reactiverse.pgclient.impl.codec.encoder.MessageEncoder;
import io.vertx.core.Handler;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
class ClosePortalCommand extends CommandBase<Void> {

  private final String portal;

  ClosePortalCommand(String portal, Handler<? super CommandResponse<Void>> handler) {
    super(handler);
    this.portal = portal;
  }

  @Override
  void exec(MessageEncoder out) {
    out.writeClosePortal(portal);
    out.writeSync();
  }

  @Override
  public void handleCloseComplete() {
    // Expected
  }
}
