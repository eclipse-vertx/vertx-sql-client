set allow_suspicious_low_cardinality_types=true;
DROP TABLE IF EXISTS vertx_test_int8;
CREATE TABLE vertx_test_int8 (
    id Int8,
    simple_t Int8,
    nullable_t Nullable(Int8),
    array_t Array(Int8),
    array3_t Array(Array(Array(Int8))),
    nullable_array_t Array(Nullable(Int8)),
    nullable_array3_t Array(Array(Array(Nullable(Int8)))),
    simple_lc_t LowCardinality(Int8),
    nullable_lc_t LowCardinality(Nullable(Int8)),
    array_lc_t Array(LowCardinality(Int8)),
    array3_lc_t Array(Array(Array(LowCardinality(Int8)))),
    nullable_array_lc_t Array(LowCardinality(Nullable(Int8))),
    nullable_array3_lc_t Array(Array(Array(LowCardinality(Nullable(Int8)))))
) engine = MergeTree()
        ORDER BY (id);
DROP TABLE IF EXISTS vertx_test_uint8;
CREATE TABLE vertx_test_uint8 (
    id Int8,
    simple_t UInt8,
    nullable_t Nullable(UInt8),
    array_t Array(UInt8),
    array3_t Array(Array(Array(UInt8))),
    nullable_array_t Array(Nullable(UInt8)),
    nullable_array3_t Array(Array(Array(Nullable(UInt8)))),
    simple_lc_t LowCardinality(UInt8),
    nullable_lc_t LowCardinality(Nullable(UInt8)),
    array_lc_t Array(LowCardinality(UInt8)),
    array3_lc_t Array(Array(Array(LowCardinality(UInt8)))),
    nullable_array_lc_t Array(LowCardinality(Nullable(UInt8))),
    nullable_array3_lc_t Array(Array(Array(LowCardinality(Nullable(UInt8)))))
) engine = MergeTree()
        ORDER BY (id);
DROP TABLE IF EXISTS vertx_test_int16;
CREATE TABLE vertx_test_int16 (
    id Int8,
    simple_t Int16,
    nullable_t Nullable(Int16),
    array_t Array(Int16),
    array3_t Array(Array(Array(Int16))),
    nullable_array_t Array(Nullable(Int16)),
    nullable_array3_t Array(Array(Array(Nullable(Int16)))),
    simple_lc_t LowCardinality(Int16),
    nullable_lc_t LowCardinality(Nullable(Int16)),
    array_lc_t Array(LowCardinality(Int16)),
    array3_lc_t Array(Array(Array(LowCardinality(Int16)))),
    nullable_array_lc_t Array(LowCardinality(Nullable(Int16))),
    nullable_array3_lc_t Array(Array(Array(LowCardinality(Nullable(Int16)))))
) engine = MergeTree()
        ORDER BY (id);
DROP TABLE IF EXISTS vertx_test_uint16;
CREATE TABLE vertx_test_uint16 (
    id Int8,
    simple_t UInt16,
    nullable_t Nullable(UInt16),
    array_t Array(UInt16),
    array3_t Array(Array(Array(UInt16))),
    nullable_array_t Array(Nullable(UInt16)),
    nullable_array3_t Array(Array(Array(Nullable(UInt16)))),
    simple_lc_t LowCardinality(UInt16),
    nullable_lc_t LowCardinality(Nullable(UInt16)),
    array_lc_t Array(LowCardinality(UInt16)),
    array3_lc_t Array(Array(Array(LowCardinality(UInt16)))),
    nullable_array_lc_t Array(LowCardinality(Nullable(UInt16))),
    nullable_array3_lc_t Array(Array(Array(LowCardinality(Nullable(UInt16)))))
) engine = MergeTree()
        ORDER BY (id);
