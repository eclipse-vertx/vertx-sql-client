package io.vertx.clickhouse.clickhousenative.impl.codec;

import io.vertx.sqlclient.Tuple;

import java.time.temporal.Temporal;
import java.util.*;

public class QueryParsers {
  private static final String INSERT_KEYWORD = "INSERT";
  private static final String UPDATE_KEYWORD = "UPDATE";

  public static final Set<String> SELECT_KEYWORDS =
    Collections.unmodifiableSet(new HashSet<>(Arrays.asList("SELECT", "WITH", "SHOW", "DESC", "EXISTS", "EXPLAIN")));
  public static final Set<String> SELECT_AND_MUTATE_KEYWORDS =
    Collections.unmodifiableSet(new HashSet<>(Arrays.asList("SELECT", "WITH", "SHOW", "DESC", "EXISTS", "EXPLAIN",
    INSERT_KEYWORD, UPDATE_KEYWORD)));


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
      String msg = String.format("The number of parameters to execute should be consistent with the expected number of parameters = [%d] but the actual number is [%d].",
        usedArgs.size(), paramsList.size());
      throw new IllegalArgumentException(msg);
    }
    bldr.append(parametrizedSql, prevIdx, parametrizedSql.length());
    return bldr.toString();
  }

  public static boolean isSelect(String sql) {
    Map.Entry<String, Integer> tmp = findKeyWord(sql, 0, SELECT_KEYWORDS);
    return tmp != null;
  }

  public static Map.Entry<String, Integer> findKeyWord(String sql, Collection<String> keywords) {
    return findKeyWord(sql, 0, keywords);
  }

  public static Map.Entry<String, Integer> findKeyWord(String sql, int startPos, Collection<String> keywords) {
    Character requiredChar = null;
    //special case to find placeholder
    if (keywords.size() == 1) {
      String str = keywords.iterator().next();
      if (str.length() == 1) {
        requiredChar = str.charAt(0);
      }
    }
    for (int i = startPos; i < sql.length(); i++) {
      String nextTwo = sql.substring(i, Math.min(i + 2, sql.length()));
      if ("--".equals(nextTwo)) {
        i = Math.max(i, sql.indexOf("\n", i));
      } else if ("/*".equals(nextTwo)) {
        i = Math.max(i, sql.indexOf("*/", i));
      } else if (requiredChar != null && requiredChar == sql.charAt(i)) {
        return new AbstractMap.SimpleEntry<>(keywords.iterator().next(), i);
      } else if (sql.charAt(i) == '\'' && (i == 0 || sql.charAt(i - 1) != '\\')) {
        while (i < sql.length() && !(sql.charAt(i) == '\'' && (i == 0 || sql.charAt(i - 1) != '\\'))) {
          ++i;
        }
      } else {
        if (Character.isLetter(sql.charAt(i))) {
          String trimmed = sql.substring(i, Math.min(sql.length(), Math.max(i, sql.indexOf(" ", i))));
          for (String keyword : keywords){
            if (trimmed.regionMatches(true, 0, keyword, 0, keyword.length())) {
              return new AbstractMap.SimpleEntry<>(keyword, i);
            }
          }
          if (requiredChar == null) {
            return null;
          }
        }
      }
    }
    return null;
  }
}
