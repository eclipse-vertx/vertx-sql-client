-- Fortune table --
-- used by TCK
DROP TABLE IF EXISTS Fortune;
CREATE TABLE Fortune (
  id       integer       NOT NULL GENERATED AS IDENTITY (START WITH 1, INCREMENT BY 1),
  message  varchar(2048),
  PRIMARY KEY  (id)
);
INSERT INTO Fortune (message) VALUES ('fortune: No such file or directory');
INSERT INTO Fortune (message) VALUES ('A computer scientist is someone who fixes things that aren''t broken.');
INSERT INTO Fortune (message) VALUES ('After enough decimal places, nobody gives a damn.');
INSERT INTO Fortune (message) VALUES ('A bad random number generator: 1, 1, 1, 1, 1, 4.33e+67, 1, 1, 1');
INSERT INTO Fortune (message) VALUES ('A computer program does what you tell it to do, not what you want it to do.');
INSERT INTO Fortune (message) VALUES ('Emacs is a nice operating system, but I prefer UNIX. — Tom Christaensen');
INSERT INTO Fortune (message) VALUES ('Any program that runs right is obsolete.');
INSERT INTO Fortune (message) VALUES ('A list is only as strong as its weakest link. — Donald Knuth');
INSERT INTO Fortune (message) VALUES ('Feature: A bug with seniority.');
INSERT INTO Fortune (message) VALUES ('Computers make very fast, very accurate mistakes.');
INSERT INTO Fortune (message) VALUES ('<script>alert("This should not be displayed in a browser alert box.");</script>');
INSERT INTO Fortune (message) VALUES ('フレームワークのベンチマーク');

-- immutable table for select query testing --
-- used by TCK
DROP TABLE IF EXISTS immutable;
CREATE TABLE immutable
(
  id      integer       NOT NULL,
  message varchar(2048) NOT NULL,
  PRIMARY KEY (id)
);
INSERT INTO immutable (id, message) VALUES (1, 'fortune: No such file or directory');
INSERT INTO immutable (id, message) VALUES (2, 'A computer scientist is someone who fixes things that aren''t broken.');
INSERT INTO immutable (id, message) VALUES (3, 'After enough decimal places, nobody gives a damn.');
INSERT INTO immutable (id, message) VALUES (4, 'A bad random number generator: 1, 1, 1, 1, 1, 4.33e+67, 1, 1, 1');
INSERT INTO immutable (id, message) VALUES (5, 'A computer program does what you tell it to do, not what you want it to do.');
INSERT INTO immutable (id, message) VALUES (6, 'Emacs is a nice operating system, but I prefer UNIX. — Tom Christaensen');
INSERT INTO immutable (id, message) VALUES (7, 'Any program that runs right is obsolete.');
INSERT INTO immutable (id, message) VALUES (8, 'A list is only as strong as its weakest link. — Donald Knuth');
INSERT INTO immutable (id, message) VALUES (9, 'Feature: A bug with seniority.');
INSERT INTO immutable (id, message) VALUES (10, 'Computers make very fast, very accurate mistakes.');
INSERT INTO immutable (id, message) VALUES (11, '<script>alert("This should not be displayed in a browser alert box.");</script>');
INSERT INTO immutable (id, message) VALUES (12, 'フレームワークのベンチマーク');

-- mutable for insert,update,delete query testing --
-- used by TCK
DROP TABLE IF EXISTS mutable;
CREATE TABLE mutable
(
  id  integer       NOT NULL,
  val varchar(2048) NOT NULL,
  PRIMARY KEY (id)
);

-- basic data type table --
-- used by TCK
DROP TABLE IF EXISTS basicdatatype;
CREATE TABLE basicdatatype
(
    id           INTEGER,
    test_int_2   SMALLINT,
    test_int_4   INTEGER,
    test_int_8   BIGINT,
    test_float_4 REAL,
    test_float_8 DOUBLE,
    test_numeric NUMERIC(5, 2),
    test_decimal DECIMAL,
    test_boolean BOOLEAN,
    test_char    CHAR(8),
    test_varchar VARCHAR(20),
    test_date    DATE,
    test_time    TIME
);
INSERT INTO basicdatatype(id, test_int_2, test_int_4, test_int_8, 
                          test_float_4, test_float_8, test_numeric, test_decimal, 
                          test_boolean, test_char, test_varchar, 
                          test_date, test_time)
VALUES (1, 32767, 2147483647, 9223372036854775807, 
        3.40282E38, 1.7976931348623157E308, 999.99, 12345, 
        TRUE, 'testchar', 'testvarchar', 
        '2019-01-01', '18:45:02');
INSERT INTO basicdatatype(id, test_int_2, test_int_4, test_int_8, 
                          test_float_4, test_float_8, test_numeric, test_decimal, 
                          test_boolean, test_char, test_varchar, 
                          test_date, test_time)
VALUES (2, 32767, 2147483647, 9223372036854775807, 
        3.40282E38, 1.7976931348623157E308, 999.99, 12345, 
        TRUE, 'testchar', 'testvarchar', 
        '2019-01-01', '18:45:02');
INSERT INTO basicdatatype(id, test_int_2, test_int_4, test_int_8, test_float_4, test_float_8, test_numeric, test_decimal, test_boolean, test_char, test_varchar, test_date, test_time)
VALUES (3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);

-- Collector API testing --
-- used by TCK
DROP TABLE IF EXISTS collector_test;
CREATE TABLE collector_test
(
    id           INT,
    test_int_2   SMALLINT,
    test_int_4   INT,
    test_int_8   BIGINT,
    test_float   FLOAT,
    test_double  DOUBLE,
    test_varchar VARCHAR(20)
);
INSERT INTO collector_test VALUES (1, 32767, 2147483647, 9223372036854775807, 123.456, 1.234567, 'HELLO,WORLD');
INSERT INTO collector_test VALUES (2, 32767, 2147483647, 9223372036854775807, 123.456, 1.234567, 'hello,world');

-- used by DB2DataTypeTest
DROP TABLE IF EXISTS db2_types;
CREATE TABLE db2_types
(
    id           INT,
    test_byte    SMALLINT,
    test_float   FLOAT,
    test_bytes   VARCHAR(255) for bit data,
    test_tstamp  TIMESTAMP
);

-- Sequence used by QueryVariationsTest
DROP SEQUENCE my_seq;
CREATE SEQUENCE my_seq INCREMENT BY 1 START WITH 1;