DROP TABLE IF EXISTS vertx_test_int32;
CREATE TABLE vertx_test_int32 (
    id Int8,
    simple_t Int32,
    nullable_t Nullable(Int32),
    array_t Array(Int32),
    array3_t Array(Array(Array(Int32))),
    nullable_array_t Array(Nullable(Int32)),
    nullable_array3_t Array(Array(Array(Nullable(Int32)))),
    simple_lc_t LowCardinality(Int32),
    nullable_lc_t LowCardinality(Nullable(Int32)),
    array_lc_t Array(LowCardinality(Int32)),
    array3_lc_t Array(Array(Array(LowCardinality(Int32)))),
    nullable_array_lc_t Array(LowCardinality(Nullable(Int32))),
    nullable_array3_lc_t Array(Array(Array(LowCardinality(Nullable(Int32)))))
) engine = MergeTree()
        ORDER BY (id);
DROP TABLE IF EXISTS vertx_test_uint32;
CREATE TABLE vertx_test_uint32 (
    id Int8,
    simple_t UInt32,
    nullable_t Nullable(UInt32),
    array_t Array(UInt32),
    array3_t Array(Array(Array(UInt32))),
    nullable_array_t Array(Nullable(UInt32)),
    nullable_array3_t Array(Array(Array(Nullable(UInt32)))),
    simple_lc_t LowCardinality(UInt32),
    nullable_lc_t LowCardinality(Nullable(UInt32)),
    array_lc_t Array(LowCardinality(UInt32)),
    array3_lc_t Array(Array(Array(LowCardinality(UInt32)))),
    nullable_array_lc_t Array(LowCardinality(Nullable(UInt32))),
    nullable_array3_lc_t Array(Array(Array(LowCardinality(Nullable(UInt32)))))
) engine = MergeTree()
        ORDER BY (id);
DROP TABLE IF EXISTS vertx_test_int64;
CREATE TABLE vertx_test_int64 (
    id Int8,
    simple_t Int64,
    nullable_t Nullable(Int64),
    array_t Array(Int64),
    array3_t Array(Array(Array(Int64))),
    nullable_array_t Array(Nullable(Int64)),
    nullable_array3_t Array(Array(Array(Nullable(Int64)))),
    simple_lc_t LowCardinality(Int64),
    nullable_lc_t LowCardinality(Nullable(Int64)),
    array_lc_t Array(LowCardinality(Int64)),
    array3_lc_t Array(Array(Array(LowCardinality(Int64)))),
    nullable_array_lc_t Array(LowCardinality(Nullable(Int64))),
    nullable_array3_lc_t Array(Array(Array(LowCardinality(Nullable(Int64)))))
) engine = MergeTree()
        ORDER BY (id);
DROP TABLE IF EXISTS vertx_test_uint64;
CREATE TABLE vertx_test_uint64 (
    id Int8,
    simple_t UInt64,
    nullable_t Nullable(UInt64),
    array_t Array(UInt64),
    array3_t Array(Array(Array(UInt64))),
    nullable_array_t Array(Nullable(UInt64)),
    nullable_array3_t Array(Array(Array(Nullable(UInt64)))),
    simple_lc_t LowCardinality(UInt64),
    nullable_lc_t LowCardinality(Nullable(UInt64)),
    array_lc_t Array(LowCardinality(UInt64)),
    array3_lc_t Array(Array(Array(LowCardinality(UInt64)))),
    nullable_array_lc_t Array(LowCardinality(Nullable(UInt64))),
    nullable_array3_lc_t Array(Array(Array(LowCardinality(Nullable(UInt64)))))
) engine = MergeTree()
        ORDER BY (id);
DROP TABLE IF EXISTS vertx_test_int128;
CREATE TABLE vertx_test_int128 (
    id Int8,
    simple_t Int128,
    nullable_t Nullable(Int128),
    array_t Array(Int128),
    array3_t Array(Array(Array(Int128))),
    nullable_array_t Array(Nullable(Int128)),
    nullable_array3_t Array(Array(Array(Nullable(Int128)))),
    simple_lc_t LowCardinality(Int128),
    nullable_lc_t LowCardinality(Nullable(Int128)),
    array_lc_t Array(LowCardinality(Int128)),
    array3_lc_t Array(Array(Array(LowCardinality(Int128)))),
    nullable_array_lc_t Array(LowCardinality(Nullable(Int128))),
    nullable_array3_lc_t Array(Array(Array(LowCardinality(Nullable(Int128)))))
) engine = MergeTree()
        ORDER BY (id);
