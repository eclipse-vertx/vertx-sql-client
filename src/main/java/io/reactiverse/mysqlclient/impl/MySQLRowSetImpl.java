package io.reactiverse.mysqlclient.impl;

import io.reactiverse.mysqlclient.ImplReusable;
import io.reactiverse.pgclient.PgIterator;
import io.reactiverse.pgclient.PgRowSet;
import io.reactiverse.pgclient.Row;

import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collector;

@ImplReusable
public class MySQLRowSetImpl extends MySQLResultBase<PgRowSet, MySQLRowSetImpl> implements PgRowSet {
  static Collector<Row, MySQLRowSetImpl, PgRowSet> COLLECTOR = Collector.of(
    MySQLRowSetImpl::new,
    (set, row) -> {
      if (set.head == null) {
        set.head = set.tail = (MySQLRowImpl) row;
      } else {
        set.tail.next = (MySQLRowImpl) row;
        set.tail = set.tail.next;
      }
    },
    (set1, set2) -> null, // Shall not be invoked as this is sequential
    (set) -> set
  );

  static Function<PgRowSet, MySQLRowSetImpl> FACTORY = rs -> (MySQLRowSetImpl) rs;

  private MySQLRowImpl head;
  private MySQLRowImpl tail;

  @Override
  public PgRowSet value() {
    return this;
  }

  @Override
  public PgIterator iterator() {
    return new PgIterator() {
      MySQLRowImpl current = head;

      @Override
      public boolean hasNext() {
        return current != null;
      }

      @Override
      public Row next() {
        if (current == null) {
          throw new NoSuchElementException();
        }
        MySQLRowImpl r = current;
        current = current.next;
        return r;
      }
    };
  }
}
