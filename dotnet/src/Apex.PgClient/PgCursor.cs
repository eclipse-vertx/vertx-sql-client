/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

using Apex.SqlClient;

namespace Apex.PgClient;

internal sealed class PgCursor : ISqlCursor
{
  private readonly PgConnection _connection;
  private readonly string _statementName;
  private readonly string _portalName;
  private readonly SqlParameters _parameters;
  private readonly int _defaultFetchSize;
  private PgConnection.PortalPage? _initialPage;
  private bool _bound = true;
  private bool _hasMore;
  private bool _disposed;

  public PgCursor(
    PgConnection connection,
    string statementName,
    string portalName,
    SqlParameters parameters,
    int defaultFetchSize,
    PgConnection.PortalPage initialPage)
  {
    _connection = connection;
    _statementName = statementName;
    _portalName = portalName;
    _parameters = parameters;
    _defaultFetchSize = defaultFetchSize;
    _initialPage = initialPage;
    _hasMore = initialPage.HasMore;
    Columns = initialPage.Rows.Columns;
  }

  public bool HasMore => !_disposed && (_initialPage is not null || _hasMore);

  public IReadOnlyList<SqlColumn> Columns { get; private set; } = Array.Empty<SqlColumn>();

  public async ValueTask<SqlRowSet> ReadAsync(
    int count,
    CancellationToken cancellationToken = default)
  {
    ObjectDisposedException.ThrowIf(_disposed, this);
    ArgumentOutOfRangeException.ThrowIfNegativeOrZero(count);
    if (!_hasMore)
    {
      if (_initialPage is { } finalPage)
      {
        _initialPage = null;
        return finalPage.Rows;
      }

      return new SqlRowSet(Columns, [], 0, string.Empty);
    }

    if (_initialPage is { } initialPage)
    {
      _initialPage = null;
      return initialPage.Rows;
    }

    PgConnection.PortalPage page = await _connection.ReadPortalAsync(
      _portalName,
      _statementName,
      _parameters,
      Columns,
      _bound,
      count == 0 ? _defaultFetchSize : count,
      cancellationToken).ConfigureAwait(false);
    _bound = true;
    _hasMore = page.HasMore;
    Columns = page.Rows.Columns;
    return page.Rows;
  }

  public async ValueTask DisposeAsync()
  {
    if (_disposed)
    {
      return;
    }

    _disposed = true;
    if (_bound)
    {
      await _connection.ClosePortalAsync(_portalName).ConfigureAwait(false);
    }
  }
}