DROP TABLE IF EXISTS vertx_test_string;
CREATE TABLE vertx_test_string (
    id Int8,
    simple_t String,
    nullable_t Nullable(String),
    array_t Array(String),
    array3_t Array(Array(Array(String))),
    nullable_array_t Array(Nullable(String)),
    nullable_array3_t Array(Array(Array(Nullable(String)))),
    simple_lc_t LowCardinality(String),
    nullable_lc_t LowCardinality(Nullable(String)),
    array_lc_t Array(LowCardinality(String)),
    array3_lc_t Array(Array(Array(LowCardinality(String)))),
    nullable_array_lc_t Array(LowCardinality(Nullable(String))),
    nullable_array3_lc_t Array(Array(Array(LowCardinality(Nullable(String)))))
) engine = MergeTree()
        ORDER BY (id);
DROP TABLE IF EXISTS vertx_test_fixedstring;
CREATE TABLE vertx_test_fixedstring (
    id Int8,
    simple_t FixedString(12),
    nullable_t Nullable(FixedString(12)),
    array_t Array(FixedString(12)),
    array3_t Array(Array(Array(FixedString(12)))),
    nullable_array_t Array(Nullable(FixedString(12))),
    nullable_array3_t Array(Array(Array(Nullable(FixedString(12))))),
    simple_lc_t LowCardinality(FixedString(12)),
    nullable_lc_t LowCardinality(Nullable(FixedString(12))),
    array_lc_t Array(LowCardinality(FixedString(12))),
    array3_lc_t Array(Array(Array(LowCardinality(FixedString(12))))),
    nullable_array_lc_t Array(LowCardinality(Nullable(FixedString(12)))),
    nullable_array3_lc_t Array(Array(Array(LowCardinality(Nullable(FixedString(12))))))
) engine = MergeTree()
        ORDER BY (id);
DROP TABLE IF EXISTS vertx_test_datetime;
CREATE TABLE vertx_test_datetime (
    id Int8,
    simple_t DateTime,
    nullable_t Nullable(DateTime),
    array_t Array(DateTime),
    array3_t Array(Array(Array(DateTime))),
    nullable_array_t Array(Nullable(DateTime)),
    nullable_array3_t Array(Array(Array(Nullable(DateTime)))),
    simple_lc_t LowCardinality(DateTime),
    nullable_lc_t LowCardinality(Nullable(DateTime)),
    array_lc_t Array(LowCardinality(DateTime)),
    array3_lc_t Array(Array(Array(LowCardinality(DateTime)))),
    nullable_array_lc_t Array(LowCardinality(Nullable(DateTime))),
    nullable_array3_lc_t Array(Array(Array(LowCardinality(Nullable(DateTime)))))
) engine = MergeTree()
        ORDER BY (id);
DROP TABLE IF EXISTS vertx_test_datetime64;
CREATE TABLE vertx_test_datetime64 (
    id Int8,
    simple_t DateTime64,
    nullable_t Nullable(DateTime64),
    array_t Array(DateTime64),
    array3_t Array(Array(Array(DateTime64))),
    nullable_array_t Array(Nullable(DateTime64)),
    nullable_array3_t Array(Array(Array(Nullable(DateTime64))))
) engine = MergeTree()
        ORDER BY (id);
DROP TABLE IF EXISTS vertx_test_date;
CREATE TABLE vertx_test_date (
    id Int8,
    simple_t Date,
    nullable_t Nullable(Date),
    array_t Array(Date),
    array3_t Array(Array(Array(Date))),
    nullable_array_t Array(Nullable(Date)),
    nullable_array3_t Array(Array(Array(Nullable(Date)))),
    simple_lc_t LowCardinality(Date),
    nullable_lc_t LowCardinality(Nullable(Date)),
    array_lc_t Array(LowCardinality(Date)),
    array3_lc_t Array(Array(Array(LowCardinality(Date)))),
    nullable_array_lc_t Array(LowCardinality(Nullable(Date))),
    nullable_array3_lc_t Array(Array(Array(LowCardinality(Nullable(Date)))))
) engine = MergeTree()
        ORDER BY (id);
