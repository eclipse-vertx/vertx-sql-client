package io.vertx.sqlclient.template.impl;

import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SqlTemplate {

  private static Pattern PARAM_PATTERN = Pattern.compile(":(\\p{javaUnicodeIdentifierStart}\\p{javaUnicodeIdentifierPart}*)");

  public final String sql;
  final List<String> mapping;

  public SqlTemplate(SqlClient client, String template) {
    mapping = new ArrayList<>();
    StringBuilder actual = new StringBuilder();
    Matcher matcher = PARAM_PATTERN.matcher(template);
    int pos = 0;
    int idx = 0;
    while (matcher.find()) {
      mapping.add(matcher.group(1));
      actual.append(template, pos, matcher.start());
      actual.append('$').append(++idx);
      pos = matcher.end();
    }
    actual.append(template, pos, template.length());
    sql = actual.toString();
  }

  public Tuple mapTuple(Map<String, Object> args) {
    return Tuple.wrap(mapping.stream().map(args::get).collect(Collectors.toList()));
  }

}
