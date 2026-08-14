/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

using System.Buffers.Binary;
using System.IO.Pipelines;
using System.Net;
using System.Net.Security;
using System.Net.Sockets;
using System.Runtime.CompilerServices;
using System.Security.Authentication;
using System.Security.Cryptography;
using System.Security.Cryptography.X509Certificates;
using Apex.PgClient.Internal;
using Apex.SqlClient;
using Apex.SqlClient.Internal;

namespace Apex.PgClient;

public sealed class PgConnection : ISqlConnection
{
  private readonly PgConnectOptions _options;
  private readonly Socket _socket;
  private readonly Stream _stream;
  private readonly PipeReader _pipeReader;
  private readonly PipeWriter _pipeWriter;
  private readonly PgWireReader _reader;
  private readonly PgWireWriter _writer;
  private readonly byte[]? _channelBindingData;
  private readonly BoundedOrderedCommandScheduler _scheduler;
  private readonly object _statementCacheGate = new();
  private readonly LruCache<string, string>? _statementCache;
  private bool _disposed;
  private int _processId;
  private int _secretKey;
  private int _statementSequence;
  private int _portalSequence;
  private byte _transactionStatus = (byte)'I';
  private DatabaseMetadata _databaseMetadata =
      new("PostgreSQL", "unknown", 0, 0);

  private PgConnection(
    PgConnectOptions options,
    Socket socket,
    Stream stream,
    bool secure,
    byte[]? channelBindingData)
  {
    _options = options;
    _socket = socket;
    _stream = stream;
    IsSecure = secure;
    _channelBindingData = channelBindingData;
    _pipeReader = PipeReader.Create(stream, new StreamPipeReaderOptions(leaveOpen: true));
    _pipeWriter = PipeWriter.Create(stream, new StreamPipeWriterOptions(leaveOpen: true));
    _reader = new PgWireReader(_pipeReader);
    _writer = new PgWireWriter(_pipeWriter);
    _scheduler = new BoundedOrderedCommandScheduler(
      options.PipeliningLimit,
      (int)Math.Max(16, Math.Min(4096, (long)options.PipeliningLimit * 4)),
      IsFatalConnectionError);
    _statementCache = options.CachePreparedStatements && options.PreparedStatementCacheSize > 0
      ? new LruCache<string, string>(
        options.PreparedStatementCacheSize,
        StringComparer.Ordinal)
      : null;
  }

  public event Action<PgNotice>? Notice;

  public event Action<PgNotification>? Notification;

  public bool IsSecure { get; }

  public DatabaseMetadata DatabaseMetadata => _databaseMetadata;

  public int ProcessId => _processId;

  public int SecretKey => _secretKey;

  internal bool IsUsable => !_disposed && !_scheduler.IsStopped && _socket.Connected;

  internal bool IsReadyForPool => IsUsable && _transactionStatus == (byte)'I';

  internal static async ValueTask<PgConnection> ConnectAsync(
      PgConnectOptions options,
      CancellationToken cancellationToken)
  {
    ValidateOptions(options);
    Socket socket = CreateSocket(options);
    try
    {
      using CancellationTokenSource timeout = CancellationTokenSource.CreateLinkedTokenSource(cancellationToken);
      timeout.CancelAfter(options.ConnectTimeout);
      Stream stream = await PgProxyConnector.ConnectAsync(socket, options, timeout.Token)
        .ConfigureAwait(false);
      bool secure = false;
      byte[]? channelBindingData = null;
      if (options.SslNegotiation == PgSslNegotiation.Direct)
      {
        if (options.SslMode == PgSslMode.Disable)
        {
          throw new InvalidOperationException("Direct SSL negotiation requires SSL to be enabled.");
        }

        stream = await UpgradeToTlsAsync(stream, options, timeout.Token).ConfigureAwait(false);
        secure = true;
        channelBindingData = GetChannelBindingData((SslStream)stream);
      }
      else if (options.SslMode is PgSslMode.Prefer or PgSslMode.Require or
               PgSslMode.VerifyCa or PgSslMode.VerifyFull)
      {
        byte response = await RequestSslAsync(stream, timeout.Token).ConfigureAwait(false);
        if (response == (byte)'S')
        {
          stream = await UpgradeToTlsAsync(stream, options, timeout.Token).ConfigureAwait(false);
          secure = true;
          channelBindingData = GetChannelBindingData((SslStream)stream);
        }
        else if (response != (byte)'N')
        {
          throw new InvalidDataException($"Unexpected PostgreSQL SSL response 0x{response:X2}.");
        }
        else if (options.SslMode is not PgSslMode.Prefer)
        {
          throw new AuthenticationException("The PostgreSQL server does not support SSL.");
        }
      }

      PgConnection connection = new(options, socket, stream, secure, channelBindingData);
      try
      {
        await connection.InitializeAsync(timeout.Token).ConfigureAwait(false);
      }
      catch (PgException exception) when (
        options.SslMode == PgSslMode.Allow &&
        IsSslRequired(exception))
      {
        await connection.DisposeAsync().ConfigureAwait(false);
        return await ConnectAsync(
          options with { SslMode = PgSslMode.Require },
          cancellationToken).ConfigureAwait(false);
      }

      return connection;
    }
    catch
    {
      socket.Dispose();
      throw;
    }
  }

  public async ValueTask<SqlRowSet> QueryAsync(
      string sql,
      CancellationToken cancellationToken = default)
  {
    ArgumentException.ThrowIfNullOrWhiteSpace(sql);
    return await ExecuteQueryCoreAsync(sql, default, cancellationToken).ConfigureAwait(false);
  }