DROP TABLE IF EXISTS vertx_test_uuid;
CREATE TABLE vertx_test_uuid (
    id Int8,
    simple_t UUID,
    nullable_t Nullable(UUID),
    array_t Array(UUID),
    array3_t Array(Array(Array(UUID))),
    nullable_array_t Array(Nullable(UUID)),
    nullable_array3_t Array(Array(Array(Nullable(UUID))))
) engine = MergeTree()
        ORDER BY (id);
DROP TABLE IF EXISTS vertx_test_decimal32;
CREATE TABLE vertx_test_decimal32 (
    id Int8,
    simple_t Decimal32(4),
    nullable_t Nullable(Decimal32(4)),
    array_t Array(Decimal32(4)),
    array3_t Array(Array(Array(Decimal32(4)))),
    nullable_array_t Array(Nullable(Decimal32(4))),
    nullable_array3_t Array(Array(Array(Nullable(Decimal32(4)))))
) engine = MergeTree()
        ORDER BY (id);
DROP TABLE IF EXISTS vertx_test_decimal64;
CREATE TABLE vertx_test_decimal64 (
    id Int8,
    simple_t Decimal64(4),
    nullable_t Nullable(Decimal64(4)),
    array_t Array(Decimal64(4)),
    array3_t Array(Array(Array(Decimal64(4)))),
    nullable_array_t Array(Nullable(Decimal64(4))),
    nullable_array3_t Array(Array(Array(Nullable(Decimal64(4)))))
) engine = MergeTree()
        ORDER BY (id);
DROP TABLE IF EXISTS vertx_test_decimal128;
CREATE TABLE vertx_test_decimal128 (
    id Int8,
    simple_t Decimal128(4),
    nullable_t Nullable(Decimal128(4)),
    array_t Array(Decimal128(4)),
    array3_t Array(Array(Array(Decimal128(4)))),
    nullable_array_t Array(Nullable(Decimal128(4))),
    nullable_array3_t Array(Array(Array(Nullable(Decimal128(4)))))
) engine = MergeTree()
        ORDER BY (id);
DROP TABLE IF EXISTS vertx_test_enum8;
CREATE TABLE vertx_test_enum8 (
    id Int8,
    simple_t Enum8('v0' = -128, 'v1' = -2,'v2' = -1, 'v3' = 0,'v4' = 1, 'v5' = 2, 'v6' = 127),
    nullable_t Nullable(Enum8('v0' = -128, 'v1' = -2,'v2' = -1, 'v3' = 0,'v4' = 1, 'v5' = 2, 'v6' = 127)),
    array_t Array(Enum8('v0' = -128, 'v1' = -2,'v2' = -1, 'v3' = 0,'v4' = 1, 'v5' = 2, 'v6' = 127)),
    array3_t Array(Array(Array(Enum8('v0' = -128, 'v1' = -2,'v2' = -1, 'v3' = 0,'v4' = 1, 'v5' = 2, 'v6' = 127)))),
    nullable_array_t Array(Nullable(Enum8('v0' = -128, 'v1' = -2,'v2' = -1, 'v3' = 0,'v4' = 1, 'v5' = 2, 'v6' = 127))),
    nullable_array3_t Array(Array(Array(Nullable(Enum8('v0' = -128, 'v1' = -2,'v2' = -1, 'v3' = 0,'v4' = 1, 'v5' = 2, 'v6' = 127)))))
) engine = MergeTree()
        ORDER BY (id);
DROP TABLE IF EXISTS vertx_test_enum16;
CREATE TABLE vertx_test_enum16 (
    id Int8,
    simple_t Enum16('v0' = -32768, 'v1' = -2,'v2' = -1, 'v3' = 0,'v4' = 1, 'v5' = 2, 'v6' = 32767),
    nullable_t Nullable(Enum16('v0' = -32768, 'v1' = -2,'v2' = -1, 'v3' = 0,'v4' = 1, 'v5' = 2, 'v6' = 32767)),
    array_t Array(Enum16('v0' = -32768, 'v1' = -2,'v2' = -1, 'v3' = 0,'v4' = 1, 'v5' = 2, 'v6' = 32767)),
    array3_t Array(Array(Array(Enum16('v0' = -32768, 'v1' = -2,'v2' = -1, 'v3' = 0,'v4' = 1, 'v5' = 2, 'v6' = 32767)))),
    nullable_array_t Array(Nullable(Enum16('v0' = -32768, 'v1' = -2,'v2' = -1, 'v3' = 0,'v4' = 1, 'v5' = 2, 'v6' = 32767))),
    nullable_array3_t Array(Array(Array(Nullable(Enum16('v0' = -32768, 'v1' = -2,'v2' = -1, 'v3' = 0,'v4' = 1, 'v5' = 2, 'v6' = 32767)))))
) engine = MergeTree()
        ORDER BY (id);
