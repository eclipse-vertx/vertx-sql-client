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
package io.vertx.db2client.impl.codec;

import java.util.ArrayDeque;

import io.netty.buffer.ByteBuf;
import io.netty.channel.CombinedChannelDuplexHandler;
import io.vertx.db2client.impl.DB2SocketConnection;

public class DB2Codec extends CombinedChannelDuplexHandler<DB2Decoder, DB2Encoder> {
    
    // TODO @AGG check what packet length limit actually is for DB2
    static final int PACKET_PAYLOAD_LENGTH_LIMIT = 0xFFFFFF;

  private final ArrayDeque<CommandCodec<?, ?>> inflight = new ArrayDeque<>();
  
  private static final boolean DEBUG_BYTES = true;

  public DB2Codec(DB2SocketConnection mySQLSocketConnection) {
    DB2Encoder encoder = new DB2Encoder(inflight, mySQLSocketConnection);
    DB2Decoder decoder = new DB2Decoder(inflight);
    init(decoder, encoder);
  }
  
  public static void dumpBuffer(ByteBuf buffer) {
      dumpBuffer(buffer, buffer.readableBytes());
  }
  
  public static void dumpBuffer(ByteBuf buffer, int length) {
      dumpBuffer(buffer, length, -1);
  }
  
  public static void dumpBuffer(ByteBuf buffer, int length, int calloutIndex) {
      if (!DEBUG_BYTES)
          return;
      System.out.println(buffer.toString());
      ByteBuf copy = buffer.slice(buffer.readerIndex(), length);
      System.out.print("  hex / dec   0  1  2  3  4  5  6  7    8  9  A  B  C  D  E  F ");
      StringBuilder asciiLine = new StringBuilder(18);
      for (int i = 0; i < copy.writerIndex(); i++) {
          if (calloutIndex >= 0 
                  && calloutIndex / 16 == i / 16 - 1
                  && i % 16 == 0) {
              System.out.print("\n              ");
              for  (int j = 0; j < 16; j++) {
                  if (j == calloutIndex % 16)
                      System.out.print("^^");
                  else
                      System.out.print("  ");
                  if (j == 8)
                      System.out.print("  ");
                  if (j < 15)
                      System.out.print(' ');
              }
          }
          if (i % 16 == 0) {
              System.out.print("  " + asciiLine.toString());
              asciiLine = new StringBuilder(18);
              System.out.printf("\n  %04x/%04d", i, i);
          }
          
          if (i % 8 == 0) {
              asciiLine.append("  ");
              System.out.print("  ");
          }
          System.out.print(" ");
          byte b = copy.getByte(i);
          System.out.print(String.format("%02x", b));
          if (b > 0x1F && b < 0x7F)
              asciiLine.append((char) b);
          else
              asciiLine.append('.');
      }
      System.out.println();
  }
  
  public static void dumpBytes(byte[] bytes) {
      if (!DEBUG_BYTES)
          return;
      for (int i = 0; i < bytes.length; i++) {
          if (i % 16 == 0)
              System.out.print("\n  ");
          if (i % 8 == 0)
              System.out.print("    ");
          System.out.print(" ");
          System.out.print(String.format("%02x", bytes[i]));
      }
      System.out.println();
  }
}
