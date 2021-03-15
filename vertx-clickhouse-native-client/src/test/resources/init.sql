
# USE testschema;
# datatype testing table
-- basic data type table --
-- used by TCK
DROP TABLE IF EXISTS basicdatatype;
CREATE TABLE basicdatatype
(
    id           Int16,
    test_int_2   Nullable(Int16),
    test_int_4   Nullable(Int32),
    test_int_8   Nullable(Int64),
    test_float_4 Nullable(Float32),
    test_float_8 Nullable(Float64),
    test_numeric Nullable(DECIMAL64(2)),
    test_decimal Nullable(DECIMAL64(0)),
    test_boolean Nullable(BOOLEAN),
    test_char    Nullable(FixedString(8)),
    test_varchar Nullable(String(20)),
    test_date    Nullable(DATE)
) engine = MergeTree()
        ORDER BY (id);
INSERT INTO basicdatatype(id, test_int_2, test_int_4, test_int_8,
                          test_float_4, test_float_8, test_numeric, test_decimal,
                          test_boolean, test_char, test_varchar,
                          test_date)
VALUES (1, 32767, 2147483647, 9223372036854775807,
        3.40282E38, 1.7976931348623157E308, 999.99, 12345,
        1, 'testchar', 'testvarchar',
        '2019-01-01');
INSERT INTO basicdatatype(id, test_int_2, test_int_4, test_int_8,
                          test_float_4, test_float_8, test_numeric, test_decimal,
                          test_boolean, test_char, test_varchar,
                          test_date)
VALUES (2, 32767, 2147483647, 9223372036854775807,
        3.40282E38, 1.7976931348623157E308, 999.99, 12345,
        1, 'testchar', 'testvarchar',
        '2019-01-01');
INSERT INTO basicdatatype(id, test_int_2, test_int_4, test_int_8,
                          test_float_4, test_float_8, test_numeric, test_decimal,
                          test_boolean, test_char, test_varchar, test_date)
VALUES (3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);

-- Collector API testing --
-- used by TCK
DROP TABLE IF EXISTS collector_test;
CREATE TABLE collector_test
(
    id           Int16,
    test_int_2   Int16,
    test_int_4   Int32,
    test_int_8   Int64,
    test_float   Float32,
    test_double  Float64,
    test_varchar VARCHAR(20)
) engine = MergeTree()
          ORDER BY (id);
INSERT INTO collector_test VALUES (1, 32767, 2147483647, 9223372036854775807, 123.456, 1.234567, 'HELLO,WORLD');
INSERT INTO collector_test VALUES (2, 32767, 2147483647, 9223372036854775807, 123.456, 1.234567, 'hello,world');
