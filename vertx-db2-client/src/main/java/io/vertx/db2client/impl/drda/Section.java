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
package io.vertx.db2client.impl.drda;

import io.vertx.core.internal.logging.Logger;
import io.vertx.core.internal.logging.LoggerFactory;

import java.sql.ResultSet;
import java.util.concurrent.atomic.AtomicBoolean;

public class Section {

  private static final Logger LOG = LoggerFactory.getLogger(Section.class);

  final DB2Package pkg;
    final int number;
    private final AtomicBoolean inUse = new AtomicBoolean(true);

    Section(DB2Package pkg, int sectionNumber) {
      this(pkg, sectionNumber, ResultSet.HOLD_CURSORS_OVER_COMMIT);
    }

    Section(DB2Package pkg, int sectionNumber, int resultSetHoldability) {
        this.pkg = pkg;
        this.number = sectionNumber;
    }

    /**
     * Marks a section for use. An initially created section is already in use.
     * @throws IllegalStateException if this method is called on a section that
     *  is already in use
     * @see #release()
     */
    void use() {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Marking section for use: " + this);
      }

      if (inUse.getAndSet(true)) {
        throw new IllegalStateException("Attempted to use a section multiple times: " + this);
      }
    }

    /**
     * Release a section so it may be used again by a different query.
     * @throws IllegalStateException if this method is called on a free section
     * @see #use()
     */
    public void release() {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Releasing section: " + this);
      }

      if (inUse.getAndSet(false)) {
        pkg.freeSections.add(this);
      } else {
        throw new IllegalStateException("Attempted to release section multiple times: " + this);
      }
    }

    @Override
    public String toString() {
        return super.toString() + "{packageName=" + pkg.name + ", sectionNumber=" + number + ", cursorName=" + pkg.cursorNamePrefix + "}";
    }

    static class ImmediateSection extends Section {
      public ImmediateSection(DB2Package pkg) {
        super(pkg, pkg.maxSections + 1);
      }

      @Override
      void use() {
        // No-op: Static section can be used by multiple statements at once
      }

      @Override
      public void release() {
        // No-op: Static section can be used by multiple statements at once
      }
    }
}
