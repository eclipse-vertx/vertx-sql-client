/*
 * Copyright (c) 2011-2020 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mssqlclient.impl.codec;

import io.netty.channel.ChannelPipeline;

import java.util.ArrayDeque;

public class MSSQLCodec {
  public static void initPipeLine(ChannelPipeline pipeline) {
    final ArrayDeque<MSSQLCommandCodec<?, ?>> inflight = new ArrayDeque<>();

    TdsMessageEncoder encoder = new TdsMessageEncoder(inflight);
    TdsMessageDecoder messageDecoder = new TdsMessageDecoder(inflight, encoder);
    TdsPacketDecoder packetDecoder = new TdsPacketDecoder();
    pipeline.addBefore("handler", "encoder", encoder);
    pipeline.addBefore("encoder", "messageDecoder", messageDecoder);
    pipeline.addBefore("messageDecoder", "packetDecoder", packetDecoder);
  }
}