  public async ValueTask<SqlRowSet> QueryAsync(
      string sql,
      SqlParameters parameters,
      CancellationToken cancellationToken = default)
  {
    ArgumentException.ThrowIfNullOrWhiteSpace(sql);
    return await ExecuteQueryCoreAsync(sql, parameters, cancellationToken).ConfigureAwait(false);
  }

  public async ValueTask<SqlCommandResult> ExecuteAsync(
      string sql,
      CancellationToken cancellationToken = default)
  {
    SqlRowSet result = await QueryAsync(sql, cancellationToken).ConfigureAwait(false);
    return new SqlCommandResult(result.AffectedRows, result.CommandTag);
  }

  public async ValueTask<SqlCommandResult> ExecuteAsync(
      string sql,
      SqlParameters parameters,
      CancellationToken cancellationToken = default)
  {
    SqlRowSet result = await QueryAsync(sql, parameters, cancellationToken).ConfigureAwait(false);
    return new SqlCommandResult(result.AffectedRows, result.CommandTag);
  }

  public async IAsyncEnumerable<SqlRow> StreamAsync(
      string sql,
      SqlParameters parameters = default,
      int fetchSize = 50,
      [EnumeratorCancellation] CancellationToken cancellationToken = default)
  {
    if (fetchSize <= 0)
    {
      throw new ArgumentOutOfRangeException(nameof(fetchSize));
    }

    await using ISqlPreparedStatement statement =
      await PrepareAsync(sql, cancellationToken).ConfigureAwait(false);
    await foreach (SqlRow row in statement.StreamAsync(
                     parameters,
                     fetchSize,
                     cancellationToken).ConfigureAwait(false))
    {
      yield return row;
    }
  }

  public async ValueTask<ISqlPreparedStatement> PrepareAsync(
      string sql,
      CancellationToken cancellationToken = default)
  {
    ObjectDisposedException.ThrowIf(_disposed, this);
    ArgumentException.ThrowIfNullOrWhiteSpace(sql);
    if (_options.UseLayer7Proxy && _transactionStatus != (byte)'T')
    {
      throw new InvalidOperationException(
        "Explicit prepared statements require an active transaction with a layer-7 proxy.");
    }

    string name = "A" + Interlocked.Increment(ref _statementSequence).ToString("x");
    return await _scheduler.ExecuteAsync(
      async token =>
      {
        token.ThrowIfCancellationRequested();
        await _writer.WritePrepareAsync(name, sql, CancellationToken.None).ConfigureAwait(false);
      },
      async _ =>
      {
        await ReadReadyAsync((byte)'1', CancellationToken.None).ConfigureAwait(false);
        return (ISqlPreparedStatement)new PgPreparedStatement(this, name, sql);
      },
      barrier: true,
      cancellationToken).ConfigureAwait(false);
  }

  public async ValueTask<ISqlTransaction> BeginTransactionAsync(
      CancellationToken cancellationToken = default)
  {
    ObjectDisposedException.ThrowIf(_disposed, this);
    return await _scheduler.ExecuteAsync(
      async token =>
      {
        token.ThrowIfCancellationRequested();
        if (_transactionStatus != (byte)'I')
        {
          throw new InvalidOperationException("A transaction is already active.");
        }

        await _writer.WriteQueryAsync("BEGIN", CancellationToken.None).ConfigureAwait(false);
      },
      async _ =>
      {
        await ReadQueryResultsAsync(CancellationToken.None).ConfigureAwait(false);
        return (ISqlTransaction)new PgTransaction(this);
      },
      barrier: true,
      cancellationToken).ConfigureAwait(false);
  }

  public async ValueTask CancelRequestAsync(CancellationToken cancellationToken = default)
  {
    ObjectDisposedException.ThrowIf(_disposed, this);
    using CancellationTokenSource timeout =
      CancellationTokenSource.CreateLinkedTokenSource(cancellationToken);
    timeout.CancelAfter(_options.ConnectTimeout);
    Socket cancelSocket = CreateSocket(_options);
    try
    {
      await using NetworkStream stream =
        await PgProxyConnector.ConnectAsync(cancelSocket, _options, timeout.Token)
          .ConfigureAwait(false);
      byte[] message = GC.AllocateUninitializedArray<byte>(16);
      BinaryPrimitives.WriteInt32BigEndian(message, 16);
      BinaryPrimitives.WriteInt32BigEndian(message.AsSpan(4), 80877102);
      BinaryPrimitives.WriteInt32BigEndian(message.AsSpan(8), _processId);
      BinaryPrimitives.WriteInt32BigEndian(message.AsSpan(12), _secretKey);
      await stream.WriteAsync(message, timeout.Token).ConfigureAwait(false);
      await stream.FlushAsync(timeout.Token).ConfigureAwait(false);
    }
    finally
    {
      cancelSocket.Dispose();
    }
  }

  public async ValueTask DisposeAsync()
  {
    if (_disposed)
    {
      return;
    }

    _disposed = true;
    try
    {
      await _scheduler.ExecuteAsync(
      async token =>
      {
        token.ThrowIfCancellationRequested();
        await _writer.WriteTerminateAsync(CancellationToken.None).ConfigureAwait(false);
      },
      static _ => ValueTask.FromResult(true),
      barrier: true).ConfigureAwait(false);
    }
    catch (Exception exception) when (
      !_socket.Connected ||
      IsFatalConnectionError(exception))
    {
    }
    finally
    {
      await _scheduler.DisposeAsync().ConfigureAwait(false);
      await _pipeWriter.CompleteAsync().ConfigureAwait(false);
      await _reader.CompleteAsync().ConfigureAwait(false);
      await _stream.DisposeAsync().ConfigureAwait(false);
      _socket.Dispose();
    }
  }

