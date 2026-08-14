/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

using System.Runtime.CompilerServices;

namespace Apex.SqlClient.Tests;

[TestClass]
public sealed class SqlClientExtensionsTests
{
  [TestMethod]
  public async Task MapsAndCollectsBufferedRows()
  {
    FakeClient client = new(CreateRows());

    IReadOnlyList<int> mapped = await client.QueryMappedAsync(
      "SELECT id",
      static row => row.Get<int>(0));
    int sum = await client.QueryCollectedAsync(
      "SELECT id",
      static rows => rows.Sum(static row => row.Get<int>(0)));

    CollectionAssert.AreEqual(new[] { 1, 2 }, mapped.ToArray());
    Assert.AreEqual(3, sum);
  }

  [TestMethod]
  public async Task MapsStreamedRows()
  {
    FakeClient client = new(CreateRows());
    List<int> mapped = [];

    await foreach (int value in client.StreamMappedAsync(
                     "SELECT id",
                     static row => row.Get<int>(0)))
    {
      mapped.Add(value);
    }

    CollectionAssert.AreEqual(new[] { 1, 2 }, mapped);
  }

  private static SqlRowSet CreateRows()
  {
    SqlColumn[] columns = [new("id", 23, 4, -1, SqlDataFormat.Text)];
    return new SqlRowSet(
      columns,
      [new SqlRow(columns, [1]), new SqlRow(columns, [2])],
      2,
      "SELECT 2");
  }

  private sealed class FakeClient : ISqlClient
  {
    private readonly SqlRowSet _rows;

    public FakeClient(SqlRowSet rows)
    {
      _rows = rows;
    }

    public ValueTask<SqlRowSet> QueryAsync(
      string sql,
      CancellationToken cancellationToken = default) =>
      ValueTask.FromResult(_rows);

    public ValueTask<SqlRowSet> QueryAsync(
      string sql,
      SqlParameters parameters,
      CancellationToken cancellationToken = default) =>
      ValueTask.FromResult(_rows);

    public ValueTask<SqlCommandResult> ExecuteAsync(
      string sql,
      CancellationToken cancellationToken = default) =>
      ValueTask.FromResult(new SqlCommandResult(0, string.Empty));

    public ValueTask<SqlCommandResult> ExecuteAsync(
      string sql,
      SqlParameters parameters,
      CancellationToken cancellationToken = default) =>
      ValueTask.FromResult(new SqlCommandResult(0, string.Empty));

    public async IAsyncEnumerable<SqlRow> StreamAsync(
      string sql,
      SqlParameters parameters = default,
      int fetchSize = 50,
      [EnumeratorCancellation] CancellationToken cancellationToken = default)
    {
      await Task.Yield();
      foreach (SqlRow row in _rows)
      {
        cancellationToken.ThrowIfCancellationRequested();
        yield return row;
      }
    }

    public ValueTask DisposeAsync() => ValueTask.CompletedTask;
  }
}
