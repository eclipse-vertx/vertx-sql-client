/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

using BenchmarkDotNet.Running;
using Apex.DriverBenchmarks;

BenchmarkSwitcher.FromAssembly(typeof(Program).Assembly).Run(args);