  private async ValueTask InitializeAsync(CancellationToken cancellationToken)
  {
    await _writer.WriteStartupAsync(_options, cancellationToken).ConfigureAwait(false);
    PgScramClient? scram = null;
    bool scramServerFinalVerified = false;

    while (true)
    {
      using PgWireMessage message = await _reader.ReadAsync(cancellationToken).ConfigureAwait(false);
      switch (message.Type)
      {
        case (byte)'R':
          PgPayloadReader authentication = new(message.Payload.Span);
          int authenticationType = authentication.ReadInt32();
          switch (authenticationType)
          {
            case 0:
              if (_options.ChannelBinding == PgChannelBinding.Require &&
                  !scramServerFinalVerified)
              {
                throw new AuthenticationException(
                  "PostgreSQL channel binding was required but authentication did not verify SCRAM-SHA-256-PLUS.");
              }

              break;
            case 3:
              RejectNonBindingAuthentication();
              await _writer.WritePasswordAsync(_options.Password, cancellationToken)
                  .ConfigureAwait(false);
              break;
            case 5:
              RejectNonBindingAuthentication();
              if (authentication.Remaining != 4)
              {
                throw new InvalidDataException("The PostgreSQL MD5 salt is invalid.");
              }

              string md5 = PgWireWriter.Md5Password(
                  _options.Password,
                  _options.Username,
                  authentication.ReadSpan(4));
              await _writer.WritePasswordAsync(md5, cancellationToken).ConfigureAwait(false);
              break;
            case 10:
              string mechanism = SelectSaslMechanism(ref authentication);
              scram = new PgScramClient(
                _options.Username,
                _options.Password,
                mechanism == "SCRAM-SHA-256-PLUS" ? _channelBindingData : null,
                advertiseChannelBinding:
                  mechanism != "SCRAM-SHA-256-PLUS" &&
                  _options.ChannelBinding == PgChannelBinding.Prefer &&
                  IsSecure);
              await _writer.WriteSaslInitialAsync(
                  mechanism,
                  scram.ClientFirstMessage,
                  cancellationToken).ConfigureAwait(false);
              break;
            case 11:
              if (scram is null)
              {
                throw new InvalidDataException("Unexpected PostgreSQL SASL continuation.");
              }

              string clientFinal = scram.HandleServerFirst(
                  authentication.ReadString(authentication.Remaining));
              await _writer.WriteSaslResponseAsync(clientFinal, cancellationToken)
                  .ConfigureAwait(false);
              break;
            case 12:
              if (scram is null)
              {
                throw new InvalidDataException("Unexpected PostgreSQL SASL completion.");
              }

              scram.HandleServerFinal(authentication.ReadString(authentication.Remaining));
              scramServerFinalVerified = true;
              break;
            default:
              throw new NotSupportedException(
                  $"PostgreSQL authentication type {authenticationType} is not supported.");
          }

          break;
        case (byte)'S':
          HandleParameterStatus(message.Payload.Span);
          break;
        case (byte)'K':
          PgPayloadReader keyData = new(message.Payload.Span);
          _processId = keyData.ReadInt32();
          _secretKey = keyData.ReadInt32();
          break;
        case (byte)'N':
          HandleNotice(message.Payload.Span);
          break;
        case (byte)'E':
          throw ParseError(message.Payload.Span);
        case (byte)'Z':
          UpdateTransactionStatus(message.Payload.Span);
          return;
        default:
          throw new InvalidDataException(
              $"Unexpected PostgreSQL startup message '{(char)message.Type}'.");
      }
    }
  }

  private void RejectNonBindingAuthentication()
  {
    if (_options.ChannelBinding == PgChannelBinding.Require)
    {
      throw new AuthenticationException(
        "PostgreSQL channel binding requires SCRAM-SHA-256-PLUS authentication.");
    }
  }

  private async ValueTask<SqlRowSet> ExecuteQueryCoreAsync(
    string sql,
    SqlParameters parameters,
    CancellationToken cancellationToken)
  {
    ObjectDisposedException.ThrowIf(_disposed, this);
    string operation = GetOperation(sql);
    using System.Diagnostics.Activity? activity = SqlClientDiagnostics.StartQuery(
      "postgresql",
      _options.Database,
      _options.Host,
      _options.Port,
      operation);
    long started = System.Diagnostics.Stopwatch.GetTimestamp();
    Exception? error = null;
    try
    {
      ObjectDisposedException.ThrowIf(_disposed, this);
      ValueTask<SqlRowSet> execution;
      bool cachedExecution = false;
      if (parameters.Count == 0)
      {
        execution = _scheduler.ExecuteAsync(
          async token =>
          {
            token.ThrowIfCancellationRequested();
            await _writer.WriteQueryAsync(sql, CancellationToken.None).ConfigureAwait(false);
          },
          _ => ReceiveQueryAsync(cancellationToken),
          barrier: cancellationToken.CanBeCanceled,
          cancellationToken: cancellationToken);
      }
      else if (_statementCache is not null &&
               sql.Length <= _options.PreparedStatementCacheSqlLengthLimit)
      {
        cachedExecution = true;
        execution = _scheduler.ExecuteAsync(
          static _ => ValueTask.CompletedTask,
          _ => PrepareCacheAndExecuteAsync(sql, parameters, cancellationToken),
          barrier: true,
          cancellationToken);
      }
      else
      {
        execution = _scheduler.ExecuteAsync(
          async token =>
          {
            token.ThrowIfCancellationRequested();
            await _writer.WriteExtendedQueryAsync(
              sql,
              parameters,
              CancellationToken.None).ConfigureAwait(false);
          },
          _ => ReceiveQueryAsync(cancellationToken),
          barrier: cancellationToken.CanBeCanceled,
          cancellationToken: cancellationToken);
      }

      try
      {
        return await execution.ConfigureAwait(false);
      }
      catch (PgException exception) when (
        cachedExecution &&
        exception.SqlState is "26000" or "0A000")
      {
        lock (_statementCacheGate)
        {
          _statementCache!.Remove(sql, out _);
        }

        return await _scheduler.ExecuteAsync(
          static _ => ValueTask.CompletedTask,
          _ => PrepareCacheAndExecuteAsync(sql, parameters, cancellationToken),
          barrier: true,
          cancellationToken).ConfigureAwait(false);
      }
    }
    catch (Exception exception)
    {
      error = exception;
      activity?.SetStatus(System.Diagnostics.ActivityStatusCode.Error, exception.Message);
      throw;
    }
    finally
    {
      SqlClientDiagnostics.RecordQuery(
        System.Diagnostics.Stopwatch.GetElapsedTime(started),
        "postgresql",
        operation,
        error);
    }
  }

