/*
 * Copyright (C) 2017 Julien Viet
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
 *
 */

package io.reactiverse.pgclient;

import org.openjdk.jmh.runner.*;
import org.openjdk.jmh.runner.options.*;

import java.util.Arrays;

public class Main {

  public static void main(String[] argv) throws Exception {
    PgConnectOptions options = PgTestBase.startPg();
    int len = argv.length;
    argv = Arrays.copyOf(argv, len + 10);
    argv[len] = "-p";
    argv[len + 1] = "host=" + options.getHost();
    argv[len + 2] = "-p";
    argv[len + 3] = "port=" + options.getPort();
    argv[len + 4] = "-p";
    argv[len + 5] = "database=" + options.getDatabase();
    argv[len + 6] = "-p";
    argv[len + 7] = "username=" + options.getUser();
    argv[len + 8] = "-p";
    argv[len + 9] = "password=" + options.getPassword();
    try {
      CommandLineOptions cmdOptions = new CommandLineOptions(argv);


      Runner runner = new Runner(cmdOptions);

      if (cmdOptions.shouldHelp()) {
        cmdOptions.showHelp();
        return;
      }

      if (cmdOptions.shouldList()) {
        runner.list();
        return;
      }

      if (cmdOptions.shouldListWithParams()) {
        runner.listWithParams(cmdOptions);
        return;
      }

      if (cmdOptions.shouldListProfilers()) {
        cmdOptions.listProfilers();
        return;
      }

      if (cmdOptions.shouldListResultFormats()) {
        cmdOptions.listResultFormats();
        return;
      }

      /*
      Options opt = new OptionsBuilder()
        .include(SimpleBenchmark.class.getSimpleName())
        .warmupIterations(5)
        .measurementIterations(5)
        .forks(1)
        .build();
      new Runner(opt).run();
      */

      try {
        runner.run();
      } catch (NoBenchmarksException e) {
        System.err.println("No matching benchmarks. Miss-spelled regexp?");

        if (cmdOptions.verbosity().orElse(Defaults.VERBOSITY) != VerboseMode.EXTRA) {
          System.err.println("Use " + VerboseMode.EXTRA + " verbose mode to debug the pattern matching.");
        } else {
          runner.list();
        }
        // System.exit(1);
      } catch (ProfilersFailedException e) {
        // This is not exactly an error, set non-zero exit code
        System.err.println(e.getMessage());
        // System.exit(1);
      } catch (RunnerException e) {
        System.err.print("ERROR: ");
        e.printStackTrace(System.err);
        // System.exit(1);
      }

    } catch (CommandLineOptionException e) {
      System.err.println("Error parsing command line:");
      System.err.println(" " + e.getMessage());
      // System.exit(1);
    } finally {
      PgTestBase.stopPg();
    }
  }
}
