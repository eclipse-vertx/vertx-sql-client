package io.vertx.sqlclient.template.impl;

import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.impl.SqlClientInternal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlTemplate {

  private static Pattern PARAM_PATTERN = Pattern.compile(":(\\p{javaUnicodeIdentifierStart}\\p{javaUnicodeIdentifierPart}*)");

  private final String sql;
  private final String[] mapping;

  public SqlTemplate(SqlClientInternal client, String template) {
    List<String> mapping = new ArrayList<>();
    Matcher matcher = PARAM_PATTERN.matcher(template);
    int prev = 0;
    StringBuilder builder = new StringBuilder();
    while (matcher.find()) {
      builder.append(template, prev, matcher.start());
      String val = matcher.group(1);
      int idx = mapping.indexOf(val);
      int actual = client.appendQueryPlaceHolder(builder, idx == -1 ? mapping.size() : idx, mapping.size());
      if (idx == -1 || actual != idx) {
        mapping.add(val);
      }
      prev = matcher.end();
    }
    builder.append(template, prev, template.length());
    this.sql = builder.toString();
    this.mapping = mapping.toArray(new String[0]);
  }

  public String getSql() {
    return sql;
  }

  public Tuple mapTuple(Map<String, Object> args) {
    Object[] array = new Object[mapping.length];
    for (int i = 0;i < array.length;i++) {
      array[i] = args.get(mapping[i]);
    }
    return Tuple.wrap(array);
  }
}