  private async ValueTask<SqlRowSet> PrepareCacheAndExecuteAsync(
    string sql,
    SqlParameters parameters,
    CancellationToken cancellationToken)
  {
    cancellationToken.ThrowIfCancellationRequested();
    string? existing;
    lock (_statementCacheGate)
    {
      _statementCache!.TryGet(sql, out existing);
    }

    string name = existing ??
      "A" + Interlocked.Increment(ref _statementSequence).ToString("x");
    if (existing is null)
    {
      await _writer.WritePrepareAsync(name, sql, CancellationToken.None).ConfigureAwait(false);
      await ReadReadyAsync((byte)'1', CancellationToken.None).ConfigureAwait(false);
      string? evicted;
      lock (_statementCacheGate)
      {
        _statementCache!.Add(sql, name, out evicted);
      }

      if (evicted is not null)
      {
        await _writer.WriteCloseStatementAsync(evicted, CancellationToken.None).ConfigureAwait(false);
        await ReadReadyAsync((byte)'3', CancellationToken.None).ConfigureAwait(false);
      }
    }

    await _writer.WritePreparedQueryAsync(name, parameters, CancellationToken.None).ConfigureAwait(false);
    return await ReceiveQueryAsync(cancellationToken).ConfigureAwait(false);
  }

  private async ValueTask<SqlRowSet> ReceiveQueryAsync(CancellationToken cancellationToken)
  {
    Task? cancellationRequest = null;
    using CancellationTokenRegistration registration = cancellationToken.Register(
      () => cancellationRequest = TryCancelRequestAsync());
    try
    {
      SqlRowSet result = await ReadQueryResultsAsync(CancellationToken.None).ConfigureAwait(false);
      if (cancellationRequest is not null)
      {
        await cancellationRequest.ConfigureAwait(false);
      }

      cancellationToken.ThrowIfCancellationRequested();
      return result;
    }
    catch (PgException) when (cancellationToken.IsCancellationRequested)
    {
      if (cancellationRequest is not null)
      {
        await cancellationRequest.ConfigureAwait(false);
      }

      throw new OperationCanceledException(cancellationToken);
    }
  }

  internal async ValueTask<SqlRowSet> ExecutePreparedAsync(
    string name,
    SqlParameters parameters,
    CancellationToken cancellationToken)
  {
    ObjectDisposedException.ThrowIf(_disposed, this);
    return await _scheduler.ExecuteAsync(
      async token =>
      {
        token.ThrowIfCancellationRequested();
        await _writer.WritePreparedQueryAsync(
          name,
          parameters,
          CancellationToken.None).ConfigureAwait(false);
      },
      _ => ReceiveQueryAsync(cancellationToken),
      barrier: cancellationToken.CanBeCanceled,
      cancellationToken: cancellationToken).ConfigureAwait(false);
  }

  internal async ValueTask ExecuteTransactionControlAsync(
      string sql,
      CancellationToken cancellationToken)
  {
    ObjectDisposedException.ThrowIf(_disposed, this);
    await _scheduler.ExecuteAsync(
      async token =>
      {
        token.ThrowIfCancellationRequested();
        await _writer.WriteQueryAsync(sql, CancellationToken.None).ConfigureAwait(false);
      },
      async _ =>
      {
        await ReadQueryResultsAsync(CancellationToken.None).ConfigureAwait(false);
        return true;
      },
      barrier: true,
      cancellationToken).ConfigureAwait(false);
  }

  internal async ValueTask ClosePreparedAsync(string name)
  {
    if (_disposed)
    {
      return;
    }

    await _scheduler.ExecuteAsync(
      async token =>
      {
        token.ThrowIfCancellationRequested();
        await _writer.WriteCloseStatementAsync(name, CancellationToken.None).ConfigureAwait(false);
      },
      async _ =>
      {
        await ReadReadyAsync((byte)'3', CancellationToken.None).ConfigureAwait(false);
        return true;
      },
      barrier: true).ConfigureAwait(false);
  }

  internal async ValueTask<ISqlCursor> CreateCursorAsync(
    string statementName,
    SqlParameters parameters,
    int fetchSize,
    CancellationToken cancellationToken)
  {
    ObjectDisposedException.ThrowIf(_disposed, this);
    if (_transactionStatus != (byte)'T')
    {
      throw new InvalidOperationException(
        "PostgreSQL cursors require an active transaction.");
    }

    string portalName = "P" + Interlocked.Increment(ref _portalSequence).ToString("x");
    PortalPage initialPage = await ReadPortalAsync(
      portalName,
      statementName,
      parameters,
      Array.Empty<SqlColumn>(),
      bound: false,
      fetchSize,
      cancellationToken).ConfigureAwait(false);
    return new PgCursor(
      this,
      statementName,
      portalName,
      parameters,
      fetchSize,
      initialPage);
  }

