package io.vertx.pgclient.impl.codec;

import io.netty.handler.codec.DecoderException;
import io.netty.util.CharsetUtil;
import io.vertx.core.buffer.Buffer;
import io.vertx.pgclient.data.Cidr;
import io.vertx.pgclient.data.Inet;
import io.vertx.pgclient.data.Path;
import io.vertx.pgclient.data.Polygon;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

/**
 */
class DataTypeEstimator {

  static final int UNSUPPORTED = 0;
  private static final int UTF8 = -1;

  static final int NUMERIC = -2;
  static final int NUMERIC_ARRAY = -7;
  static final int BUFFER = -3;

  static final int UNKNOWN = -6;

  static final int BOOL = 1;
  static final int INT2 = 2;
  static final int INT4 = 4;
  static final int INT8 = 8;
  static final int FLOAT4 = 4;
  static final int FLOAT8 = 8;

  static final int CHAR = UTF8;
  static final int VARCHAR = UTF8;
  static final int BPCHAR = UTF8;
  static final int TEXT = UTF8;
  static final int NAME = UTF8;

  static final int DATE = 4;
  static final int TIME = 8;
  static final int TIMETZ = 12;
  static final int TIMESTAMP = 8;
  static final int TIMESTAMPTZ = 8;
  static final int INTERVAL = 16;

  static final int BYTEA = BUFFER;

  static final int INET = -10;
  static final int CIDR = -9;
  static final int UUID = 16;

  static final int JSON = UTF8;
  static final int JSONB = -8;

  static final int MONEY = 8;

  static final int POINT = 16;
  static final int LINE = 24;
  static final int LSEG = 32;
  static final int BOX = 32;
  static final int CIRCLE = 24;
  static final int POLYGON = -4;
  static final int PATH = -5;

  // Eventually make this configurable per options
  private static final float AVG_BYTES_PER_CHAR_UTF8 = CharsetUtil.encoder(CharsetUtil.UTF_8).averageBytesPerChar();

  static int estimateUTF8(String s) {
    return (int)(s.length() * AVG_BYTES_PER_CHAR_UTF8);
  }

  static int estimateByteArray(byte[] b) {
    return b.length;
  }

  static int estimateCStringUTF8(String s) {
    return estimateUTF8(s) + 1;
  }

  private static int estimateUnknown(String value) {
    return estimateUTF8(value);
  }

  private static int estimateJSONB(String value) {
    return 1 + estimateUTF8(value);
  }

  private static int estimateNumeric(String value) {
    return estimateUTF8(value);
  }

  private static int estimateInetOrCidr(Cidr value) {
    return estimateInetOrCidr(value.getAddress());
  }

  private static int estimateInetOrCidr(Inet value) {
    return estimateInetOrCidr(value.getAddress());
  }

  private static int estimateInetOrCidr(InetAddress address) {
    int len;
    if (address instanceof Inet6Address) {
      Inet6Address inet6Address = (Inet6Address) address;
      len = inet6Address.getAddress().length;
    } else if (address instanceof Inet4Address) {
      Inet4Address inet4Address = (Inet4Address) address;
      len = inet4Address.getAddress().length;
    } else {
      // Invalid
      len = 0;
    }
    return 1 + 1 + 1 + 1 + len;
  }

  private static int estimateNumericArray(Object[] value) {
    int length = 1;
    for (Object elt : value) {
      length += elt == null ? 4 : estimateNumeric((String) elt);
    }
    length += value.length;
    return length;
  }

  private static int estimateBuffer(Buffer b) {
    return b.length();
  }

  private static int estimatePolygon(Polygon p) {
    return 4 + p.getPoints().size() * 16;
  }

  private static int estimatePath(Path p) {
    return 1 + 4 + p.getPoints().size() * 16;
  }

  static int estimate(int estimator, Object o) {
    if (estimator > 0) {
      return estimator;
    } else {
      switch (estimator) {
        case DataTypeEstimator.CIDR:
          return estimateInetOrCidr((Cidr) o);
        case DataTypeEstimator.INET:
          return estimateInetOrCidr((Inet) o);
        case DataTypeEstimator.JSONB:
          return estimateJSONB((String) o);
        case DataTypeEstimator.UNKNOWN:
          return estimateUnknown((String) o);
        case DataTypeEstimator.NUMERIC:
          return estimateNumeric((String) o);
        case DataTypeEstimator.NUMERIC_ARRAY:
          return estimateNumericArray((Object[]) o);
        case DataTypeEstimator.UTF8:
          return estimateUTF8((String) o);
        case DataTypeEstimator.BUFFER:
          return estimateBuffer((Buffer) o);
        case DataTypeEstimator.POLYGON:
          return estimatePolygon((Polygon) o);
        case DataTypeEstimator.PATH:
          return estimatePath((Path) o);
        default:
          throw new UnsupportedOperationException();
      }
    }
  }
}
