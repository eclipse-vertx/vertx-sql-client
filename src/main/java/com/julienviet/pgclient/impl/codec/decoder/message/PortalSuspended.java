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

import com.julienviet.pgclient.PgResult;
import com.julienviet.pgclient.impl.codec.encoder.message.Execute;
import com.julienviet.pgclient.impl.codec.decoder.InboundMessage;

/**
 *
 * <p>
 * The appearance of this message tells the frontend that another {@link Execute} should be issued against the
 * same portal to complete the operation. The {@link CommandComplete} message indicating completion of the source
 * SQL command is not sent until the portal's execution is completed
 *
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class PortalSuspended implements InboundMessage {

  private final PgResult<?> result;

  public PortalSuspended(PgResult<?> result) {
    this.result = result;
  }

  public PgResult<?> result() {
    return result;
  }

  @Override
  public String toString() {
    return "PortalSuspended{}";
  }
}