DROP TABLE IF EXISTS vertx_test_float32;
CREATE TABLE vertx_test_float32 (
    id Int8,
    simple_t Float32,
    nullable_t Nullable(Float32),
    array_t Array(Float32),
    array3_t Array(Array(Array(Float32))),
    nullable_array_t Array(Nullable(Float32)),
    nullable_array3_t Array(Array(Array(Nullable(Float32)))),
    simple_lc_t LowCardinality(Float32),
    nullable_lc_t LowCardinality(Nullable(Float32)),
    array_lc_t Array(LowCardinality(Float32)),
    array3_lc_t Array(Array(Array(LowCardinality(Float32)))),
    nullable_array_lc_t Array(LowCardinality(Nullable(Float32))),
    nullable_array3_lc_t Array(Array(Array(LowCardinality(Nullable(Float32)))))
) engine = MergeTree()
        ORDER BY (id);
DROP TABLE IF EXISTS vertx_test_float64;
CREATE TABLE vertx_test_float64 (
    id Int8,
    simple_t Float64,
    nullable_t Nullable(Float64),
    array_t Array(Float64),
    array3_t Array(Array(Array(Float64))),
    nullable_array_t Array(Nullable(Float64)),
    nullable_array3_t Array(Array(Array(Nullable(Float64)))),
    simple_lc_t LowCardinality(Float64),
    nullable_lc_t LowCardinality(Nullable(Float64)),
    array_lc_t Array(LowCardinality(Float64)),
    array3_lc_t Array(Array(Array(LowCardinality(Float64)))),
    nullable_array_lc_t Array(LowCardinality(Nullable(Float64))),
    nullable_array3_lc_t Array(Array(Array(LowCardinality(Nullable(Float64)))))
) engine = MergeTree()
        ORDER BY (id);
DROP TABLE IF EXISTS vertx_test_ipv6;
CREATE TABLE vertx_test_ipv6 (
    id Int8,
    simple_t IPv6,
    nullable_t Nullable(IPv6),
    array_t Array(IPv6),
    array3_t Array(Array(Array(IPv6))),
    nullable_array_t Array(Nullable(IPv6)),
    nullable_array3_t Array(Array(Array(Nullable(IPv6)))),
    simple_lc_t LowCardinality(IPv6),
    nullable_lc_t LowCardinality(Nullable(IPv6)),
    array_lc_t Array(LowCardinality(IPv6)),
    array3_lc_t Array(Array(Array(LowCardinality(IPv6)))),
    nullable_array_lc_t Array(LowCardinality(Nullable(IPv6))),
    nullable_array3_lc_t Array(Array(Array(LowCardinality(Nullable(IPv6)))))
) engine = MergeTree()
        ORDER BY (id);
DROP TABLE IF EXISTS vertx_test_ipv4;
CREATE TABLE vertx_test_ipv4 (
    id Int8,
    simple_t IPv4,
    nullable_t Nullable(IPv4),
    array_t Array(IPv4),
    array3_t Array(Array(Array(IPv4))),
    nullable_array_t Array(Nullable(IPv4)),
    nullable_array3_t Array(Array(Array(Nullable(IPv4)))),
    simple_lc_t LowCardinality(IPv4),
    nullable_lc_t LowCardinality(Nullable(IPv4)),
    array_lc_t Array(LowCardinality(IPv4)),
    array3_lc_t Array(Array(Array(LowCardinality(IPv4)))),
    nullable_array_lc_t Array(LowCardinality(Nullable(IPv4))),
    nullable_array3_lc_t Array(Array(Array(LowCardinality(Nullable(IPv4)))))
) engine = MergeTree()
        ORDER BY (id);
