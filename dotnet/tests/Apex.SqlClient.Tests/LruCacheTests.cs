/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

using Apex.SqlClient.Internal;

namespace Apex.SqlClient.Tests;

[TestClass]
public sealed class LruCacheTests
{
  [TestMethod]
  public void EvictsLeastRecentlyUsedEntry()
  {
    LruCache<string, int> cache = new(2, StringComparer.Ordinal);
    Assert.IsFalse(cache.Add("one", 1, out _));
    Assert.IsFalse(cache.Add("two", 2, out _));
    Assert.IsTrue(cache.TryGet("one", out int one));
    Assert.AreEqual(1, one);

    Assert.IsTrue(cache.Add("three", 3, out int evicted));

    Assert.AreEqual(2, evicted);
    Assert.IsFalse(cache.TryGet("two", out _));
    Assert.IsTrue(cache.TryGet("one", out _));
    Assert.IsTrue(cache.TryGet("three", out _));
  }

  [TestMethod]
  public void RemovesEntry()
  {
    LruCache<string, int> cache = new(2, StringComparer.Ordinal);
    cache.Add("one", 1, out _);

    Assert.IsTrue(cache.Remove("one", out int removed));
    Assert.AreEqual(1, removed);
    Assert.IsFalse(cache.TryGet("one", out _));
  }
}
