/*
 * Copyright (C) 2019,2020 IBM Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertx.db2client.impl.drda;

import java.sql.ResultSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Section {
	
	private static final Logger LOG = Logger.getLogger(Section.class.getName());
    
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
    	if (LOG.isLoggable(Level.FINE))
    		LOG.fine("Marking section for use: " + this);
    	
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
    	if (LOG.isLoggable(Level.FINE))
    		LOG.fine("Releasing section: " + this);
    	
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