  internal async ValueTask<PortalPage> ReadPortalAsync(
    string portalName,
    string statementName,
    SqlParameters parameters,
    IReadOnlyList<SqlColumn> columns,
    bool bound,
    int fetchSize,
    CancellationToken cancellationToken)
  {
    ObjectDisposedException.ThrowIf(_disposed, this);
    return await _scheduler.ExecuteAsync(
      async token =>
      {
        token.ThrowIfCancellationRequested();
        if (bound)
        {
          await _writer.WriteExecutePortalAsync(
            portalName,
            fetchSize,
            CancellationToken.None).ConfigureAwait(false);
        }
        else
        {
          await _writer.WriteOpenPortalAsync(
            portalName,
            statementName,
            parameters,
            fetchSize,
            CancellationToken.None).ConfigureAwait(false);
        }
      },
      _ => ReadPortalPageAsync(columns, CancellationToken.None),
      barrier: true,
      cancellationToken).ConfigureAwait(false);
  }

  internal async ValueTask ClosePortalAsync(string portalName)
  {
    if (_disposed)
    {
      return;
    }

    await _scheduler.ExecuteAsync(
      async token =>
      {
        token.ThrowIfCancellationRequested();
        await _writer.WriteClosePortalAsync(
          portalName,
          CancellationToken.None).ConfigureAwait(false);
      },
      async _ =>
      {
        await ReadReadyAsync((byte)'3', CancellationToken.None).ConfigureAwait(false);
        return true;
      },
      barrier: true).ConfigureAwait(false);
  }

  internal async ValueTask<PgNotification> WaitForNotificationAsync(
    CancellationToken cancellationToken)
  {
    ObjectDisposedException.ThrowIf(_disposed, this);
    return await _scheduler.ExecuteAsync(
      static _ => ValueTask.CompletedTask,
      ReadNotificationAsync,
      barrier: true,
      cancellationToken).ConfigureAwait(false);
  }

  internal async IAsyncEnumerable<SqlRow> StreamPreparedAsync(
    string statementName,
    SqlParameters parameters,
    int fetchSize,
    [EnumeratorCancellation] CancellationToken cancellationToken)
  {
    ISqlTransaction? transaction = null;
    if (_transactionStatus == (byte)'I')
    {
      transaction = await BeginTransactionAsync(cancellationToken).ConfigureAwait(false);
    }

    try
    {
      await using ISqlCursor cursor = await CreateCursorAsync(
        statementName,
        parameters,
        fetchSize,
        cancellationToken).ConfigureAwait(false);
      while (cursor.HasMore)
      {
        SqlRowSet page = await cursor.ReadAsync(fetchSize, cancellationToken).ConfigureAwait(false);
        foreach (SqlRow row in page)
        {
          cancellationToken.ThrowIfCancellationRequested();
          yield return row;
        }
      }

      if (transaction is not null)
      {
        await transaction.CommitAsync(cancellationToken).ConfigureAwait(false);
      }
    }
    finally
    {
      if (transaction is not null)
      {
        await transaction.DisposeAsync().ConfigureAwait(false);
      }
    }
  }

  private async Task TryCancelRequestAsync()
  {
    try
    {
      await CancelRequestAsync(CancellationToken.None).ConfigureAwait(false);
    }

    catch (Exception)
    {
      _socket.Dispose();
    }
  }

  private async ValueTask<PgNotification> ReadNotificationAsync(
    CancellationToken cancellationToken)
  {
    while (true)
    {
      using PgWireMessage message = await _reader.ReadAsync(cancellationToken).ConfigureAwait(false);
      switch (message.Type)
      {
        case (byte)'A':
          return HandleNotification(message.Payload.Span);
        case (byte)'N':
          HandleNotice(message.Payload.Span);
          break;
        case (byte)'S':
          HandleParameterStatus(message.Payload.Span);
          break;
        case (byte)'E':
          throw ParseError(message.Payload.Span);
        default:
          throw new InvalidDataException(
            $"Unexpected idle PostgreSQL message '{(char)message.Type}'.");
      }
    }
  }

  private async ValueTask<SqlRowSet> ReadQueryResultsAsync(CancellationToken cancellationToken)
  {
    List<ResultBuilder> results = [];
    ResultBuilder current = new();
    PgException? error = null;

    while (true)
    {
      using PgWireMessage message = await _reader.ReadAsync(cancellationToken).ConfigureAwait(false);
      switch (message.Type)
      {
        case (byte)'T':
          current.SetColumns(ParseColumns(message.Payload.Span));
          break;
        case (byte)'D':
          current.AddRow(ParseRow(message.Payload.Span, current.Columns));
          break;
        case (byte)'C':
          current.Complete(ParseCommandTag(message.Payload.Span));
          results.Add(current);
          current = new ResultBuilder();
          break;
        case (byte)'I':
          current.Complete(string.Empty);
          results.Add(current);
          current = new ResultBuilder();
          break;
        case (byte)'E':
          error = ParseError(message.Payload.Span);
          break;
        case (byte)'N':
          HandleNotice(message.Payload.Span);
          break;
        case (byte)'S':
          HandleParameterStatus(message.Payload.Span);
          break;
        case (byte)'A':
          HandleNotification(message.Payload.Span);
          break;
        case (byte)'1':
        case (byte)'2':
        case (byte)'3':
        case (byte)'n':
        case (byte)'t':
          break;
        case (byte)'Z':
          UpdateTransactionStatus(message.Payload.Span);
          if (error is not null)
          {
            throw error;
          }

          return BuildResultChain(results);
        default:
          throw new InvalidDataException(
            $"Unexpected PostgreSQL query message '{(char)message.Type}'.");
      }
    }
  }

