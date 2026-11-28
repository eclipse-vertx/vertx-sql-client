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

package io.vertx.sqlclient.data;

/**
 * The text representation shared by the dense vector types: a bracketed, comma-separated list
 * of components, e.g {@code "[0.1,0.2,0.3]"}.
 */
final class VectorText {

  private VectorText() {
    // Utility class
  }

  /**
   * Check that the text is a bracketed list.
   *
   * @throws IllegalArgumentException when the text is not a bracketed list
   */
  static void checkText(String text) {
    String s = text.trim();
    if (s.length() < 2 || s.charAt(0) != '[' || s.charAt(s.length() - 1) != ']') {
      throw new IllegalArgumentException("Invalid vector text: " + text);
    }
  }

  /**
   * Parse the text into single precision components.
   *
   * @throws IllegalArgumentException when the text is malformed
   */
  static float[] parseFloats(String text) {
    String[] tokens = tokens(text);
    float[] values = new float[tokens.length];
    for (int i = 0; i < tokens.length; i++) {
      try {
        values[i] = Float.parseFloat(tokens[i]);
      } catch (NumberFormatException e) {
        throw invalidComponent(tokens[i]);
      }
    }
    return values;
  }

  /**
   * Parse the text into double precision components.
   *
   * @throws IllegalArgumentException when the text is malformed
   */
  static double[] parseDoubles(String text) {
    String[] tokens = tokens(text);
    double[] values = new double[tokens.length];
    for (int i = 0; i < tokens.length; i++) {
      try {
        values[i] = Double.parseDouble(tokens[i]);
      } catch (NumberFormatException e) {
        throw invalidComponent(tokens[i]);
      }
    }
    return values;
  }

  /**
   * Parse the text into byte components.
   *
   * @throws IllegalArgumentException when the text is malformed
   */
  static byte[] parseBytes(String text) {
    String[] tokens = tokens(text);
    byte[] values = new byte[tokens.length];
    for (int i = 0; i < tokens.length; i++) {
      try {
        values[i] = Byte.parseByte(tokens[i]);
      } catch (NumberFormatException e) {
        throw invalidComponent(tokens[i]);
      }
    }
    return values;
  }

  private static String[] tokens(String text) {
    checkText(text);
    String content = text.trim();
    content = content.substring(1, content.length() - 1).trim();
    if (content.isEmpty()) {
      return new String[0];
    }
    String[] tokens = content.split(",", -1);
    for (int i = 0; i < tokens.length; i++) {
      tokens[i] = tokens[i].trim();
    }
    return tokens;
  }

  private static IllegalArgumentException invalidComponent(String token) {
    return new IllegalArgumentException("Invalid vector component: " + token);
  }

  /**
   * Render single precision components.
   */
  static String toString(float[] values) {
    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < values.length; i++) {
      if (i > 0) {
        sb.append(',');
      }
      sb.append(values[i]);
    }
    return sb.append(']').toString();
  }

  /**
   * Render double precision components.
   */
  static String toString(double[] values) {
    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < values.length; i++) {
      if (i > 0) {
        sb.append(',');
      }
      sb.append(values[i]);
    }
    return sb.append(']').toString();
  }

  /**
   * Render byte components.
   */
  static String toString(byte[] values) {
    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < values.length; i++) {
      if (i > 0) {
        sb.append(',');
      }
      sb.append(values[i]);
    }
    return sb.append(']').toString();
  }
}
