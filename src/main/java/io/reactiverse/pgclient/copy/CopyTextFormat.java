package io.reactiverse.pgclient.copy;

public enum CopyTextFormat {
  TEXT("\t", "\\N"), CSV("," , " ");

  private final String defaultDelimiter;
  private final String nullCharacter;

  CopyTextFormat(String defaultDelimiter, String nullCharacter) {
    this.defaultDelimiter = defaultDelimiter;
    this.nullCharacter = nullCharacter;
  }

  public String getDefaultDelimiter() {
    return defaultDelimiter;
  }

  public String getNullCharacter() {
    return nullCharacter;
  }
}