  private async ValueTask<PortalPage> ReadPortalPageAsync(
    IReadOnlyList<SqlColumn> existingColumns,
    CancellationToken cancellationToken)
  {
    IReadOnlyList<SqlColumn> columns = existingColumns;
    List<SqlRow> rows = [];
    string commandTag = string.Empty;
    bool hasMore = false;
    bool completed = false;
    PgException? error = null;
    while (true)
    {
      using PgWireMessage message = await _reader.ReadAsync(cancellationToken).ConfigureAwait(false);
      switch (message.Type)
      {
        case (byte)'T':
          columns = ParseColumns(message.Payload.Span);
          break;
        case (byte)'D':
          rows.Add(ParseRow(message.Payload.Span, columns));
          break;
        case (byte)'C':
          commandTag = ParseCommandTag(message.Payload.Span);
          completed = true;
          break;
        case (byte)'s':
          hasMore = true;
          completed = true;
          break;
        case (byte)'2':
        case (byte)'n':
          break;
        case (byte)'E':
          error = ParseError(message.Payload.Span);
          break;
        case (byte)'N':
          HandleNotice(message.Payload.Span);
          break;
        case (byte)'S':
          HandleParameterStatus(message.Payload.Span);
          break;
        case (byte)'A':
          HandleNotification(message.Payload.Span);
          break;
        case (byte)'Z':
          UpdateTransactionStatus(message.Payload.Span);
          if (error is not null)
          {
            throw error;
          }

          if (!completed)
          {
            throw new InvalidDataException("PostgreSQL portal execution did not complete.");
          }

          return new PortalPage(
            new SqlRowSet(
              columns,
              rows.ToArray(),
              ParseAffectedRows(commandTag),
              commandTag),
            hasMore);
        default:
          throw new InvalidDataException(
            $"Unexpected PostgreSQL portal message '{(char)message.Type}'.");
      }
    }
  }

  private async ValueTask ReadReadyAsync(
      byte expectedCompletion,
      CancellationToken cancellationToken)
  {
    bool completed = false;
    PgException? error = null;
    while (true)
    {
      using PgWireMessage message = await _reader.ReadAsync(cancellationToken).ConfigureAwait(false);
      switch (message.Type)
      {
        case var type when type == expectedCompletion:
          completed = true;
          break;
        case (byte)'E':
          error = ParseError(message.Payload.Span);
          break;
        case (byte)'N':
          HandleNotice(message.Payload.Span);
          break;
        case (byte)'S':
          HandleParameterStatus(message.Payload.Span);
          break;
        case (byte)'A':
          HandleNotification(message.Payload.Span);
          break;
        case (byte)'Z':
          UpdateTransactionStatus(message.Payload.Span);
          if (error is not null)
          {
            throw error;
          }

          if (!completed)
          {
            throw new InvalidDataException(
                $"PostgreSQL did not send completion '{(char)expectedCompletion}'.");
          }

          return;
        default:
          throw new InvalidDataException(
              $"Unexpected PostgreSQL control message '{(char)message.Type}'.");
      }
    }
  }

  private static IReadOnlyList<SqlColumn> ParseColumns(ReadOnlySpan<byte> payload)
  {
    PgPayloadReader reader = new(payload);
    int count = reader.ReadInt16();
    SqlColumn[] columns = new SqlColumn[count];
    for (int i = 0; i < count; i++)
    {
      string name = reader.ReadCString();
      _ = reader.ReadInt32();
      _ = reader.ReadInt16();
      uint typeId = unchecked((uint)reader.ReadInt32());
      short typeSize = reader.ReadInt16();
      int typeModifier = reader.ReadInt32();
      SqlDataFormat format = (SqlDataFormat)reader.ReadInt16();
      columns[i] = new SqlColumn(name, typeId, typeSize, typeModifier, format);
    }

    return columns;
  }

  private static SqlRow ParseRow(
      ReadOnlySpan<byte> payload,
      IReadOnlyList<SqlColumn> columns)
  {
    PgPayloadReader reader = new(payload);
    int count = reader.ReadInt16();
    if (count != columns.Count)
    {
      throw new InvalidDataException(
          $"PostgreSQL row has {count} values but {columns.Count} columns were described.");
    }

    object?[] values = new object?[count];
    for (int i = 0; i < count; i++)
    {
      int length = reader.ReadInt32();
      try
      {
        values[i] = length < 0
          ? null
          : DecodeColumn(columns[i], reader.ReadSpan(length));
      }
      catch (Exception exception)
      {
        throw new InvalidDataException(
          $"Failed to decode PostgreSQL column {i} with type OID {columns[i].TypeId}.",
          exception);
      }
    }

    return new SqlRow(columns, values);
  }

  private static string ParseCommandTag(ReadOnlySpan<byte> payload)
  {
    PgPayloadReader reader = new(payload);
    return reader.ReadCString();
  }

  private static object DecodeColumn(SqlColumn column, ReadOnlySpan<byte> value) =>
    column.Format == SqlDataFormat.Binary
      ? PgBinaryCodec.Decode(column.TypeId, value)
      : PgTextCodec.Decode(column.TypeId, value);

  private static SqlRowSet BuildResultChain(IReadOnlyList<ResultBuilder> builders)
  {
    if (builders.Count == 0)
    {
      return SqlRowSet.Empty;
    }

    SqlRowSet? next = null;
    for (int i = builders.Count - 1; i >= 0; i--)
    {
      next = builders[i].Build(next);
    }

    return next!;
  }

