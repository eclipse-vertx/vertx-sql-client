package io.reactiverse.pgclient.copy;

import io.vertx.codegen.annotations.VertxGen;

/**
 * Represents the format of a COPY.
 */
@VertxGen
public enum CopyFormat {
  /**
   * Indicates a standard text format.
   */
  TEXT("\t", "\\N"),
  /**
   * Indicates CSV format.
   */
  CSV("," , " "),
  /**
   * Indicates binary format.
   */
  BINARY(null, null);

  private final String defaultDelimiter;
  private final String nullCharacter;

  CopyFormat(String defaultDelimiter, String nullCharacter) {
    this.defaultDelimiter = defaultDelimiter;
    this.nullCharacter = nullCharacter;
  }

  /**
   * Returns the default delimiter for the given format. Not applicable for binary.
   * @return
   */
  public String getDefaultDelimiter() {
    return defaultDelimiter;
  }

  /**
   * Returns the null character for the given format. Not applicable for binary.
   * @return
   */
  public String getNullCharacter() {
    return nullCharacter;
  }
}
