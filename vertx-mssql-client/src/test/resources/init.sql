-- TCK usage --
-- immutable for select query testing --
DROP TABLE IF EXISTS immutable;
CREATE TABLE immutable
(
  id      integer       NOT NULL,
  message varchar(2048) NOT NULL,
  PRIMARY KEY (id)
);

INSERT INTO immutable (id, message)
VALUES (1, 'fortune: No such file or directory');
INSERT INTO immutable (id, message)
VALUES (2, 'A computer scientist is someone who fixes things that aren''t broken.');
INSERT INTO immutable (id, message)
VALUES (3, 'After enough decimal places, nobody gives a damn.');
INSERT INTO immutable (id, message)
VALUES (4, 'A bad random number generator: 1, 1, 1, 1, 1, 4.33e+67, 1, 1, 1');
INSERT INTO immutable (id, message)
VALUES (5, 'A computer program does what you tell it to do, not what you want it to do.');
INSERT INTO immutable (id, message)
VALUES (6, 'Emacs is a nice operating system, but I prefer UNIX. — Tom Christaensen');
INSERT INTO immutable (id, message)
VALUES (7, 'Any program that runs right is obsolete.');
INSERT INTO immutable (id, message)
VALUES (8, 'A list is only as strong as its weakest link. — Donald Knuth');
INSERT INTO immutable (id, message)
VALUES (9, 'Feature: A bug with seniority.');
INSERT INTO immutable (id, message)
VALUES (10, 'Computers make very fast, very accurate mistakes.');
INSERT INTO immutable (id, message)
VALUES (11, '<script>alert("This should not be displayed in a browser alert box.");</script>');
INSERT INTO immutable (id, message)
VALUES (12, 'フレームワークのベンチマーク');
-- immutable for select query testing --

-- mutable for insert,update,delete query testing
DROP TABLE IF EXISTS mutable;
CREATE TABLE mutable
(
  id  integer       NOT NULL,
  val varchar(2048) NOT NULL,
  PRIMARY KEY (id)
);
-- mutable for insert,update,delete query testing

-- table for test ANSI SQL data type codecs
DROP TABLE IF EXISTS basicdatatype;
CREATE TABLE basicdatatype
(
    id           INT,
    test_int_2   SMALLINT,
    test_int_4   INT,
    test_int_8   BIGINT,
    test_float_4 REAL,
    test_float_8 DOUBLE PRECISION,
    test_numeric NUMERIC(5, 2),
    test_decimal DECIMAL,
    test_boolean BIT,
    test_char    CHAR(8),
    test_varchar VARCHAR(20),
    test_date    DATE,
    test_time    TIME(2)
);
INSERT INTO basicdatatype(id, test_int_2, test_int_4, test_int_8, test_float_4, test_float_8, test_numeric,
                          test_decimal, test_boolean, test_char, test_varchar, test_date, test_time)
VALUES (1, 32767, 2147483647, 9223372036854775807, 3.40282E38, 1.7976931348623157E308, 999.99,
        12345, 1, 'testchar', 'testvarchar', '2019-01-01', '18:45:02');
INSERT INTO basicdatatype(id, test_int_2, test_int_4, test_int_8, test_float_4, test_float_8, test_numeric,
                          test_decimal, test_boolean, test_char, test_varchar, test_date, test_time)
VALUES ('2', '32767', '2147483647', '9223372036854775807', '3.40282E38', '1.7976931348623157E308', '999.99',
        '12345', 0, 'testchar', 'testvarchar', '2019-01-01', '18:45:02');
INSERT INTO basicdatatype(id, test_int_2, test_int_4, test_int_8, test_float_4, test_float_8, test_numeric,
                          test_decimal, test_boolean, test_char, test_varchar, test_date, test_time)