  private string SelectSaslMechanism(ref PgPayloadReader reader)
  {
    bool supportsScram = false;
    bool supportsScramPlus = false;
    while (reader.Remaining > 0)
    {
      string mechanism = reader.ReadCString();
      if (mechanism.Length == 0)
      {
        break;
      }

      supportsScram |= mechanism == "SCRAM-SHA-256";
      supportsScramPlus |= mechanism == "SCRAM-SHA-256-PLUS";
    }

    if (_options.ChannelBinding != PgChannelBinding.Disable &&
          IsSecure &&
          _channelBindingData is not null &&
          supportsScramPlus)
    {
      return "SCRAM-SHA-256-PLUS";
    }

    if (_options.ChannelBinding == PgChannelBinding.Require)
    {
      throw new AuthenticationException(
        "PostgreSQL channel binding is required but SCRAM-SHA-256-PLUS is unavailable.");
    }

    return supportsScram
        ? "SCRAM-SHA-256"
        : throw new NotSupportedException("The server does not offer SCRAM-SHA-256.");
  }

  private void HandleParameterStatus(ReadOnlySpan<byte> payload)
  {
    PgPayloadReader reader = new(payload);
    string name = reader.ReadCString();
    string value = reader.ReadCString();
    if (name == "server_version")
    {
      string numeric = value.Split(' ', '-', StringSplitOptions.RemoveEmptyEntries)[0];
      string[] parts = numeric.Split('.');
      int major = int.TryParse(parts.ElementAtOrDefault(0), out int parsedMajor) ? parsedMajor : 0;
      int minor = int.TryParse(parts.ElementAtOrDefault(1), out int parsedMinor) ? parsedMinor : 0;
      _databaseMetadata = new DatabaseMetadata("PostgreSQL", value, major, minor);
    }
  }

  private void HandleNotice(ReadOnlySpan<byte> payload)
  {
    IReadOnlyDictionary<char, string> fields = ParseErrorFields(payload);
    PgNotice notice = new(
        Get(fields, 'M') ?? "PostgreSQL notice",
        Get(fields, 'V') ?? Get(fields, 'S'),
        Get(fields, 'C'),
        Get(fields, 'D'),
        Get(fields, 'H'));
    InvokeSafely(Notice, notice);
  }

  private PgNotification HandleNotification(ReadOnlySpan<byte> payload)
  {
    PgPayloadReader reader = new(payload);
    PgNotification notification = new(
        reader.ReadInt32(),
        reader.ReadCString(),
        reader.ReadCString());
    InvokeSafely(Notification, notification);
    return notification;
  }

  private static void InvokeSafely<T>(Action<T>? handlers, T value)
  {
    if (handlers is null)
    {
      return;
    }

    foreach (Action<T> handler in handlers.GetInvocationList().Cast<Action<T>>())
    {
      try
      {
        handler(value);
      }
      catch (Exception exception)
      {
        System.Diagnostics.Trace.TraceError(
          "Apex SQL client event handler failed: {0}",
          exception);
      }
    }
  }

  private static PgException ParseError(ReadOnlySpan<byte> payload) =>
      new(ParseErrorFields(payload));

  private static IReadOnlyDictionary<char, string> ParseErrorFields(ReadOnlySpan<byte> payload)
  {
    PgPayloadReader reader = new(payload);
    Dictionary<char, string> fields = [];
    while (reader.Remaining > 0)
    {
      char type = (char)reader.ReadByte();
      if (type == '\0')
      {
        break;
      }

      fields[type] = reader.ReadCString();
    }

    return fields;
  }

  private static string? Get(IReadOnlyDictionary<char, string> fields, char key) =>
      fields.TryGetValue(key, out string? value) ? value : null;

  private void UpdateTransactionStatus(ReadOnlySpan<byte> payload)
  {
    if (payload.Length != 1 ||
        payload[0] is not ((byte)'I' or (byte)'T' or (byte)'E'))
    {
      throw new InvalidDataException("The PostgreSQL transaction status is invalid.");
    }

    _transactionStatus = payload[0];
  }

  private static string GetOperation(string sql)
  {
    ReadOnlySpan<char> text = sql.AsSpan().TrimStart();
    int separator = text.IndexOfAny(" \t\r\n");
    return (separator < 0 ? text : text[..separator]).ToString().ToUpperInvariant();
  }

  internal static bool IsFatalConnectionError(Exception exception) =>
    exception is IOException or
      SocketException or
      InvalidDataException or
      AuthenticationException or
      ObjectDisposedException or
      PgException { SqlState: "57P01" or "57P02" or "57P03" or "08006" };

  private static long ParseAffectedRows(string commandTag)
  {
    ReadOnlySpan<char> tag = commandTag.AsSpan();
    int lastSpace = tag.LastIndexOf(' ');
    return lastSpace >= 0 &&
           long.TryParse(tag[(lastSpace + 1)..], out long affected)
      ? affected
      : 0;
  }

  private static Socket CreateSocket(PgConnectOptions options)
  {
    if (IsUnixSocket(options))
    {
      return new Socket(AddressFamily.Unix, SocketType.Stream, ProtocolType.Unspecified);
    }

    return new Socket(AddressFamily.InterNetworkV6, SocketType.Stream, ProtocolType.Tcp)
    {
      NoDelay = true,
      DualMode = true,
    };
  }

  private static bool IsUnixSocket(PgConnectOptions options) =>
      options.Host.Length > 0 && options.Host[0] == '/';

  private static async ValueTask<byte> RequestSslAsync(
      Stream stream,
      CancellationToken cancellationToken)
  {
    byte[] request = new byte[8];
    BinaryPrimitives.WriteInt32BigEndian(request, 8);
    BinaryPrimitives.WriteInt32BigEndian(request.AsSpan(4), 80877103);
    await stream.WriteAsync(request, cancellationToken).ConfigureAwait(false);
    await stream.FlushAsync(cancellationToken).ConfigureAwait(false);
    byte[] response = new byte[1];
    await stream.ReadExactlyAsync(response, cancellationToken).ConfigureAwait(false);
    return response[0];
  }

