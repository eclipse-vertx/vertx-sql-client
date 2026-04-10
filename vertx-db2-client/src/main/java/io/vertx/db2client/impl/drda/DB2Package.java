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

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ## Packages
 * - One static package named "SYSSTAT" with 41 sections. The static
 *   package may be used by multiple queries because it can never have
 *   a cursor. The static package is used for EXECIMM queries
 * - Other packages are considered "dynamic"
 * - Dynamic package name format is:
 *   SYS[L|S][H|N]<ISO><PKGNUM>
 *   L = Large (384 sections)
 *   S = Small (64 sections)
 *   H = Hold cursors
 *   N = No Hold cursors
 *   ISO = Isolation level, 1 char (0=NC, 1=UR, 2=CS, 3=RS, 4=RR)
 *   PKGNUM = Section Number (00-FF)
 * - By default there are 3 small and 3 large packages
 *
 * See this doc for package and section details:
 * https://www.ibm.com/support/pages/75-ways-demystify-db2-9-tech-tip-db2-cli-packages-demystified
 * https://www.ibm.com/support/knowledgecenter/SSEPGG_11.1.0/com.ibm.db2.luw.apdv.cli.doc/doc/c0004933.html
 */
public class DB2Package {

  private static final Logger LOG = LoggerFactory.getLogger(DB2Package.class);

    private static final int MAX_SECTIONS_SMALL_PKG = 64;
    private static final int MAX_SECTIONS_LARGE_PKG = 384;

    final String name; // ex: SYSSH200
    final String cursorNamePrefix; // ex: SQL_CURSH200C
    final int maxSections;

    byte[] pkgNameConsistencyBytes;
    final ConcurrentLinkedDeque<Section> freeSections = new ConcurrentLinkedDeque<>();
    private AtomicInteger nextAvailableSectionNumber = new AtomicInteger(1);

  public DB2Package(boolean isSmallPackage, int pkgNum) {
      maxSections = isSmallPackage ? MAX_SECTIONS_SMALL_PKG : MAX_SECTIONS_LARGE_PKG;
      // assume packages are always HOLD cursors
      // assume isolation level 2
      String config = (isSmallPackage ? 'S' : 'L') + "H2";
      String pkgNumStr = pkgNum < 16 ? "0" + Integer.toHexString(pkgNum) : Integer.toHexString(pkgNum);
      name = "SYS" + config + pkgNumStr;
      cursorNamePrefix = "SQL_CUR" + config + pkgNumStr + 'C';
  }

  boolean isSmallPackage() {
    return maxSections == MAX_SECTIONS_SMALL_PKG;
  }

  int sectionsInUse() {
    return nextAvailableSectionNumber.get() - 1 - freeSections.size();
  }

  Section getFreeSection() {
    Section s = freeSections.poll();
    if (s != null) {
      s.use();
      if (LOG.isDebugEnabled()) {
        LOG.debug("Using existing section " + s);
      }
      return s;
    }

    int sectionNumber = nextAvailableSectionNumber.getAndIncrement();
    if (sectionNumber > maxSections) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("All sections in use for package " + this);
      }
      return null;
    }

    return new Section(this, sectionNumber);
  }

  @Override
  public String toString() {
    return super.toString() + "{name=" + name + ", freeSections=" + freeSections.size() +
        ",nextSection=" + nextAvailableSectionNumber + ", maxSections=" + maxSections + "}";
  }

}