VALUES (3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
-- table for test ANSI SQL data type codecs

-- table for testing nullable data types
DROP TABLE IF EXISTS nullable_datatype;
CREATE TABLE nullable_datatype
(
    id                  INT,
    test_tinyint        TINYINT,
    test_smallint       SMALLINT,
    test_int            INT,
    test_bigint         BIGINT,
    test_float_4        REAL,
    test_float_8        DOUBLE PRECISION,
    test_numeric        NUMERIC(5, 2),
    test_decimal        DECIMAL,
    test_boolean        BIT,
    test_char           CHAR(8),
    test_varchar        VARCHAR(20),
    test_date           DATE,
    test_time           TIME(5),
    test_datetime2      DATETIME2(4),
    test_datetimeoffset DATETIMEOFFSET(3),
    test_binary         BINARY(20),
    test_varbinary      VARBINARY(20)
);

INSERT INTO nullable_datatype(id, test_tinyint, test_smallint, test_int, test_bigint, test_float_4, test_float_8,
                              test_numeric, test_decimal, test_boolean, test_char, test_varchar, test_date, test_time,
                              test_datetime2, test_datetimeoffset, test_binary, test_varbinary)
VALUES (1, 127, 32767, 2147483647, 9223372036854775807, 3.40282E38, 1.7976931348623157E308, 999.99,
        12345, 1, 'testchar', 'testvarchar', '2019-01-01', '18:45:02', '2019-01-01T18:45:02',
        '2019-01-01T18:45:02-03:15', CONVERT(VARBINARY, 'hello world'), CONVERT(VARBINARY, 'big apple'));
INSERT INTO nullable_datatype(id, test_tinyint, test_smallint, test_int, test_bigint, test_float_4, test_float_8,
                              test_numeric, test_decimal, test_boolean, test_char, test_varchar, test_date, test_time,
                              test_datetime2, test_datetimeoffset, test_binary, test_varbinary)
VALUES (2, 127, 32767, 2147483647, 9223372036854775807, 3.40282E38, 1.7976931348623157E308, 999.99,
        12345, 1, 'testchar', 'testvarchar', '2019-01-01', '18:45:02', '2019-01-01T18:45:02',
        '2019-01-01T18:45:02-03:15', CONVERT(VARBINARY, 'hello world'), CONVERT(VARBINARY, 'big apple'));
INSERT INTO nullable_datatype(id, test_tinyint, test_smallint, test_int, test_bigint, test_float_4, test_float_8,
                              test_numeric, test_decimal, test_boolean, test_char, test_varchar, test_date, test_time,
                              test_datetime2, test_datetimeoffset, test_binary, test_varbinary)
VALUES (3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
-- table for testing nullable data types

-- table for testing NOT NULL data types
DROP TABLE IF EXISTS not_nullable_datatype;
CREATE TABLE not_nullable_datatype
(
    id                  INT              NOT NULL,
    test_tinyint        TINYINT          NOT NULL,
    test_smallint       SMALLINT         NOT NULL,
    test_int            INT              NOT NULL,
    test_bigint         BIGINT           NOT NULL,
    test_float_4        REAL             NOT NULL,
    test_float_8        DOUBLE PRECISION NOT NULL,
    test_numeric        NUMERIC(5, 2)    NOT NULL,
    test_decimal        DECIMAL          NOT NULL,
    test_boolean        BIT              NOT NULL,
    test_char           CHAR(8)          NOT NULL,
    test_varchar        VARCHAR(20)      NOT NULL,
    test_date           DATE             NOT NULL,
    test_time           TIME(6)          NOT NULL,
    test_datetime2      DATETIME2(7)     NOT NULL,
    test_datetimeoffset DATETIMEOFFSET(1) NOT NULL,
    test_binary         BINARY(20)       NOT NULL,
    test_varbinary      VARBINARY(20)    NOT NULL
);

INSERT INTO not_nullable_datatype(id, test_tinyint, test_smallint, test_int, test_bigint, test_float_4, test_float_8,
                                  test_numeric, test_decimal, test_boolean, test_char, test_varchar, test_date,
                                  test_time, test_datetime2, test_datetimeoffset, test_binary, test_varbinary)
VALUES (1, 127, 32767, 2147483647, 9223372036854775807, 3.40282E38, 1.7976931348623157E308, 999.99,
        12345, 1, 'testchar', 'testvarchar', '2019-01-01', '18:45:02', '2019-01-01T18:45:02',
        '2019-01-01T18:45:02-03:15', CONVERT(VARBINARY, 'hello world'), CONVERT(VARBINARY, 'big apple'));
INSERT INTO not_nullable_datatype(id, test_tinyint, test_smallint, test_int, test_bigint, test_float_4, test_float_8,
                                  test_numeric, test_decimal, test_boolean, test_char, test_varchar, test_date,
                                  test_time, test_datetime2, test_datetimeoffset, test_binary, test_varbinary)
VALUES (2, 127, 32767, 2147483647, 9223372036854775807, 3.40282E38, 1.7976931348623157E308, 999.99,
        12345, 1, 'testchar', 'testvarchar', '2019-01-01', '18:45:02', '2019-01-01T18:45:02',
        '2019-01-01T18:45:02-03:15', CONVERT(VARBINARY, 'hello world'), CONVERT(VARBINARY, 'big apple'));
-- table for testing NOT NULL data types

-- Fortune table
DROP TABLE IF EXISTS Fortune;
CREATE TABLE Fortune
(
    id      integer       NOT NULL,
    message varchar(2048) NOT NULL,
    PRIMARY KEY (id)
);

INSERT INTO Fortune (id, message)
VALUES (1, 'fortune: No such file or directory');
INSERT INTO Fortune (id, message)
VALUES (2, 'A computer scientist is someone who fixes things that aren''t broken.');
INSERT INTO Fortune (id, message)
VALUES (3, 'After enough decimal places, nobody gives a damn.');
INSERT INTO Fortune (id, message)
VALUES (4, 'A bad random number generator: 1, 1, 1, 1, 1, 4.33e+67, 1, 1, 1');
INSERT INTO Fortune (id, message)
VALUES (5, 'A computer program does what you tell it to do, not what you want it to do.');
INSERT INTO Fortune (id, message)
VALUES (6, 'Emacs is a nice operating system, but I prefer UNIX. — Tom Christaensen');
INSERT INTO Fortune (id, message)
VALUES (7, 'Any program that runs right is obsolete.');
INSERT INTO Fortune (id, message)
VALUES (8, 'A list is only as strong as its weakest link. — Donald Knuth');
INSERT INTO Fortune (id, message)
VALUES (9, 'Feature: A bug with seniority.');
INSERT INTO Fortune (id, message)
VALUES (10, 'Computers make very fast, very accurate mistakes.');
INSERT INTO Fortune (id, message)
VALUES (11, '<script>alert("This should not be displayed in a browser alert box.");</script>');
INSERT INTO Fortune (id, message)
VALUES (12, 'フレームワークのベンチマーク');
