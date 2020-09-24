package io.vertx.sqlclient.templates.impl;

import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.impl.SqlClientInternal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlTemplate implements Function<Integer, String> {

  private static Pattern PARAM_PATTERN = Pattern.compile("(?<!\\\\)#\\{(\\p{javaUnicodeIdentifierStart}\\p{javaUnicodeIdentifierPart}*)}");
  private static Pattern BACKSLASH_DOLLAR_PATTERN = Pattern.compile("\\\\#");

  private final String sql;
  private final String[] mapping;

  public static SqlTemplate create(SqlClientInternal client, List<String> template) {
    List<String> mapping = new ArrayList<>();
    Iterator<String> it = template.iterator();
    StringBuilder builder = new StringBuilder();
    while (it.hasNext()) {
      String part = it.next();
      builder.append(sanitize(part));
      if (it.hasNext()) {
        String val = it.next();
        int idx = mapping.indexOf(val);
        int actual = client.appendQueryPlaceholder(builder, idx == -1 ? mapping.size() : idx, mapping.size());
        if (idx == -1 || actual != idx) {
          mapping.add(val);
        }
      } else {
        break;
      }
    }
    return new SqlTemplate(builder.toString(), mapping.toArray(new String[0]));
  }

  /**
   * Sanitize a string escaped dollars, i.e {@code assertEquals(sanitize("\#"), "#")}
   *
   * @param s the string to sanitize
   * @return the sanitized string
   */
  private static String sanitize(String s) {
    StringBuilder sb = new StringBuilder();
    Matcher m = BACKSLASH_DOLLAR_PATTERN.matcher(s);
    int prev = 0;
    while (m.find()) {
      int start = m.start();
      sb.append(s, prev, start);
      sb.append("#");
      prev = m.end();
    }
    sb.append(s, prev, s.length());
    return sb.toString();
  }

  public static SqlTemplate create(SqlClientInternal client, String template) {
    List<String> parts = new ArrayList<>();
    Matcher matcher = PARAM_PATTERN.matcher(template);
    int prev = 0;
    while (matcher.find()) {
      parts.add(template.substring(prev, matcher.start()));
      parts.add(matcher.group(1));
      prev = matcher.end();
    }
    parts.add(template.substring(prev));
    return create(client, parts);
  }

  public SqlTemplate(String sql, String[] mapping) {
    this.sql = sql;
    this.mapping = mapping;
  }

  public String getSql() {
    return sql;
  }

  @Override
  public String apply(Integer idx) {
    return mapping[idx];
  }

  public int numberOfParams() {
    return mapping.length;
  }

  public Tuple mapTuple(Map<String, Object> args) {
    Object[] array = new Object[mapping.length];
    for (int i = 0;i < array.length;i++) {
      array[i] = args.get(mapping[i]);
    }
    return Tuple.wrap(array);
  }
}
