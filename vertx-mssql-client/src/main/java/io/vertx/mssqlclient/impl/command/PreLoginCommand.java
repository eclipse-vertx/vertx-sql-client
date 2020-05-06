/*
 * Copyright (c) 2011-2019 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mssqlclient.impl.command;

import io.vertx.mssqlclient.impl.protocol.client.prelogin.EncryptionOptionToken;
import io.vertx.mssqlclient.impl.protocol.client.prelogin.OptionToken;
import io.vertx.mssqlclient.impl.protocol.client.prelogin.VersionOptionToken;
import io.vertx.sqlclient.impl.command.CommandBase;

import java.util.ArrayList;
import java.util.List;

public class PreLoginCommand extends CommandBase<Void> {
  private final List<OptionToken> optionTokens = new ArrayList<>();

  public PreLoginCommand(boolean ssl) {
    initVersionOptionToken();
    initEncryptOptionToken(ssl);
  }

  public List<OptionToken> optionTokens() {
    return optionTokens;
  }

  private void initVersionOptionToken() {
    optionTokens.add(new VersionOptionToken((short) 0, (short) 0, 0, 0));
  }

  private void initEncryptOptionToken(boolean ssl) {
    if (ssl) {
      optionTokens.add(new EncryptionOptionToken(EncryptionOptionToken.ENCRYPT_ON));
    } else {
      optionTokens.add(new EncryptionOptionToken(EncryptionOptionToken.ENCRYPT_NOT_SUP));
    }
  }
}
