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
package tests.oracleclient.junit;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.management.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * JUnit 4 rule that dumps all thread stack traces when a test exceeds a configured timeout.
 * Active only when the {@code test.threadDump.timeoutSeconds} system property is set.
 * After the initial dump, subsequent dumps are produced every 60 seconds.
 * Dumps are written to the surefire-reports directory with a filename matching the pattern
 * picked up by CI artifact uploads.
 */
public class ThreadDumpOnTimeoutRule implements TestRule {

  private static final String TIMEOUT_PROPERTY = "test.threadDump.timeoutSeconds";
  private static final long REPEAT_INTERVAL_SECONDS = 60;

  @Override
  public Statement apply(Statement base, Description description) {
    String value = System.getProperty(TIMEOUT_PROPERTY);
    if (value == null || value.isEmpty()) {
      return base;
    }
    long timeoutSeconds;
    try {
      timeoutSeconds = Long.parseLong(value);
    } catch (NumberFormatException e) {
      return base;
    }
    String testName = description.getClassName() + "#" + description.getMethodName();
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        CountDownLatch stopLatch = new CountDownLatch(1);
        Thread watchdog = new Thread(() -> {
          AtomicInteger dumpCount = new AtomicInteger();
          if (awaitLatch(stopLatch, timeoutSeconds)) {
            return;
          }
          while (!awaitLatch(stopLatch, REPEAT_INTERVAL_SECONDS)) {
            dumpThreadsToFile(testName, description, dumpCount.incrementAndGet());
          }
        }, "thread-dump-watchdog-" + description.getMethodName());
        watchdog.setDaemon(true);
        watchdog.start();
        try {
          base.evaluate();
        } finally {
          stopLatch.countDown();
        }
      }
    };
  }

  private static boolean awaitLatch(CountDownLatch latch, long seconds) {
    try {
      return latch.await(seconds, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      return true;
    }
  }

  private static void dumpThreadsToFile(String testName, Description description, int dumpNumber) {
    Path reportsDir = resolveReportsDir();
    String fileName = "surefire-timeout-jstack-" +
      description.getClassName() + "-" +
      description.getMethodName() + "-" +
      dumpNumber + ".txt";
    Path dumpFile = reportsDir.resolve(fileName);
    try {
      Files.createDirectories(reportsDir);
      try (PrintStream out = new PrintStream(Files.newOutputStream(dumpFile))) {
        dumpThreads(testName, out);
      }
      System.err.println("Thread dump written to " + dumpFile);
    } catch (IOException e) {
      System.err.println("Failed to write thread dump to " + dumpFile + ": " + e);
      dumpThreads(testName, System.err);
    }
  }

  private static Path resolveReportsDir() {
    String targetDir = System.getProperty("target.dir");
    if (targetDir != null && !targetDir.isEmpty()) {
      return Paths.get(targetDir, "surefire-reports");
    }
    return Paths.get("target", "surefire-reports");
  }

  private static void dumpThreads(String testName, PrintStream out) {
    ThreadMXBean bean = ManagementFactory.getThreadMXBean();
    ThreadInfo[] threads = bean.dumpAllThreads(true, true);
    out.println();
    out.println("===== Thread dump for stuck test: " + testName + " at " + Instant.now() + " =====");
    out.println("Total threads: " + threads.length);
    out.println();
    for (ThreadInfo ti : threads) {
      printThreadInfo(ti, out);
    }
    long[] deadlocked = bean.findDeadlockedThreads();
    if (deadlocked != null) {
      out.println("*** DEADLOCK DETECTED ***");
      for (long id : deadlocked) {
        out.println("  Thread id=" + id);
      }
      out.println();
    }
    out.println("===== End thread dump =====");
    out.println();
    out.flush();
  }

  private static void printThreadInfo(ThreadInfo ti, PrintStream out) {
    out.println("\"" + ti.getThreadName() + "\"" +
      " id=" + ti.getThreadId() +
      " state=" + ti.getThreadState() +
      (ti.getLockName() != null ? " on " + ti.getLockName() : "") +
      (ti.getLockOwnerName() != null ? " owned by \"" + ti.getLockOwnerName() + "\" id=" + ti.getLockOwnerId() : ""));
    StackTraceElement[] stack = ti.getStackTrace();
    MonitorInfo[] monitors = ti.getLockedMonitors();
    for (int i = 0; i < stack.length; i++) {
      out.println("    at " + stack[i]);
      for (MonitorInfo mi : monitors) {
        if (mi.getLockedStackDepth() == i) {
          out.println("    - locked " + mi);
        }
      }
    }
    LockInfo[] locks = ti.getLockedSynchronizers();
    if (locks.length > 0) {
      out.println("    Locked synchronizers:");
      for (LockInfo li : locks) {
        out.println("      - " + li);
      }
    }
    out.println();
  }
}
