package io.vertx.clickhouse.clickhousenative.impl.codec;

import io.vertx.sqlclient.Tuple;

import java.time.temporal.Temporal;
import java.util.HashSet;
import java.util.Set;

public class QueryParsers {
  private static final String[] selectKeywords = new String[]{"SELECT", "WITH", "SHOW", "DESC", "EXISTS", "EXPLAIN"};
  private static final String INSERT_KEYWORD = "INSERT";
  private static final int INSERT_KEYWORD_LENGTH = INSERT_KEYWORD.length();

  private static final String UPDATE_KEYWORD = "UPDATE";
  private static final int UPDATE_KEYWORD_LENGTH = UPDATE_KEYWORD.length();

  //TODO: maybe switch to antlr4
  public static String insertParamValuesIntoQuery(String parametrizedSql, Tuple paramsList) {
    int prevIdx = 0;
    int newIdx;
    StringBuilder bldr = new StringBuilder();
    Set<Integer> usedArgs = new HashSet<>();
    while(prevIdx < parametrizedSql.length() && (newIdx = parametrizedSql.indexOf('$', prevIdx)) != -1) {
      if (newIdx == 0 || parametrizedSql.charAt(newIdx - 1) != '\\') {
        int paramIdxStartPos = newIdx + 1;
        int paramIdxEndPos = paramIdxStartPos;
        while (paramIdxEndPos < parametrizedSql.length() && Character.isDigit(parametrizedSql.charAt(paramIdxEndPos))) {
          ++paramIdxEndPos;
        }
        if (paramIdxStartPos == paramIdxEndPos) {
          throw new IllegalArgumentException("$ without digit at pos " + paramIdxStartPos + " in query " + parametrizedSql);
        }
        int paramIndex = Integer.parseInt(parametrizedSql.substring(paramIdxStartPos, paramIdxEndPos)) - 1;
        if (paramsList == null || paramIndex >= paramsList.size()) {
          throw new IllegalArgumentException("paramList is null or too small(" + (paramsList == null ? null : paramsList.size()) +
            ") for arg with index " + paramIndex);
        }
        Object paramValue = paramsList.getValue(paramIndex);
        bldr.append(parametrizedSql, prevIdx, newIdx);
        Class<?> paramClass = paramValue == null ? null : paramValue.getClass();
        if (paramClass != null) {
          if (CharSequence.class.isAssignableFrom(paramClass) || paramClass == Character.class || Temporal.class.isAssignableFrom(paramClass)) {
            bldr.append('\'').append(paramValue).append('\'');
          } else if (paramClass == Double.class) {
            //DB parser gets mad at 4.9e-322 or smaller. Using cast to cure
            bldr.append(String.format("CAST('%s', 'Float64')", paramValue.toString()));
          } else if (paramClass == Float.class) {
            bldr.append(String.format("CAST('%s', 'Float32')", paramValue.toString()));
          } else {
            bldr.append(paramValue);
          }
        } else {
          bldr.append(paramValue);
        }
        usedArgs.add(paramIndex);
        newIdx = paramIdxEndPos;
      }
      prevIdx = newIdx;
    }
    if (usedArgs.size() != paramsList.size()) {
      throw new IllegalArgumentException("param count mismatch: query consumed " + + usedArgs.size() + ", but provided count is " + paramsList.size());
    }
    bldr.append(parametrizedSql, prevIdx, parametrizedSql.length());
    return bldr.toString();
  }


  public static QueryType queryType(String sql) {
      for (int i = 0; i < sql.length(); i++) {
        String nextTwo = sql.substring(i, Math.min(i + 2, sql.length()));
        if ("--".equals(nextTwo)) {
          i = Math.max(i, sql.indexOf("\n", i));
        } else if ("/*".equals(nextTwo)) {
          i = Math.max(i, sql.indexOf("*/", i));
        } else if (Character.isLetter(sql.charAt(i))) {
          String trimmed = sql.substring(i, Math.min(sql.length(), Math.max(i, sql.indexOf(" ", i))));
          for (String keyword : selectKeywords){
            if (trimmed.regionMatches(true, 0, keyword, 0, keyword.length())) {
              return QueryType.SELECT;
            }
          }
          if (trimmed.regionMatches(true, 0, INSERT_KEYWORD, 0, INSERT_KEYWORD_LENGTH)) {
            return QueryType.INSERT;
          }
          if (trimmed.regionMatches(true, 0, UPDATE_KEYWORD, 0, UPDATE_KEYWORD_LENGTH)) {
            return QueryType.UPDATE;
          }
          return null;
        }
      }
      return null;
  }

  enum QueryType {
    SELECT, INSERT, UPDATE;
  }
}
