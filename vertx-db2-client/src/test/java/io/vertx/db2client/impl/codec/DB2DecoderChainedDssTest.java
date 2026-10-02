/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.db2client.impl.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;

import java.util.ArrayDeque;

import static org.junit.Assert.*;

/**
 * Tests for {@link DB2Decoder#computeLength(ByteBuf)} handling of chained
 * DRDA DSS segments that arrive split across TCP deliveries.
 * <p>
 * A DRDA response can consist of multiple DSS (Data Stream Structure) segments
 * chained together via bit 6 (0x40) of byte 3 in each DSS header. When TCP
 * delivers only the first segment(s) of a chained response, the decoder must
 * wait for the remaining segments before attempting to process the response.
 * <p>
 * DSS header format (6 bytes):
 * <pre>
 *   bytes 0-1: length (unsigned 16-bit, big-endian)
 *   byte  2  : magic  (0xD0)
 *   byte  3  : format flags (bit 6 = chain bit: 0x40 = chained, 0x00 = last)
 *   bytes 4-5: correlation ID
 * </pre>
 */
public class DB2DecoderChainedDssTest {

  private static final byte DSS_MAGIC = (byte) 0xD0;
  private static final byte CHAIN = 0x42;     // chain bit (0x40) + RPYDSS format (0x02)
  private static final byte NO_CHAIN = 0x02;  // no chain, RPYDSS format

  private EmbeddedChannel createChannel() {
    ArrayDeque<DB2CommandMessage<?, ?>> inflight = new ArrayDeque<>();
    return new EmbeddedChannel(new DB2Decoder(inflight));
  }

  private ByteBuf buildDss(int length, boolean chained) {
    ByteBuf buf = Unpooled.buffer(length);
    buf.writeShort(length);
    buf.writeByte(DSS_MAGIC);
    buf.writeByte(chained ? CHAIN : NO_CHAIN);
    buf.writeShort(1); // correlation ID
    buf.writeZero(length - 6);
    return buf;
  }

  /**
   * When a chained DSS arrives but its successor has not been delivered yet,
   * the decoder must wait for more data. Before the fix, computeLength()
   * returned the length of just the first DSS (which equalled readableBytes),
   * causing decode() to pass partial data to decodePayload(). With an empty
   * inflight queue this manifested as a NullPointerException.
   */
  @Test
  public void testChainedDssSplitAcrossTcpDeliveries() {
    EmbeddedChannel channel = createChannel();

    ByteBuf dss1 = buildDss(20, true);

    // Feed only DSS_1 (chain bit set) without the following DSS_2.
    // The decoder must NOT attempt to process this partial response.
    assertFalse(
        "Decoder must not produce output for incomplete chained response",
        channel.writeInbound(dss1));

    // No exception means the decoder correctly waited for more data.
    // Before the fix, this would NPE in decodePayload() because
    // inflight.peek() returned null.
    assertNull("No inbound message expected", channel.readInbound());

    channel.finishAndReleaseAll();
  }

  /**
   * When only a partial DSS header is available (fewer than 4 bytes, so the
   * chain bit cannot be read), the decoder must wait for more data.
   */
  @Test
  public void testPartialDssHeader() {
    EmbeddedChannel channel = createChannel();

    // Only 3 bytes - not enough for a full 4-byte header check
    ByteBuf partial = Unpooled.buffer(3);
    partial.writeShort(20);     // declared length = 20
    partial.writeByte(DSS_MAGIC);

    assertFalse(
        "Decoder must wait when DSS header is incomplete",
        channel.writeInbound(partial));

    assertNull("No inbound message expected", channel.readInbound());

    channel.finishAndReleaseAll();
  }

  /**
   * When a chained DSS arrives with its successor partially delivered (only
   * part of DSS_2's bytes are in the buffer), the decoder must still wait.
   */
  @Test
  public void testChainedDssWithPartialSecondSegment() {
    EmbeddedChannel channel = createChannel();

    ByteBuf dss1 = buildDss(20, true);
    // DSS_2 declares length 30 but only 10 bytes arrived so far
    ByteBuf partialDss2 = Unpooled.buffer(10);
    partialDss2.writeShort(30);
    partialDss2.writeByte(DSS_MAGIC);
    partialDss2.writeByte(NO_CHAIN);
    partialDss2.writeShort(1);
    partialDss2.writeZero(4);

    ByteBuf combined = Unpooled.wrappedBuffer(dss1, partialDss2);

    assertFalse(
        "Decoder must wait when second DSS segment is incomplete",
        channel.writeInbound(combined));

    assertNull("No inbound message expected", channel.readInbound());

    channel.finishAndReleaseAll();
  }

  /**
   * Three-segment chain where only the first two segments have arrived.
   * DSS_1 (chain) -> DSS_2 (chain) -> DSS_3 (not yet received).
   */
  @Test
  public void testThreeSegmentChainWithMissingThird() {
    EmbeddedChannel channel = createChannel();

    ByteBuf dss1 = buildDss(16, true);
    ByteBuf dss2 = buildDss(24, true);  // chain bit still set

    ByteBuf combined = Unpooled.wrappedBuffer(dss1, dss2);

    assertFalse(
        "Decoder must wait when third chained segment is missing",
        channel.writeInbound(combined));

    assertNull("No inbound message expected", channel.readInbound());

    channel.finishAndReleaseAll();
  }
}
