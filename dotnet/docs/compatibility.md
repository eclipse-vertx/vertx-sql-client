# PostgreSQL compatibility

## Server versions

The integration matrix targets PostgreSQL 14, 16, and 18. PostgreSQL 17+ direct TLS is covered by protocol-level TLS tests and remains part of the server matrix.

## Type mappings

| PostgreSQL type | Apex type | Text | Binary | Array |
|---|---|---:|---:|---:|
| `bool` | `bool` | Yes | Yes | Yes |
| `int2`, `int4`, `int8` | `short`, `int`, `long` | Yes | Yes | Yes |
| `float4`, `float8` | `float`, `double` | Yes | Yes | Yes |
| `numeric` | `PgNumeric` | Yes | Yes | Yes |
| character, name, text-search | `string` | Yes | Yes for registered built-in OIDs | Yes for built-in arrays |
| custom enum/unknown | `string` | Yes | No; unknown binary OIDs fail explicitly | No |
| `uuid` | `Guid` | Yes | Yes | Yes |
| `date` | `DateOnly` | Yes | Yes | Yes |
| `time` | `TimeOnly` | Yes | Yes | Yes |
| `timetz` | `PgTimeWithTimeZone` | Yes | Yes | Yes |
| `timestamp` | `DateTime` with `Unspecified` kind | Yes | Yes | Yes |
| `timestamptz` | `DateTimeOffset` | Yes | Yes | Yes |
| `interval` | `PgInterval` | Yes | Yes | Yes |
| `bytea` | `byte[]` | Hex text | Yes | Yes |
| `json`, `jsonb` | `JsonElement` | Yes | Yes | Yes |
| geometric types | `PgPoint`, `PgLine`, `PgLineSegment`, `PgBox`, `PgPath`, `PgPolygon`, `PgCircle` | Yes | Yes | Yes |
| `inet`, `cidr` | `PgInet`, `PgCidr` | Yes | Yes | Yes |
| `money` | `PgMoney` | Yes | Yes | Yes |

Date and timestamp infinity values map to the corresponding .NET minimum and maximum values. One-dimensional arrays preserve SQL `NULL` elements as `null` in `object?[]`.

## Intentionally unsupported

For parity with the Vert.x PostgreSQL client, `bit`, `varbit`, `macaddr`, `macaddr8`, `xml`, `oid`, and `void` throw `PgUnsupportedTypeException`. `hstore` uses extension-assigned OIDs and requires a future type-registry lookup before it can be rejected by name.

Multidimensional arrays are not yet supported and currently throw `NotSupportedException`.