  private static async ValueTask<Stream> UpgradeToTlsAsync(
      Stream stream,
      PgConnectOptions options,
      CancellationToken cancellationToken)
  {
    RemoteCertificateValidationCallback? validation =
        options.CertificateValidationCallback ??
        options.SslMode switch
        {
          PgSslMode.Require or PgSslMode.Prefer => static (_, _, _, _) => true,
          PgSslMode.VerifyCa => VerifyCertificateAuthority,
          _ => null,
        };

    SslStream ssl = new(stream, leaveInnerStreamOpen: false, validation);
    X509CertificateCollection? clientCertificates = options.ClientCertificates.Count == 0
        ? null
        : new X509CertificateCollection(options.ClientCertificates.ToArray());
    SslClientAuthenticationOptions authenticationOptions = new()
    {
      TargetHost = options.Host,
      EnabledSslProtocols = SslProtocols.None,
      ClientCertificates = clientCertificates,
      CertificateRevocationCheckMode = options.CertificateRevocationCheckMode,
    };
    if (options.SslNegotiation == PgSslNegotiation.Direct)
    {
      authenticationOptions.ApplicationProtocols =
        [new SslApplicationProtocol("postgresql")];
    }

    await ssl.AuthenticateAsClientAsync(
        authenticationOptions,
        cancellationToken).ConfigureAwait(false);
    if (options.SslNegotiation == PgSslNegotiation.Direct &&
          ssl.NegotiatedApplicationProtocol != new SslApplicationProtocol("postgresql"))
    {
      throw new AuthenticationException(
        "PostgreSQL direct TLS did not negotiate the required 'postgresql' ALPN protocol.");
    }
    return ssl;
  }

  private static bool VerifyCertificateAuthority(
      object sender,
      X509Certificate? certificate,
      X509Chain? chain,
      SslPolicyErrors errors) =>
      certificate is not null &&
      chain is not null &&
      (errors & ~SslPolicyErrors.RemoteCertificateNameMismatch) == SslPolicyErrors.None;

  private static byte[] GetChannelBindingData(SslStream stream)
  {
    X509Certificate certificate = stream.RemoteCertificate ??
      throw new AuthenticationException(
        "The TLS server certificate is unavailable for PostgreSQL channel binding.");
    X509Certificate2 certificate2 =
      X509CertificateLoader.LoadCertificate(certificate.GetRawCertData());
    string signatureAlgorithm = certificate2.SignatureAlgorithm.Value ?? string.Empty;
    return signatureAlgorithm switch
    {
      "1.2.840.113549.1.1.12" or "1.2.840.10045.4.3.3" =>
        SHA384.HashData(certificate2.RawData),
      "1.2.840.113549.1.1.13" or "1.2.840.10045.4.3.4" =>
        SHA512.HashData(certificate2.RawData),
      _ => SHA256.HashData(certificate2.RawData),
    };
  }

  private static bool IsSslRequired(PgException exception) =>
    exception.SqlState == "28000" &&
    (exception.Message.Contains("SSL off", StringComparison.OrdinalIgnoreCase) ||
     exception.Message.Contains("no encryption", StringComparison.OrdinalIgnoreCase));

  private static void ValidateOptions(PgConnectOptions options)
  {
    ArgumentException.ThrowIfNullOrWhiteSpace(options.Host);
    ArgumentException.ThrowIfNullOrWhiteSpace(options.Username);
    ArgumentException.ThrowIfNullOrWhiteSpace(options.Database);
    ArgumentOutOfRangeException.ThrowIfNegativeOrZero(options.Port);
    if (options.Port > ushort.MaxValue)
    {
      throw new ArgumentOutOfRangeException(nameof(options), "Port must be at most 65535.");
    }

    ArgumentOutOfRangeException.ThrowIfNegativeOrZero(options.PipeliningLimit);
    ArgumentOutOfRangeException.ThrowIfNegative(options.PreparedStatementCacheSize);
    if (options.Proxy is not null)
    {
      ArgumentException.ThrowIfNullOrWhiteSpace(options.Proxy.Host);
      ArgumentOutOfRangeException.ThrowIfNegativeOrZero(options.Proxy.Port);
      if (IsUnixSocket(options))
      {
        throw new ArgumentException(
          "Proxy transport cannot target a Unix domain socket.",
          nameof(options));
      }
    }

    if (options.UseLayer7Proxy && options.CachePreparedStatements)
    {
      throw new ArgumentException(
        "Prepared statement caching must be disabled with a layer-7 proxy.",
        nameof(options));
    }
    if (options.ChannelBinding == PgChannelBinding.Require &&
        options.SslMode == PgSslMode.Disable)
    {
      throw new ArgumentException(
        "PostgreSQL channel binding requires SSL.",
        nameof(options));
    }

    if (options.SslNegotiation == PgSslNegotiation.Direct &&
        options.SslMode is PgSslMode.Disable or PgSslMode.Allow or PgSslMode.Prefer)
    {
      throw new ArgumentException(
        "PostgreSQL direct TLS requires Require, VerifyCa, or VerifyFull SSL mode.",
        nameof(options));
    }
  }

  private sealed class ResultBuilder
  {
    private readonly List<SqlRow> _rows = [];
    private string _commandTag = string.Empty;

    public IReadOnlyList<SqlColumn> Columns { get; private set; } = Array.Empty<SqlColumn>();

    public void SetColumns(IReadOnlyList<SqlColumn> columns) => Columns = columns;

    public void AddRow(SqlRow row) => _rows.Add(row);

    public void Complete(string commandTag) => _commandTag = commandTag;

    public SqlRowSet Build(SqlRowSet? next)
    {
      long affectedRows = PgConnection.ParseAffectedRows(_commandTag);
      return new SqlRowSet(Columns, _rows.ToArray(), affectedRows, _commandTag, next);
    }

  }

  internal readonly record struct PortalPage(SqlRowSet Rows, bool HasMore);
}
