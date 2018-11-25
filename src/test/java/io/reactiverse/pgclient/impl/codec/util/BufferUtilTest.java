package io.reactiverse.pgclient.impl.codec.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Assert;
import org.junit.Test;

public class BufferUtilTest {
  private byte byteToFind;
  private int n;
  private ByteBuf byteBuf;

  private int actualIdx;
  private int expectedIdx;

  @Test
  public void testLocateSecondIdxOfByte() {
    byteBuf = Unpooled.buffer(4);
    byteBuf.writeByte(1);
    byteBuf.writeByte(2);
    byteBuf.writeByte(1);
    byteBuf.writeByte(3);

    byteToFind = 1;
    n = 2;
    expectedIdx = 2;

    actualIdx = Util.nthIndexOf(byteBuf, 0, byteBuf.readableBytes() - 1, byteToFind, n);

    Assert.assertEquals(expectedIdx, actualIdx);
  }

  @Test
  public void testLocateThirdIdxOfByte() {
    byteBuf = Unpooled.buffer(10);
    byteBuf.writeByte(1);
    byteBuf.writeByte(2);
    byteBuf.writeByte(1);
    byteBuf.writeByte(2);
    byteBuf.writeByte(1);
    byteBuf.writeByte(1);
    byteBuf.writeByte(1);
    byteBuf.writeByte(1);
    byteBuf.writeByte(2);
    byteBuf.writeByte(1);

    byteToFind = 2;
    n = 3;
    expectedIdx = 8;

    actualIdx = Util.nthIndexOf(byteBuf, 0, byteBuf.readableBytes() - 1, byteToFind, n);

    Assert.assertEquals(expectedIdx, actualIdx);
  }

  @Test
  public void testLocateIdxOfByteNotExist() {
    byteBuf = Unpooled.buffer(4);
    byteBuf.writeByte(1);
    byteBuf.writeByte(2);
    byteBuf.writeByte(3);
    byteBuf.writeByte(4);

    byteToFind = 5;
    n = 3;
    expectedIdx = -1;

    actualIdx = Util.nthIndexOf(byteBuf, 0, byteBuf.readableBytes() - 1, byteToFind, n);

    Assert.assertEquals(expectedIdx, actualIdx);
  }

  @Test
  public void testLocateIdxLargerThanTimesAppeared() {
    byteBuf = Unpooled.buffer(4);
    byteBuf.writeByte(1);
    byteBuf.writeByte(1);
    byteBuf.writeByte(2);
    byteBuf.writeByte(2);

    byteToFind = 1;
    n = 3;
    expectedIdx = -1;

    actualIdx = Util.nthIndexOf(byteBuf, 0, byteBuf.readableBytes() - 1, byteToFind, n);

    Assert.assertEquals(expectedIdx, actualIdx);
  }
}
