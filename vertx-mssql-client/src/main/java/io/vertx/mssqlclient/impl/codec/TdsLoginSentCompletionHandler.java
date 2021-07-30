/*
 * Copyright (c) 2011-2021 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mssqlclient.impl.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.ssl.SslHandler;

import static io.vertx.mssqlclient.impl.codec.EncryptionLevel.ENCRYPT_OFF;

public class TdsLoginSentCompletionHandler extends ChannelInboundHandlerAdapter {

  private final SslHandler sslHandler;
  private final byte encryptionLevel;

  private ChannelPipeline pipeline;

  public TdsLoginSentCompletionHandler(SslHandler sslHandler, byte encryptionLevel) {
    this.sslHandler = sslHandler;
    this.encryptionLevel = encryptionLevel;
  }

  @Override
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    pipeline = ctx.pipeline();
  }

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    if (evt == InitCommandCodec.LOGIN_SENT) {
      pipeline.remove(this);
      if (encryptionLevel == ENCRYPT_OFF) {
        pipeline.remove(sslHandler);
      }
    } else {
      super.userEventTriggered(ctx, evt);
    }
  }
}
